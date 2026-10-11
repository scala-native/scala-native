package scala.scalanative.unsafe

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import scala.scalanative.NIRCompiler
import scala.scalanative.api.CompilationFailedException
import scala.scalanative.buildinfo.ScalaNativeBuildInfo.scalaVersion
import scala.scalanative.linker.compileAndLoad
import scala.scalanative.nir._

class LayoutIntrinsicsTest {
  private def methodName(sig: Sig): Option[String] = sig.unmangled match {
    case Sig.Method(name, _, _) => Some(name)
    case _                      => None
  }

  private class Methods(defns: Seq[Defn]) {
    def apply(name: String, owner: String = "Test$"): MethodBody = {
      val matches = defns.collect {
        case d: Defn.Define if (d.name match {
              case Global.Member(Global.Top(`owner`), sig) =>
                methodName(sig).contains(name)
              case _ => false
            }) =>
          d
      }
      assertEquals(s"Expected one $owner.$name definition", 1, matches.size)
      new MethodBody(matches.head)
    }
  }

  private case class CallResult(value: Val, args: Seq[Val])

  private class MethodBody(val definition: Defn.Define) {
    val insts = definition.insts
    val ops = insts.collect { case Inst.Let(_, op, _) => op }
    val params = insts.head.asInstanceOf[Inst.Label].params
    private val definitions = insts.collect {
      case Inst.Let(id, op, _) => id -> op
    }.toMap

    def calls(name: String): Seq[CallResult] = insts.collect {
      case Inst.Let(id, call @ Op.Call(_, callee, args), _) if (callee match {
            case Val.Global(Global.Member(_, sig), _) =>
              methodName(sig).contains(name)
            case Val.Local(id, _) =>
              definitions.get(id).exists {
                case Op.Method(_, sig) => methodName(sig).contains(name)
                case _                 => false
              }
            case _ => false
          }) =>
        CallResult(Val.Local(id, call.resty), args)
    }

    def oneCall(name: String): CallResult = {
      val found = calls(name)
      assertEquals(
        s"Expected one $name call:\n${definition.show}",
        1,
        found.size
      )
      found.head
    }

    def module(name: String): Val = {
      val found = insts.collect {
        case Inst.Let(id, op @ Op.Module(Global.Top(`name`)), _) =>
          Val.Local(id, op.resty)
      }
      assertEquals(
        s"Expected one $name module:\n${definition.show}",
        1,
        found.size
      )
      found.head
    }

    def assertIntrinsic(op: Op): Unit = {
      val values = insts.collect {
        case Inst.Let(id, `op`, _) => Val.Local(id, op.resty)
      }
      assertEquals(definition.show, 1, values.size)
      assertEquals(
        definition.show,
        values,
        insts.collect { case Inst.Ret(value) => origin(value) }
      )
      assertNoCalls()
    }

    private def origin(value: Val): Val = value match {
      case Val.Local(id, _) =>
        definitions.get(id) match {
          case Some(Op.Copy(v))       => origin(v)
          case Some(Op.As(_, v))      => origin(v)
          case Some(Op.Conv(_, _, v)) => origin(v)
          case _                      => value
        }
      case _ => value
    }

    def assertCallArgument(name: String, index: Int, expected: Val): Unit =
      assertEquals(
        definition.show,
        origin(expected),
        origin(oneCall(name).args(index))
      )

    def assertTagLayout(
        member: String,
        evidence: Val,
        elements: Option[Val] = None
    ): Unit = {
      val layout = oneCall(member)
      assertEquals(definition.show, origin(evidence), origin(layout.args.head))
      assertFalse(
        s"Unknown layouts must use tag evidence:\n${definition.show}",
        ops.exists {
          case _: Op.SizeOf | _: Op.AlignmentOf => true
          case _                                => false
        }
      )
      val allocations = ops.collect { case op: Op.Stackalloc => op }
      if (allocations.nonEmpty) {
        assertEquals(definition.show, 1, allocations.size)
        val allocation = allocations.head
        assertEquals(definition.show, Type.Byte, allocation.ty)
        val product = origin(allocation.n) match {
          case Val.Local(id, _) => definitions(id)
          case _                => throw new AssertionError(definition.show)
        }
        product match {
          case Op.Bin(Bin.Imul, Type.Long, size, count) =>
            assertEquals(definition.show, layout.value, origin(size))
            elements.foreach(n =>
              assertEquals(definition.show, origin(n), origin(count))
            )
          case _ => fail(s"Expected tag.size * count:\n${definition.show}")
        }
      } else {
        val returns = insts.collect { case Inst.Ret(value) => origin(value) }
        assertEquals(definition.show, Seq(layout.value), returns)
      }
    }

    def assertNoCalls(): Unit = assertFalse(
      definition.show,
      ops.exists(_.isInstanceOf[Op.Call])
    )
  }

  private def inspect(source: String)(check: Methods => Unit): Unit =
    compileAndLoad("Test.scala" -> source)(defns => check(new Methods(defns)))

  @Test def layoutEvidenceCountsAsUsed(): Unit = {
    assumeFalse(scalaVersion.startsWith("3."))
    val options =
      if (scalaVersion.startsWith("2.12."))
        Array("-Ywarn-unused:implicits", "-Xfatal-warnings")
      else Array("-Wunused:implicits", "-Werror")
    NIRCompiler(
      _.compile(
        """import scala.scalanative.unsafe._
          |import scala.scalanative.runtime.Intrinsics
          |object Test {
          |  def size[T](implicit tag: Tag[T]) = sizeOf[T]
          |  def alignment[T](implicit tag: Tag[T]) = alignmentOf[T]
          |  def stack[T](implicit tag: Tag[T]) = stackalloc[T](2)
          |  def direct[T](implicit tag: Tag[T]) = Intrinsics.sizeOf[T]
          |}
          |""".stripMargin,
        options
      )
    )
    assertThrows(
      classOf[CompilationFailedException],
      () =>
        NIRCompiler(
          _.compile(
            """import scala.scalanative.unsafe._
              |object Test { def unused[T](implicit tag: Tag[T]) = sizeOf[Int] }
              |""".stripMargin,
            options
          )
        )
    )
  }

  @Test def inlineWrappersPreserveExplicitEvidence(): Unit = {
    assumeTrue(scalaVersion.startsWith("3."))
    inspect(
      """import scala.scalanative.unsafe.*
        |object Test {
        |  type Foo
        |  inline def size[T](using Tag[T]) = sizeOf[T]
        |  inline def alignment[T](using Tag[T]) = alignmentOf[T]
        |  inline def stack[T](using Tag[T]) = stackalloc[T](2)
        |  def runSize(tag: Tag[Foo]) = size[Foo](using tag)
        |  def runAlignment(tag: Tag[Foo]) = alignment[Foo](using tag)
        |  def runStack(tag: Tag[Foo]) = stack[Foo](using tag)
        |  def concrete = size[Int](using Tag.Int)
        |}
        |""".stripMargin
    ) { methods =>
      Seq("runSize", "runAlignment", "runStack").foreach { name =>
        val method = methods(name)
        method.assertTagLayout(
          if (name == "runAlignment") "alignment" else "size",
          method.params.last,
          if (name == "runStack") Some(Val.Int(2)) else None
        )
      }
      methods("concrete").assertIntrinsic(Op.SizeOf(Type.Int))
    }
  }

  @Test def concreteLayoutsDoNotUseTags(): Unit = {
    inspect(
      """import scala.scalanative.runtime.Intrinsics
        |import scala.scalanative.unsafe._
        |object Test {
        |  def size = Intrinsics.sizeOf[CArray[Byte, Nat._4]]
        |  def alignment = Intrinsics.alignmentOf[Int]
        |  def pointer[T] = Intrinsics.sizeOf[Ptr[T]]
        |  def stack[T] = Intrinsics.stackalloc[Ptr[T]]()
        |}
        |""".stripMargin
    ) { methods =>
      val expected = Seq(
        "size" -> Op.SizeOf(Type.ArrayValue(Type.Byte, 4)),
        "alignment" -> Op.AlignmentOf(Type.Int),
        "pointer" -> Op.SizeOf(Type.Ptr)
      )
      expected.foreach {
        case (name, op) =>
          methods(name).assertIntrinsic(op)
      }
      val stack = methods("stack")
      assertTrue(stack.ops.exists {
        case Op.Stackalloc(Type.Ptr, _) => true
        case _                          => false
      })
      stack.assertNoCalls()
    }
  }

  @Test def abstractAndGenericLayouts(): Unit = inspect(
    """import scala.scalanative.unsafe._
      |import scala.scalanative.runtime.Intrinsics
      |object Test {
      |  type Foo
      |  implicit val fooTag: Tag[Foo] =
      |    Tag.materializeCArrayTag[Byte, Nat.Digit2[Nat._4, Nat._2]].asInstanceOf[Tag[Foo]]
      |  def size = Intrinsics.sizeOf[Foo]
      |  def alignment = Intrinsics.alignmentOf[Foo]
      |  def stack = Intrinsics.stackalloc[Foo]()
      |  def stackN(n: scala.scalanative.runtime.RawSize) = Intrinsics.stackalloc[Foo](n)
      |  def generic[T](implicit tag: Tag[T]) = sizeOf[T]
      |  def genericAlignment[T](implicit tag: Tag[T]) = alignmentOf[T]
      |  def genericStack[T](implicit tag: Tag[T]) = stackalloc[T](2)
      |  def nested = sizeOf[CStruct2[Byte, Foo]]
      |  def array = sizeOf[CArray[Foo, Nat._2]]
      |  trait Opaque
      |  implicit val opaqueTag: Tag[Opaque] = Tag.Byte.asInstanceOf[Tag[Opaque]]
      |  def traitSize = sizeOf[Opaque]
      |}
      |""".stripMargin
  ) { methods =>
    Seq("size", "alignment", "stack", "stackN").foreach { name =>
      val method = methods(name)
      val member = if (name == "alignment") "alignment" else "size"
      val elements = name match {
        case "stack"  => Some(Val.Int(1))
        case "stackN" => Some(method.params.last)
        case _        => None
      }
      method.assertTagLayout(member, method.oneCall("fooTag").value, elements)
    }
    Seq("generic", "genericAlignment", "genericStack").foreach { name =>
      val method = methods(name)
      val member = if (name == "genericAlignment") "alignment" else "size"
      val elements = if (name == "genericStack") Some(Val.Int(2)) else None
      method.assertTagLayout(member, method.params.last, elements)
      assertTrue(method.definition.show, method.calls("fooTag").isEmpty)
    }
    Seq("nested" -> "materializeCStruct2Tag", "array" -> "materializeCArrayTag")
      .foreach {
        case (name, materializer) =>
          val method = methods(name)
          val tag = method.oneCall(materializer)
          assertTrue(
            method.definition.show,
            tag.args.contains(method.oneCall("fooTag").value)
          )
          method.assertTagLayout("size", tag.value)
      }
    val traitSize = methods("traitSize")
    traitSize.assertTagLayout("size", traitSize.oneCall("opaqueTag").value)
  }

  @Test def implicitEvidenceScopes(): Unit =
    checkEvidenceScopes(useGiven = false)

  @Test def givenEvidenceScopes(): Unit = {
    assumeTrue(scalaVersion.startsWith("3."))
    checkEvidenceScopes(useGiven = true)
  }

  private def checkEvidenceScopes(useGiven: Boolean): Unit = {
    val evidence = if (useGiven) "given" else "implicit val"
    val parameter = if (useGiven) "using" else "implicit"
    val topImport = if (useGiven) "TopEvidence.given" else "TopEvidence._"
    val outerImport = if (useGiven) "OuterEvidence.given" else "OuterEvidence._"
    val localImport = if (useGiven) "LocalEvidence.given" else "LocalEvidence._"
    inspect(
      s"""import scala.scalanative.unsafe._
         |object Types {
         |  type Top
         |  type LocalImported
         |  type LocalDefined
         |  type OuterImported
         |  type OuterDefined
         |  type NestedDefined
         |}
         |object TopEvidence {
         |  $evidence topTag: Tag[Types.Top] = Tag.Byte.asInstanceOf[Tag[Types.Top]]
         |}
         |object LocalEvidence {
         |  $evidence localImportedTag: Tag[Types.LocalImported] =
         |    Tag.Int.asInstanceOf[Tag[Types.LocalImported]]
         |}
         |object OuterEvidence {
         |  $evidence outerImportedTag: Tag[Types.OuterImported] =
         |    Tag.Long.asInstanceOf[Tag[Types.OuterImported]]
         |}
         |import $topImport
         |object Test {
         |  def topLevelImport = sizeOf[Types.Top]
         |  def localImport = {
         |    import $localImport
         |    sizeOf[Types.LocalImported]
         |  }
         |  def namedLocalImport = {
         |    import LocalEvidence.localImportedTag
         |    alignmentOf[Types.LocalImported]
         |  }
         |  def localDefinition = {
         |    $evidence localTag: Tag[Types.LocalDefined] =
         |      Tag.Short.asInstanceOf[Tag[Types.LocalDefined]]
         |    sizeOf[Types.LocalDefined]
         |  }
         |  $evidence outerDefinedTag: Tag[Types.OuterDefined] =
         |    Tag.Double.asInstanceOf[Tag[Types.OuterDefined]]
         |  def outerDefinition = sizeOf[Types.OuterDefined]
         |  def methodParameter[T]($parameter tag: Tag[T]) = sizeOf[T]
         |}
         |object Nested {
         |  import $outerImport
         |  object Inner {
         |    def outerScopeImport = sizeOf[Types.OuterImported]
         |  }
         |}
         |object OuterDefinition {
         |  $evidence nestedTag: Tag[Types.NestedDefined] =
         |    Tag.Float.asInstanceOf[Tag[Types.NestedDefined]]
         |  object Inner {
         |    def outerScopeDefinition = sizeOf[Types.NestedDefined]
         |  }
         |}
         |class ClassParameter[T]($parameter val classTag: Tag[T]) {
         |  def size = sizeOf[T]
         |  def alignment = alignmentOf[T]
         |  def stack = stackalloc[T](2)
         |}
         |""".stripMargin
    ) { methods =>
      Seq(
        ("topLevelImport", "topTag", "size", "Test$"),
        ("localImport", "localImportedTag", "size", "Test$"),
        ("namedLocalImport", "localImportedTag", "alignment", "Test$"),
        ("outerDefinition", "outerDefinedTag", "size", "Test$"),
        ("outerScopeImport", "outerImportedTag", "size", "Nested$Inner$"),
        ("outerScopeDefinition", "nestedTag", "size", "OuterDefinition$Inner$")
      ).foreach {
        case (name, tag, member, owner) =>
          val method = methods(name, owner)
          method.assertTagLayout(member, method.oneCall(tag).value)
      }
      val local = methods("localDefinition")
      local.assertTagLayout(
        "size",
        if (useGiven) local.oneCall("localTag$1").value
        else local.module("scala.scalanative.unsafe.Tag$Short$")
      )
      val methodParameter = methods("methodParameter")
      methodParameter.assertTagLayout("size", methodParameter.params.last)
      Seq("size", "alignment", "stack").foreach { name =>
        val method = methods(name, "ClassParameter")
        method.assertTagLayout(
          if (name == "alignment") "alignment" else "size",
          method.oneCall("classTag").value,
          if (name == "stack") Some(Val.Int(2)) else None
        )
      }
    }
  }

  @Test def abstractUpperBoundRequiresEvidence(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () =>
        NIRCompiler(
          _.compile(
            """import scala.scalanative.unsafe._
              |object Test {
              |  type Foo <: CArray[Byte, Nat._4]
              |  def size = sizeOf[Foo]
              |}
              |""".stripMargin
          )
        )
    )
    assertTrue(err.getMessage(), err.getMessage().contains("implicit"))
  }

  @Test def missingEvidenceFails(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () =>
        NIRCompiler(
          _.compile(
            """import scala.scalanative.unsafe._
              |object Test {
              |  type Foo
              |  def size = sizeOf[Foo]
              |}
              |""".stripMargin
          )
        )
    )
    assertTrue(
      err.getMessage(),
      err.getMessage().contains("Cannot determine native layout")
    )
    assertTrue(err.getMessage(), err.getMessage().contains("Tag"))
  }

  @Test def ambiguousEvidenceFails(): Unit = {
    val err = assertThrows(
      classOf[CompilationFailedException],
      () =>
        NIRCompiler(
          _.compile(
            """import scala.scalanative.unsafe._
              |object Test {
              |  type Foo
              |  implicit val first: Tag[Foo] = Tag.Byte.asInstanceOf[Tag[Foo]]
              |  implicit val second: Tag[Foo] = first
              |  def size = sizeOf[Foo]
              |}
              |""".stripMargin
          )
        )
    )
    assertTrue(err.getMessage(), err.getMessage().contains("Tag"))
  }

  @Test def byteCountIsUsedForStackAllocation(): Unit = {
    inspect(
      """import scala.scalanative.runtime.{Intrinsics, RawSize}
        |import scala.scalanative.unsafe._
        |object Test {
        |  def stack[T](n: RawSize)(implicit tag: Tag[T]) = Intrinsics.stackalloc[T](n)
        |}
        |""".stripMargin
    ) { methods =>
      val stack = methods("stack")
      stack.assertTagLayout("size", stack.params.last, Some(stack.params(1)))
    }
  }

  @Test def genericArrayLengthAndPathDependentEvidence(): Unit = inspect(
    """import scala.scalanative.unsafe._
      |trait Types {
      |  type Foo
      |  implicit def tag: Tag[Foo]
      |}
      |object Test {
      |  def size[N <: Nat](implicit tag: Tag[N]) = sizeOf[CArray[Byte, N]]
      |  def alignment[N <: Nat](implicit tag: Tag[N]) = alignmentOf[CArray[Int, N]]
      |  def stack[T, N <: Nat](implicit t: Tag[T], n: Tag[N]) =
      |    stackalloc[CArray[T, N]](2)
      |  def pathDependent(types: Types) = {
      |    import types.tag
      |    sizeOf[CStruct2[Int, types.Foo]]
      |  }
      |}
      |""".stripMargin
  ) { methods =>
    Seq("size", "alignment", "stack").foreach { name =>
      val method = methods(name)
      val tag = method.oneCall("materializeCArrayTag")
      assertEquals(method.definition.show, method.params.last, tag.args.last)
      if (name == "stack")
        assertEquals(method.definition.show, method.params(1), tag.args(1))
      method.assertTagLayout(
        if (name == "alignment") "alignment" else "size",
        tag.value,
        if (name == "stack") Some(Val.Int(2)) else None
      )
    }
    val path = methods("pathDependent")
    val evidence = path.oneCall("tag")
    assertEquals(path.definition.show, path.params.last, evidence.args.head)
    val tag = path.oneCall("materializeCStruct2Tag")
    assertEquals(path.definition.show, evidence.value, tag.args.last)
    path.assertTagLayout("size", tag.value)
  }

  @Test def inheritedLayoutEvidence(): Unit = inspect(
    """import scala.scalanative.unsafe._
      |trait Evidence[T] {
      |  implicit def tag: Tag[T]
      |}
      |trait Layout[T] extends Evidence[T] {
      |  def size = sizeOf[T]
      |  def alignment = alignmentOf[T]
      |  def stack = stackalloc[T](2)
      |}
      |object Types {
      |  type Foo
      |  object Foo extends Layout[Foo] {
      |    implicit val tag: Tag[Foo] = Tag.Int.asInstanceOf[Tag[Foo]]
      |  }
      |}
      |object Test {
      |  import Types.Foo.tag
      |  def size = sizeOf[Types.Foo]
      |  def alignment = alignmentOf[Types.Foo]
      |  def stack = stackalloc[Types.Foo](2)
      |  def array = sizeOf[CArray[Types.Foo, Nat._2]]
      |}
      |""".stripMargin
  ) { methods =>
    Seq("size", "alignment", "stack").foreach { name =>
      Seq(methods(name, "Layout"), methods(name)).foreach { method =>
        method.assertTagLayout(
          if (name == "alignment") "alignment" else "size",
          method.oneCall("tag").value,
          if (name == "stack") Some(Val.Int(2)) else None
        )
      }
    }
    val array = methods("array")
    val tag = array.oneCall("materializeCArrayTag")
    assertEquals(array.definition.show, array.oneCall("tag").value, tag.args(1))
    array.assertTagLayout("size", tag.value)
  }

  @Test def bindgenEnumsKeepIntrinsicLayouts(): Unit = {
    assumeTrue(scalaVersion.startsWith("3."))
    inspect(
      """import scala.scalanative.unsafe.*
        |import scala.scalanative.unsigned.*
        |import scala.scalanative.runtime.Intrinsics
        |trait CEnum[T](using T =:= Int) {
        |  given Tag[T] = Tag.Int.asInstanceOf[Tag[T]]
        |  def genericSize = sizeOf[T]
        |  def genericAlignment = alignmentOf[T]
        |  def genericStack = stackalloc[T](2)
        |}
        |object Types {
        |  opaque type Signed = Int
        |  object Signed extends CEnum[Signed] {
        |    given _tag: Tag[Signed] = Tag.Int
        |  }
        |  opaque type Unsigned = UInt
        |  object Unsigned {
        |    given Tag[Unsigned] = Tag.UInt
        |  }
        |}
        |object Test {
        |  def size = Intrinsics.sizeOf[Types.Signed]
        |  def alignment = Intrinsics.alignmentOf[Types.Unsigned]
        |  def array = Intrinsics.sizeOf[CArray[Types.Signed, Nat._2]]
        |  def struct = Intrinsics.sizeOf[CStruct2[Byte, Types.Unsigned]]
        |  def stack = Intrinsics.stackalloc[Types.Signed]()
        |}
        |""".stripMargin
    ) { methods =>
      val expected = Seq(
        "size" -> Op.SizeOf(Type.Int),
        "alignment" -> Op.AlignmentOf(Type.Int),
        "array" -> Op.SizeOf(Type.ArrayValue(Type.Int, 2)),
        "struct" -> Op.SizeOf(Type.StructValue(Seq(Type.Byte, Type.Int)))
      )
      expected.foreach {
        case (name, op) =>
          methods(name).assertIntrinsic(op)
      }
      val stack = methods("stack")
      assertTrue(stack.ops.exists {
        case Op.Stackalloc(Type.Int, _) => true
        case _                          => false
      })
      stack.assertNoCalls()
      Seq("genericSize", "genericAlignment", "genericStack").foreach { name =>
        val method = methods(name, "CEnum")
        method.assertTagLayout(
          if (name == "genericAlignment") "alignment" else "size",
          method.oneCall("given_Tag_T").value,
          if (name == "genericStack") Some(Val.Int(2)) else None
        )
      }
    }
  }

  @Test def opaqueLayoutAndInlineEvidence(): Unit = {
    assumeTrue(scalaVersion.startsWith("3."))
    inspect(
      """import scala.scalanative.unsafe.*
        |object Types {
        |  opaque type Foo = CArray[Byte, Nat.Digit2[Nat._4, Nat._2]]
        |}
        |object Test {
        |  def opaqueSize = sizeOf[Types.Foo]
        |  def opaqueStack = stackalloc[Types.Foo]()
        |  inline def size[T](using Tag[T]) = sizeOf[T]
        |  type Bar
        |  given Tag[Bar] = Tag.Byte.asInstanceOf[Tag[Bar]]
        |  def abstractSize = size[Bar]
        |  type Baz
        |  inline given Tag[Baz] = Tag.Byte.asInstanceOf[Tag[Baz]]
        |  def inlineTagSize = sizeOf[Baz]
        |  def inlineNestedTagSize = sizeOf[CArray[Baz, Nat._2]]
        |}
        |""".stripMargin
    ) { methods =>
      methods("opaqueSize").assertIntrinsic(
        Op.SizeOf(Type.ArrayValue(Type.Byte, 42))
      )
      val opaqueStack = methods("opaqueStack")
      assertTrue(
        opaqueStack.definition.show,
        opaqueStack.ops.exists {
          case Op.Stackalloc(Type.ArrayValue(Type.Byte, 42), _) => true
          case _                                                => false
        }
      )
      Seq("abstractSize" -> "given_Tag_Bar", "inlineTagSize" -> "inline")
        .foreach {
          case (name, evidence) =>
            val method = methods(name)
            val tag =
              if (evidence == "inline")
                method.module("scala.scalanative.unsafe.Tag$Byte$")
              else method.oneCall(evidence).value
            method.assertTagLayout("size", tag)
        }
      val nested = methods("inlineNestedTagSize")
      val tag = nested.oneCall("materializeCArrayTag")
      nested.assertTagLayout("size", tag.value)
      nested.assertCallArgument(
        "materializeCArrayTag",
        1,
        nested.module("scala.scalanative.unsafe.Tag$Byte$")
      )
    }
  }
}
