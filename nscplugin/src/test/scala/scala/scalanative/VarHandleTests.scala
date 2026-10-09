package scala.scalanative

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.api.CompilationFailedException
import scala.scalanative.linker.compileAndLoad

/** Compiler-plugin contract for compile-time-only VarHandle lookup. */
class VarHandleTests {
  @Test def resolvesBackingFieldFromPrecompiledScalaClass(): Unit = {
    org.junit.Assume.assumeTrue(
      "Scala 3 TASTy field fixture",
      scala.util.Properties.versionNumberString.startsWith("3.")
    )
    val source =
      """
        |import java.lang.invoke.MethodHandles
        |import scala.scalanative.varhandlefixtures.ScalaFields
        |class PrecompiledFieldLookup {
        |  val handle = MethodHandles.privateLookupIn(classOf[ScalaFields], MethodHandles.lookup())
        |    .findVarHandle(classOf[ScalaFields], "value", classOf[Int])
        |  val privateHandle = MethodHandles.privateLookupIn(classOf[ScalaFields], MethodHandles.lookup())
        |    .findVarHandle(classOf[ScalaFields], "privateValue", classOf[Long])
        |}
        |""".stripMargin
    val fixtures = new java.io.File(
      classOf[
        varhandlefixtures.ScalaFields
      ].getProtectionDomain.getCodeSource.getLocation.toURI
    )
    val classpath = sys.props("scalanative.nativeruntime.cp") +
      java.io.File.pathSeparator + fixtures.getAbsolutePath
    NIRCompiler { compiler =>
      val files = compiler.compile(source, Array("-classpath", classpath))
      scala.scalanative.util.Scope { implicit scope =>
        val fields = files.toSeq
          .filter(_.toString.endsWith(".nir"))
          .flatMap { file =>
            val directory =
              scala.scalanative.io.VirtualDirectory.real(file.getParent)
            nir.serialization.deserializeBinary(directory, file.getFileName)
          }
          .collect { case method: nir.Defn.Define => method }
          .flatMap(_.insts.collect {
            case nir.Inst.Let(_, nir.Op.Field(_, field), _) => field
          })
        val owner =
          nir.Global.Top("scala.scalanative.varhandlefixtures.ScalaFields")
        for (name <- List("value", "privateValue"))
          assertTrue(
            fields.mkString("\n"),
            fields.contains(
              owner.member(nir.Sig.Field(name, nir.Sig.Scope.Public))
            )
          )
      }
    }
  }

  @Test def runtimeViewsRemainVirtualCallsAndAccessesValidateDeclaredTypes()
      : Unit = {
    val source =
      """
        |import java.lang.invoke.VarHandle
        |class Coordinates
        |class RuntimeViews {
        |  def exact(h: VarHandle): VarHandle = h.withInvokeExactBehavior()
        |  def adapting(h: VarHandle): VarHandle = h.withInvokeBehavior()
        |  def get(h: VarHandle, box: Coordinates): Long = h.get(box)
        |  def add(h: VarHandle, box: Coordinates, value: Int): Int = h.getAndAdd(box, value)
        |}
        |""".stripMargin
    compileAndLoad("RuntimeViews.scala" -> source) { defns =>
      val methods = defns.collect {
        case d: nir.Defn.Define if d.name.top.id == "RuntimeViews" => d
      }
      def body(name: String): nir.Defn.Define = methods
        .find(_.name.sig.unmangled match {
          case nir.Sig.Method(n, _, _) => n == name
          case _                       => false
        })
        .get
      def calls(method: nir.Defn.Define): Seq[String] = method.insts
        .collect {
          case nir.Inst.Let(_, nir.Op.Method(_, sig), _) => sig.unmangled
        }
        .collect { case nir.Sig.Method(name, _, _) => name }
      assertTrue(calls(body("exact")).contains("withInvokeExactBehavior"))
      assertTrue(calls(body("adapting")).contains("withInvokeBehavior"))
      for (name <- List("get", "add")) {
        assertTrue(name, calls(body(name)).contains("validateInvocation"))
        assertFalse(
          name,
          body(name).insts.exists {
            case nir.Inst.Let(_, _: nir.Op.Arrayalloc, _) => true
            case _                                        => false
          }
        )
      }
      val getTokens = body("get").insts
        .collect {
          case nir.Inst.Let(_, nir.Op.Call(_, _, args), _) => args
        }
        .flatten
        .collect { case nir.Val.ClassOf(name) => name }
      assertTrue(getTokens.contains(nir.Global.Top("Coordinates")))
      assertTrue(
        getTokens.contains(
          nir.Global.Top("scala.scalanative.runtime.PrimitiveLong")
        )
      )
    }
  }

  @Test def preservesAccessNamesOnUnrelatedTypes(): Unit = {
    val source =
      """
        |class Ordinary {
        |  def getAndAdd(target: Object, value: Int): Int = value
        |  def getAcquire(target: Object): Int = 42
        |}
        |class Caller {
        |  def access(handle: Ordinary, target: Object): Int =
        |    handle.getAndAdd(target, 2) + handle.getAcquire(target)
        |}
        |""".stripMargin
    compileAndLoad("Ordinary.scala" -> source) { defns =>
      val calls = defns
        .collect {
          case d: nir.Defn.Define if d.name.top.id == "Caller" => d
        }
        .flatMap(_.insts.collect {
          case nir.Inst.Let(_, nir.Op.Method(_, signature), _) =>
            signature.unmangled
        })
      assertTrue(calls.exists {
        case nir.Sig.Method("getAndAdd", _, _) => true
        case _                                 => false
      })
      assertTrue(calls.exists {
        case nir.Sig.Method("getAcquire", _, _) => true
        case _                                  => false
      })
    }
  }

  @Test def fieldLookupsUseDedicatedFactories(): Unit = {
    val types = List(
      ("Boolean", "Boolean", "false"),
      ("Byte", "Byte", "0.toByte"),
      ("Short", "Short", "0.toShort"),
      ("Char", "Char", "0.toChar"),
      ("Int", "Int", "0"),
      ("Long", "Long", "0L"),
      ("Float", "Float", "0.0f"),
      ("Double", "Double", "0.0"),
      ("Reference", "String", "null")
    )
    val static = scala.util.Properties.versionNumberString.startsWith("3.")
    val source = types
      .map {
        case (kind, tpe, initial) =>
          val instance =
            s"""class Factory$kind {
               |  var value: $tpe = $initial
               |  def handle = java.lang.invoke.MethodHandles.lookup().findVarHandle(classOf[Factory$kind], "value", classOf[$tpe])
               |}""".stripMargin
          val staticSource =
            if (!static) ""
            else
              s"""class StaticFactory$kind
                 |object StaticFactory$kind {
                 |  @scala.annotation.static var value: $tpe = $initial
                 |  def handle = java.lang.invoke.MethodHandles.lookup().findStaticVarHandle(classOf[StaticFactory$kind], "value", classOf[$tpe])
                 |}""".stripMargin
          instance + "\n" + staticSource
      }
      .mkString("\n")
    compileAndLoad("Factories.scala" -> source) { defns =>
      for {
        (kind, _, _) <- types
        owner <- List(
          "Factory" + kind
        ) ++ (if (static) List("StaticFactory" + kind + "$") else Nil)
      } {
        val method = defns.collectFirst {
          case d: nir.Defn.Define
              if d.name.top.id == owner && (d.name.sig.unmangled match {
                case nir.Sig.Method("handle", _, _) => true
                case _                              => false
              }) =>
            d
        }.get
        val calls = method.insts
          .collect {
            case nir.Inst.Let(
                  _,
                  nir.Op.Call(
                    _,
                    nir.Val.Global(global: nir.Global.Member, _),
                    args
                  ),
                  _
                ) =>
              (global.sig.unmangled, args)
          }
          .collect {
            case (nir.Sig.Method(name, parameters, _), args)
                if name.startsWith("create") =>
              (name, parameters, args)
          }
        assertEquals(owner + " must call exactly one factory", 1, calls.size)
        val (name, parameters, args) = calls.head
        assertEquals(owner, "create" + kind + "Handle", name)
        assertEquals(owner, if (kind == "Reference") 4 else 3, parameters.size)
        assertFalse(
          owner + " must not pass a field-kind tag",
          args.exists(_.isInstanceOf[nir.Val.Int])
        )
        if (kind == "Reference")
          assertEquals(
            owner,
            nir.Val.ClassOf(nir.Global.Top("java.lang.String")),
            args.last
          )
        else
          assertEquals(
            owner + " must pass only the coordinate class token",
            if (owner.startsWith("StaticFactory")) 0 else 1,
            args.count(_.isInstanceOf[nir.Val.ClassOf])
          )
      }
    }
  }

  @Test def adaptedWitnessesUseWrapperInstanceMethods(): Unit = {
    val wrappers = List(
      "Boolean" -> "Boolean",
      "Byte" -> "Byte",
      "Short" -> "Short",
      "Char" -> "Character",
      "Int" -> "Integer",
      "Long" -> "Long",
      "Float" -> "Float",
      "Double" -> "Double"
    )
    val bodies = wrappers.map {
      case (primitive, wrapper) =>
        s"def result$primitive(handle: java.lang.invoke.VarHandle, receiver: Object, value: java.lang.$wrapper): $primitive = handle.getAndSet(receiver, value)"
    }
    compileAndLoad(
      "Unboxing.scala" -> ("class Unboxing {\n" + bodies.mkString("\n") + "\n}")
    ) { defns =>
      wrappers.foreach {
        case (primitive, wrapper) =>
          val method = defns.collectFirst {
            case d: nir.Defn.Define
                if d.name.top.id == "Unboxing" && (d.name.sig.unmangled match {
                  case nir.Sig.Method(name, _, _) =>
                    name == "result" + primitive
                  case _ => false
                }) =>
              d
          }.get
          val calls = method.insts
            .collect {
              case nir.Inst.Let(_, nir.Op.Method(_, sig), _) => sig.unmangled
              case nir.Inst.Let(
                    _,
                    nir.Op.Call(
                      _,
                      nir.Val.Global(global: nir.Global.Member, _),
                      _
                    ),
                    _
                  ) =>
                global.sig.unmangled
            }
            .collect { case nir.Sig.Method(name, _, _) => name }
          assertTrue(
            primitive + " must call its wrapper's value method: " + calls,
            calls.contains(
              primitive.toLowerCase(java.util.Locale.ROOT) + "Value"
            )
          )
          assertFalse(
            wrapper + " must not use Scala's null-defaulting unboxing",
            calls.exists(_.startsWith("unboxTo"))
          )
      }
    }
  }

  @Test def protocolAliasesEmitSwitchInstructions(): Unit = {
    val groups = List(
      "AccessOperation" -> List(
        "Get",
        "Set",
        "Compare",
        "WeakCompare",
        "CompareExchange",
        "Exchange",
        "Add",
        "Bitwise"
      ),
      "MemoryOrder" -> List("Plain", "Volatile", "Acquire", "Release"),
      "BitwiseOperation" -> List("Or", "And", "Xor")
    )
    val methods = groups.map {
      case (group, entries) =>
        val cases = entries
          .map(entry =>
            s"case NativeVarHandle.$group.$entry => NativeVarHandle.$group.$entry"
          )
          .mkString("\n")
        s"def $group(value: NativeVarHandle.$group): Int = (value: @scala.annotation.switch) match {\n$cases\ncase _ => -1\n}"
    }
    compileAndLoad(
      "ProtocolSwitches.scala" -> ("package scala.scalanative.runtime\nclass VarHandleProtocolSwitches {\n" + methods
        .mkString("\n") + "\n}")
    ) { defns =>
      groups.foreach {
        case (group, entries) =>
          val method = defns.collectFirst {
            case d: nir.Defn.Define
                if d.name.top.id == "scala.scalanative.runtime.VarHandleProtocolSwitches" && (d.name.sig.unmangled match {
                  case nir.Sig.Method(name, _, _) => name == group
                  case _                          => false
                }) =>
              d
          }.get
          val switches = method.insts.collect {
            case switch: nir.Inst.Switch => switch
          }
          assertEquals(group, 1, switches.length)
          assertEquals(group, entries.size, switches.head.cases.size)
      }
    }
  }

  @Test def adaptsEveryOperandModeWithoutVarargs(): Unit = {
    val wrappers = List(
      "Boolean" -> "Boolean",
      "Byte" -> "Byte",
      "Short" -> "Short",
      "Char" -> "Character",
      "Int" -> "Integer",
      "Long" -> "Long",
      "Float" -> "Float",
      "Double" -> "Double"
    )
    val widening = List(
      "Byte" -> List("Short", "Int", "Long", "Float", "Double"),
      "Short" -> List("Int", "Long", "Float", "Double"),
      "Char" -> List("Int", "Long", "Float", "Double"),
      "Int" -> List("Long", "Float", "Double"),
      "Long" -> List("Float", "Double"),
      "Float" -> List("Double")
    )
    val stores = List("set", "setOpaque", "setRelease", "setVolatile")
    val compares = List(
      "compareAndSet",
      "weakCompareAndSet",
      "weakCompareAndSetPlain",
      "weakCompareAndSetAcquire",
      "weakCompareAndSetRelease"
    )
    val rmw = List(
      "compareAndExchange",
      "getAndSet",
      "getAndAdd",
      "getAndBitwiseOr",
      "getAndBitwiseAnd",
      "getAndBitwiseXor"
    ).flatMap(p => List("", "Acquire", "Release").map(p + _))
    val pairs = wrappers.map {
      case (primitive, wrapper) => ("java.lang." + wrapper, primitive)
    } ++ widening.flatMap {
      case (source, targets) => targets.map(target => (source, target))
    }
    val bodies = for {
      (operand, target) <- pairs
      method <- stores ++ compares ++ rmw
      instance <- List(true, false)
    } yield {
      val result =
        if (stores.contains(method)) "Unit"
        else if (compares.contains(method)) "Boolean"
        else target
      val values =
        if (compares.contains(method) || method
              .startsWith("compareAndExchange")) List("expected", "desired")
        else List("desired")
      val arguments = (if (instance) List("receiver") else Nil) ++ values
      val parameters =
        List("handle: VarHandle") ++ (if (instance) List("receiver: AnyRef")
                                      else
                                        Nil) ++ values.map(v => s"$v: $operand")
      s"def ${method}_${operand.replace('.', '_')}_${target}_$instance(${parameters.mkString(", ")}): $result = handle.$method(${arguments.mkString(", ")})"
    }
    compileAndLoad(
      "OperandAdaptation.scala" -> ("import java.lang.invoke.VarHandle\nclass OperandAdaptation {\n" + bodies
        .mkString("\n") + "\n}")
    ) { defns =>
      val methods = defns.collect {
        case d: nir.Defn.Define
            if d.name.top.id == "OperandAdaptation" && (d.name.sig.unmangled match {
              case nir.Sig.Method(name, _, _) => name != "<init>";
              case _                          => false
            }) =>
          d
      }
      assertEquals(bodies.size, methods.size)
      methods.foreach { method =>
        val ops = method.insts.collect { case nir.Inst.Let(_, op, _) => op }
        assertTrue(
          method.name.show,
          ops.exists {
            case nir.Op.Method(_, sig) =>
              sig.unmangled match {
                case nir.Sig.Method("invokeAdapted", _, _) => true;
                case _                                     => false
              };
            case _ => false
          }
        )
        assertFalse(
          method.name.show,
          ops.exists { case _: nir.Op.Arrayalloc => true; case _ => false }
        )
      }
    }
  }

  @Test def boxedFieldLookupsUseReferenceHandles(): Unit = {
    val types = List(
      ("Boolean", "Boolean", "true"),
      ("Byte", "Byte", "1.toByte"),
      ("Short", "Short", "1.toShort"),
      ("Char", "Character", "1.toChar"),
      ("Int", "Integer", "1"),
      ("Long", "Long", "1L"),
      ("Float", "Float", "1.0f"),
      ("Double", "Double", "1.0d")
    )
    val source = types
      .map {
        case (tpe, wrapper, value) =>
          s"""class Box$tpe {
             |  private var value: java.lang.$wrapper = java.lang.$wrapper.valueOf($value)
             |  def current: java.lang.$wrapper = value
             |  def handle = java.lang.invoke.MethodHandles.lookup().findVarHandle(classOf[Box$tpe], "value", classOf[java.lang.$wrapper])
             |}""".stripMargin
      }
      .mkString("\n")
    compileAndLoad("BoxedFields.scala" -> source) { definitions =>
      types.foreach {
        case (tpe, _, _) =>
          val method = definitions.collectFirst {
            case d: nir.Defn.Define
                if d.name.top.id == "Box" + tpe &&
                  (d.name.sig.unmangled match {
                    case nir.Sig.Method("handle", _, _) => true; case _ => false
                  }) =>
              d
          }.get
          assertTrue(
            tpe + " fields must use the reference factory",
            method.insts.exists {
              case nir.Inst.Let(
                    _,
                    nir.Op
                      .Call(_, nir.Val.Global(global: nir.Global.Member, _), _),
                    _
                  ) =>
                global.sig.unmangled match {
                  case nir.Sig.Method("createReferenceHandle", _, _) => true
                  case _                                             => false
                }
              case _ => false
            }
          )
      }
    }
  }
  @Test def boxesEveryPrimitiveRmwResultWithoutVarargs(): Unit = {
    val types =
      List("Boolean", "Byte", "Short", "Char", "Int", "Long", "Float", "Double")
    val modes = for {
      prefix <- List(
        "compareAndExchange",
        "getAndSet",
        "getAndAdd",
        "getAndBitwiseOr",
        "getAndBitwiseAnd",
        "getAndBitwiseXor"
      )
      suffix <- List("", "Acquire", "Release")
    } yield prefix + suffix
    val cases = for {
      tpe <- types
      mode <- modes
      static <- List(false, true)
    } yield {
      val name = tpe + mode + (if (static) "Static" else "Instance")
      val coordinates = if (static) Nil else List("receiver")
      val values =
        if (mode.startsWith("compareAndExchange")) List("value", "value")
        else List("value")
      (
        name,
        "boxTo" + (tpe match {
          case "Char" => "Character"
          case "Int"  => "Integer"
          case other  => other
        }),
        s"def $name(handle: java.lang.invoke.VarHandle, receiver: Object, value: $tpe): AnyRef = handle.$mode(${(coordinates ++ values).mkString(", ")})"
      )
    }
    compileAndLoad(
      "Boxing.scala" -> ("class Boxing {\n" + cases
        .map(_._3)
        .mkString("\n") + "\n}")
    ) { definitions =>
      val methods = definitions.collect {
        case d: nir.Defn.Define if d.name.top.id == "Boxing" => d
      }
      cases.foreach {
        case (name, conversion, _) =>
          val method = methods
            .find(_.name.sig.unmangled match {
              case nir.Sig.Method(n, _, _) => n == name
              case _                       => false
            })
            .get
          val operations = method.insts.collect {
            case nir.Inst.Let(_, op, _) => op
          }
          val calls = operations
            .collect {
              case nir.Op.Method(_, sig) => sig.unmangled
              case nir.Op.Call(
                    _,
                    nir.Val.Global(global: nir.Global.Member, _),
                    _
                  ) =>
                global.sig.unmangled
            }
            .collect { case nir.Sig.Method(n, _, _) => n }
          assertTrue(name + ": " + calls, calls.contains(conversion))
          assertFalse(name, calls.contains("unsupportedSignature"))
          assertFalse(
            name,
            operations.exists {
              case _: nir.Op.Arrayalloc => true; case _ => false
            }
          )
      }
    }
  }
  @Test def widensEveryPrimitiveRmwResultWithoutBoxing(): Unit = {
    val widening = List(
      "Byte" -> List("Short", "Int", "Long", "Float", "Double"),
      "Short" -> List("Int", "Long", "Float", "Double"),
      "Char" -> List("Int", "Long", "Float", "Double"),
      "Int" -> List("Long", "Float", "Double"),
      "Long" -> List("Float", "Double"),
      "Float" -> List("Double")
    )
    val methods = for {
      prefix <- List(
        "compareAndExchange",
        "getAndSet",
        "getAndAdd",
        "getAndBitwiseOr",
        "getAndBitwiseAnd",
        "getAndBitwiseXor"
      )
      suffix <- List("", "Acquire", "Release")
    } yield prefix + suffix
    val cases = for {
      (source, targets) <- widening
      target <- targets
      method <- methods
      static <- List(false, true)
    } yield {
      val name =
        source + "To" + target + method + (if (static) "Static" else "Instance")
      val coordinate = if (static) Nil else List("receiver")
      val values =
        if (method.startsWith("compareAndExchange")) List("value", "value")
        else List("value")
      val code =
        s"def $name(handle: java.lang.invoke.VarHandle, receiver: Object, value: $source): $target = handle.$method(${(coordinate ++ values).mkString(", ")})"
      (name, "to" + target, code)
    }
    compileAndLoad(
      "Widening.scala" ->
        ("class Widening {\n" + cases.map(_._3).mkString("\n") + "\n}")
    ) { definitions =>
      val methods = definitions.collect {
        case d: nir.Defn.Define if d.name.top.id == "Widening" => d
      }
      cases.foreach {
        case (name, conversion, _) =>
          val method = methods
            .find(_.name.sig.unmangled match {
              case nir.Sig.Method(n, _, _) => n == name
              case _                       => false
            })
            .getOrElse(throw new AssertionError("missing " + name))
          val operations = method.insts.collect {
            case nir.Inst.Let(_, op, _) => op
          }
          val calls = operations
            .collect {
              case nir.Op.Method(_, sig) => sig.unmangled
              case nir.Op.Call(
                    _,
                    nir.Val.Global(global: nir.Global.Member, _),
                    _
                  ) =>
                global.sig.unmangled
            }
            .collect { case nir.Sig.Method(n, _, _) => n }
          assertTrue(
            name + " must lower " + conversion + " to a primitive conversion",
            operations.exists { case _: nir.Op.Conv => true; case _ => false }
          )
          assertFalse(name, calls.contains("unsupportedSignature"))
          assertFalse(
            name,
            operations.exists {
              case _: nir.Op.Box | _: nir.Op.Unbox | _: nir.Op.Arrayalloc =>
                true
              case _ => false
            }
          )
      }
    }
  }

  @Test def ignoresNonVarHandleHigherOrderCalls(): Unit =
    compileAndLoad(
      "HigherOrderCalls.scala" ->
        """object HigherOrderCalls {
          |  def choose(flag: Boolean): Int => Int =
          |    if (flag) (x: Int) => x + 1 else (x: Int) => x - 1
          |  def apply(flag: Boolean): Int = {
          |    choose(flag)(1)
          |    ((x: Int) => x + 2)(3)
          |    List(1, 2).map(choose(flag)).sum
          |  }
          |}""".stripMargin
    ) { definitions => assertTrue(definitions.nonEmpty) }

  @Test def discardedRmwUsesExactAccessors(): Unit = {
    val prefixes = List(
      "compareAndExchange",
      "getAndSet",
      "getAndAdd",
      "getAndBitwiseOr",
      "getAndBitwiseAnd",
      "getAndBitwiseXor"
    )
    val methods =
      prefixes.flatMap(prefix => List("", "Acquire", "Release").map(prefix + _))
    val types = List(
      "Boolean",
      "Byte",
      "Short",
      "Char",
      "Int",
      "Long",
      "Float",
      "Double",
      "AnyRef"
    )
    val bodies = for {
      tpe <- types
      method <- methods
      static <- List(false, true)
      statement <- List(false, true)
    } yield {
      val coordinate = if (static) Nil else List("receiver")
      val values =
        if (method.startsWith("compareAndExchange")) List("expected", "desired")
        else List("desired")
      val args = (coordinate ::: values).mkString(", ")
      val parameters =
        (if (static) Nil else List("receiver: Object")) ::: values.map(
          _ + ": " + tpe
        )
      val name = s"${method}_${tpe}_${static}_${statement}"
      val call = s"handle.$method($args)"
      s"def $name(handle: VarHandle, ${parameters.mkString(", ")}): Unit = " +
        (if (statement) s"{ $call; () }" else call)
    }
    compileAndLoad(
      "DiscardedAccess.scala" ->
        ("import java.lang.invoke.VarHandle\nclass DiscardedAccess {\n" + bodies
          .mkString("\n") + "\n}")
    ) { defns =>
      val definitions = defns.collect {
        case d: nir.Defn.Define
            if d.name.top.id == "DiscardedAccess" &&
              d.name.sig.unmangled.isInstanceOf[nir.Sig.Method] =>
          d
      }
      assertEquals(bodies.size, definitions.size)
      definitions.foreach { d =>
        val nir.Sig.Method(name, _, _) = d.name.sig.unmangled: @unchecked
        val parts = name.split("_")
        val operation =
          if (parts(0).startsWith("compareAndExchange")) "compareExchange"
          else if (parts(0).startsWith("getAndSet")) "exchange"
          else if (parts(0).startsWith("getAndAdd")) "add"
          else "bitwise"
        val accessor =
          operation + (if (parts(1) == "AnyRef") "Reference" else parts(1))
        val ops = d.insts.collect { case nir.Inst.Let(_, op, _) => op }
        val calledMethods =
          ops.collect { case nir.Op.Method(_, sig) => sig.unmangled }.collect {
            case nir.Sig.Method(method, _, _) => method
          }
        assertTrue(
          d.name.show + " must invoke " + accessor,
          calledMethods.contains(accessor)
        )
        assertFalse(
          d.name.show + " must not reject a discarded result",
          calledMethods.contains("unsupportedSignature")
        )
        assertFalse(
          d.name.show + " must not box or allocate varargs",
          ops.exists {
            case _: nir.Op.Box | _: nir.Op.Unbox | _: nir.Op.Arrayalloc => true
            case _                                                      => false
          }
        )
      }
    }
  }

  @Test def discardedResultsDoNotDiscardBoundOrOperandBoxing(): Unit =
    compileAndLoad(
      "DiscardedContexts.scala" ->
        """import java.lang.invoke.VarHandle
          |class DiscardedContexts {
          |  def control(handle: VarHandle, receiver: Object, condition: Boolean): Unit = {
          |    if (condition) handle.getAndAdd(receiver, 1) else handle.getAndAdd(receiver, 2)
          |    condition match {
          |      case true => handle.getAndAdd(receiver, 3)
          |      case false => handle.getAndAdd(receiver, 4)
          |    }
          |    try handle.getAndAdd(receiver, 5)
          |    catch { case _: Exception => handle.getAndAdd(receiver, 6) }
          |    finally handle.getAndAdd(receiver, 7)
          |    ()
          |  }
          |  def bound(handle: VarHandle, receiver: Object): Unit = {
          |    val boxed: AnyRef = handle.getAndAdd(receiver, 1)
          |    ()
          |  }
          |  def operand(handle: VarHandle, receiver: Object): Unit =
          |    handle.getAndAdd(receiver, {
          |      val boxed: AnyRef = handle.getAndAdd(receiver, 1)
          |      boxed.asInstanceOf[Int]
          |    })
          |}
          |""".stripMargin
    ) { defns =>
      defns
        .collect {
          case d: nir.Defn.Define if d.name.top.id == "DiscardedContexts" => d
        }
        .foreach { d =>
          d.name.sig.unmangled match {
            case nir.Sig.Method(name, _, _) =>
              val calls = d.insts
                .collect {
                  case nir.Inst.Let(_, nir.Op.Method(_, sig), _) =>
                    sig.unmangled
                }
                .collect { case nir.Sig.Method(method, _, _) => method }
              if (name == "control") {
                assertTrue(
                  "every branch must retain its atomic operation: " + calls
                    .mkString(", "),
                  calls.count(_ == "addInt") >= 7
                )
                assertFalse(calls.contains("unsupportedSignature"))
              } else {
                assertTrue(name, calls.contains("addInt"))
                assertFalse(name, calls.contains("unsupportedSignature"))
              }
            case _ => ()
          }
        }
    }

  @Test def lowersEveryExactOperationAndVariableType(): Unit =
    loweringMatrix(instance = true)

  @Test def lowersEveryStaticExactOperationAndVariableType(): Unit =
    loweringMatrix(instance = false)

  private def loweringMatrix(instance: Boolean): Unit = {
    val types = List(
      "Boolean",
      "Byte",
      "Short",
      "Char",
      "Int",
      "Long",
      "Float",
      "Double",
      "AnyRef"
    )
    val reads = List("get", "getOpaque", "getAcquire", "getVolatile")
    val writes = List("set", "setOpaque", "setRelease", "setVolatile")
    val compares = List(
      "compareAndSet",
      "weakCompareAndSetPlain",
      "weakCompareAndSet",
      "weakCompareAndSetAcquire",
      "weakCompareAndSetRelease"
    )
    val compareExchanges = List(
      "compareAndExchange",
      "compareAndExchangeAcquire",
      "compareAndExchangeRelease"
    )
    val rmw = List(
      "getAndSet",
      "getAndAdd",
      "getAndBitwiseOr",
      "getAndBitwiseAnd",
      "getAndBitwiseXor"
    )
      .flatMap(prefix => List("", "Acquire", "Release").map(prefix + _))
    val methods = reads ++ writes ++ compares ++ compareExchanges ++ rmw
    val bodies = for (tpe <- types; method <- methods) yield {
      val result =
        if (writes.contains(method)) "Unit"
        else if (compares.contains(method)) "Boolean"
        else tpe
      val values =
        if (reads.contains(method)) Nil
        else if (compares.contains(method) || compareExchanges.contains(method))
          List("expected", "desired")
        else List("desired")
      val declarations = values.map(name => s"$name: $tpe")
      val coordinates = if (instance) List("receiver") else Nil
      val receiverDeclaration = if (instance) ", receiver: Object" else ""
      s"def ${method}_$tpe(handle: VarHandle$receiverDeclaration${declarations.map(", " + _).mkString}): $result = handle.$method(${(coordinates ::: values).mkString(", ")})"
    }
    val constants =
      """package scala.scalanative.runtime
        |object VarHandleMatrixConstants {
        |  def plain: Int = NativeVarHandle.MemoryOrder.Plain
        |  def volatile: Int = NativeVarHandle.MemoryOrder.Volatile
        |  def acquire: Int = NativeVarHandle.MemoryOrder.Acquire
        |  def release: Int = NativeVarHandle.MemoryOrder.Release
        |  def or: Int = NativeVarHandle.BitwiseOperation.Or
        |  def and: Int = NativeVarHandle.BitwiseOperation.And
        |  def xor: Int = NativeVarHandle.BitwiseOperation.Xor
        |}
        |""".stripMargin
    compileAndLoad(
      "VarHandleMatrix.scala" ->
        ("import java.lang.invoke.VarHandle\nclass ExactMatrix {\n" + bodies
          .mkString("\n") + "\n}"),
      "VarHandleMatrixConstants.scala" -> constants
    ) { defns =>
      val protocolValues = defns
        .collect {
          case d: nir.Defn.Define
              if d.name.top.id == "scala.scalanative.runtime.VarHandleMatrixConstants$" =>
            d.name.sig.unmangled match {
              case nir.Sig.Method(name, _, _) =>
                d.insts.collectFirst {
                  case nir.Inst.Ret(value: nir.Val.Int) => name -> value
                }
              case _ => None
            }
        }
        .flatten
        .toMap
      val definitions = defns.collect {
        case d: nir.Defn.Define if d.name.top.id == "ExactMatrix" => d
      }
      val matrixDefinitions = definitions.filter(_.name.sig.unmangled match {
        case nir.Sig.Method(name, _, _) =>
          bodies.exists(_.startsWith("def " + name + "("))
        case _ => false
      })
      assertEquals(
        "every matrix method must be emitted",
        types.size * methods.size,
        matrixDefinitions.size
      )
      matrixDefinitions.foreach { d =>
        val nir.Sig.Method(sourceName, _, _) = d.name.sig.unmangled: @unchecked
        val separator = sourceName.lastIndexOf('_')
        val sourceMethod = sourceName.substring(0, separator)
        val sourceType = sourceName.substring(separator + 1)
        val kind = if (sourceType == "AnyRef") "Reference" else sourceType
        val operation =
          if (reads.contains(sourceMethod)) "get"
          else if (writes.contains(sourceMethod)) "set"
          else if (sourceMethod == "compareAndSet") "compare"
          else if (sourceMethod.startsWith("weakCompareAndSet")) "weakCompare"
          else if (compareExchanges.contains(sourceMethod)) "compareExchange"
          else if (sourceMethod.startsWith("getAndSet")) "exchange"
          else if (sourceMethod.startsWith("getAndAdd")) "add"
          else "bitwise"
        val expectedAccessor =
          if (operation == "get" && kind == "Reference") "getBoxedReference"
          else operation + kind
        val ops = d.insts.collect { case nir.Inst.Let(_, op, _) => op }
        assertFalse(
          d.name.show + " must not box or allocate varargs",
          ops.exists {
            case _: nir.Op.Box | _: nir.Op.Unbox | _: nir.Op.Arrayalloc => true
            case _                                                      => false
          }
        )
        assertTrue(
          d.name.show + " must dispatch to the Native protocol",
          ops.exists {
            case nir.Op.Method(_, signature) =>
              signature.unmangled match {
                case nir.Sig.Method(name, _, _) => name == expectedAccessor
                case _                          => false
              }
            case nir.Op
                  .Call(_, nir.Val.Global(member: nir.Global.Member, _), _) =>
              member.owner.id == "scala.scalanative.runtime.NativeVarHandle" && (member.sig.unmangled match {
                case nir.Sig.Method(name, _, _) => name == expectedAccessor
                case _                          => false
              })
            case _ => false
          }
        )
        val order =
          if (sourceMethod.endsWith("Acquire")) "acquire"
          else if (sourceMethod.endsWith("Release")) "release"
          else if (sourceMethod == "get" || sourceMethod == "set" ||
              sourceMethod.endsWith("Opaque") || sourceMethod.endsWith("Plain"))
            "plain"
          else "volatile"
        assertTrue(
          d.name.show + " must use " + order + " order",
          ops.exists {
            case nir.Op.Call(_, _, args) =>
              args.lastOption.contains(protocolValues(order))
            case _ => false
          }
        )
        if (operation == "bitwise") {
          val bit =
            if (sourceMethod.startsWith("getAndBitwiseOr")) "or"
            else if (sourceMethod.startsWith("getAndBitwiseAnd")) "and"
            else "xor"
          assertTrue(
            d.name.show + " must use bitwise " + bit,
            ops.exists {
              case nir.Op.Call(_, _, args) =>
                args.dropRight(1).lastOption.contains(protocolValues(bit))
              case _ => false
            }
          )
        }
      }
    }
  }

  @Test def fieldBindingsReturnRawPointersWithoutBoxing(): Unit =
    compileAndLoad(
      "FieldBindings.scala" ->
        """import java.lang.invoke.MethodHandles
          |class FieldBindings {
          |  private var intValue = 0
          |  private var longValue = 0L
          |  private var reference: Object = null
          |  val intHandle = MethodHandles.lookup().findVarHandle(classOf[FieldBindings], "intValue", classOf[Int])
          |  val longHandle = MethodHandles.lookup().findVarHandle(classOf[FieldBindings], "longValue", classOf[Long])
          |  val referenceHandle = MethodHandles.lookup().findVarHandle(classOf[FieldBindings], "reference", classOf[Object])
          |}
          |""".stripMargin
    ) { defns =>
      val bindings = defns.collect {
        case d: nir.Defn.Class
            if d.traits.exists(
              _.id == "scala.scalanative.runtime.NativeVarHandle$FieldBinding"
            ) =>
          d.name
      }.toSet
      assertEquals(
        "one allocation-free binding for each field",
        3,
        bindings.size
      )
      val methods = defns.collect {
        case d: nir.Defn.Define
            if bindings.contains(d.name.top) && !d.name.sig.isCtor =>
          d
      }
      assertEquals(3, methods.size)
      methods.foreach { method =>
        assertEquals(nir.Type.Ptr, method.ty.ret)
        assertFalse(
          "field bindings must not box the pointer",
          method.insts.exists {
            case nir.Inst.Let(_, _: nir.Op.Box, _) => true
            case _                                 => false
          }
        )
      }
    }

  @Test def primitiveAccessorsRemainUnboxed(): Unit =
    compileAndLoad(
      "ExactAccess.scala" ->
        """import java.lang.invoke.VarHandle
          |class ExactAccess {
          |  def read(handle: VarHandle, receiver: Object): Int = handle.get(receiver)
          |  def add(handle: VarHandle, receiver: Object, value: Int): Int =
          |    handle.getAndAdd(receiver, value)
          |  def discard(handle: VarHandle, receiver: Object, value: Int): Unit = {
          |    val old: Int = handle.getAndAdd(receiver, value)
          |    ()
          |  }
          |}
          |""".stripMargin
    ) { defns =>
      val operations = defns
        .collect {
          case d: nir.Defn.Define if d.name.top.id == "ExactAccess" => d.insts
        }
        .flatten
        .collect { case nir.Inst.Let(_, op, _) => op }
      assertFalse(
        "primitive access must not box or unbox",
        operations.exists {
          case _: nir.Op.Box | _: nir.Op.Unbox => true
          case _                               => false
        }
      )
      assertFalse(
        "primitive access must not allocate varargs",
        operations.exists {
          case _: nir.Op.Arrayalloc => true
          case _                    => false
        }
      )
    }

  private def compiles(source: String): Unit =
    NIRCompiler(_.compile(source.stripMargin))

  private def compilationError(source: String): String =
    assertThrows(
      classOf[CompilationFailedException],
      () => NIRCompiler(_.compile(source.stripMargin))
    ).getMessage

  @Test def resolvesLiteralInstanceLookups(): Unit = compiles(
    """|
       |import java.lang.invoke.MethodHandles
       |class Instance { private var value = 0
       |  val handle = MethodHandles.lookup().findVarHandle(classOf[Instance], "value", Integer.TYPE)
       |  def current = value }
       |"""
  )

  @Test def leavesUnrelatedMethodsWithLookupNamesUntouched(): Unit = compiles(
    """|
       |class UnrelatedLookup {
       |  def findVarHandle(value: Int): Int = value + 1
       |  def findStaticVarHandle(value: Int): Int = value + 2
       |}
       |object UnrelatedUse {
       |  val lookup = new UnrelatedLookup
       |  val instance = lookup.findVarHandle(1)
       |  val static = lookup.findStaticVarHandle(2)
       |}
       |"""
  )

  @Test def lowersStoredHandleCallsToTheResolvedFieldWithoutLookupStubs()
      : Unit =
    compileAndLoad(
      "VarHandleUse.scala" ->
        """|
           |import java.lang.invoke.{MethodHandles, VarHandle}
           |class Box { private var value: Int = 0
           |  private val handle: VarHandle = MethodHandles.lookup()
           |    .findVarHandle(classOf[Box], "value", Integer.TYPE)
           |  def update(): Int = {
           |    handle.compareAndSet(this, 0, 1)
           |    val old: Int = handle.getAndAdd(this, 4)
           |    old
           |  }
           |  def current: Int = value
           |}
           |""".stripMargin
    ) { defns =>
      val ops = defns
        .collect { case defn: nir.Defn.Define => defn.insts }
        .flatten
        .collect { case nir.Inst.Let(_, op, _) => op }

      val resolvedValueAccess = ops.exists {
        case nir.Op.Field(_, member)
            if member.owner.id == "Box" && (member.sig.unmangled match {
              case nir.Sig.Field("value", _) => true; case _ => false
            }) =>
          true
        case nir.Op.Fieldload(_, _, member)
            if member.owner.id == "Box" && (member.sig.unmangled match {
              case nir.Sig.Field("value", _) => true; case _ => false
            }) =>
          true
        case nir.Op.Fieldstore(_, _, member, _)
            if member.owner.id == "Box" && (member.sig.unmangled match {
              case nir.Sig.Field("value", _) => true; case _ => false
            }) =>
          true
        case _ => false
      }
      assertTrue("the resolved Box.value field is absent", resolvedValueAccess)

      val hasLookupStubCall = ops.exists {
        case nir.Op.Call(_, nir.Val.Global(name, _), _) =>
          val owner = name.top.id
          owner == "java.lang.invoke.MethodHandles$Lookup" ||
            owner == "java.lang.invoke.MethodHandles" ||
            owner == "java.lang.invoke.VarHandle"
        case _ => false
      }
      assertFalse(
        "literal lookup must not leave a reachable lookup/access stub call",
        hasLookupStubCall
      )
    }

  @Test def resolvesLiteralStaticLookupsOnScala3(): Unit =
    if (scala.util.Properties.versionNumberString.startsWith("3."))
      compiles(
        """|
       |import java.lang.invoke.MethodHandles
       |class Static
       |object Static {
       |  @scala.annotation.static var value = 0
       |  val handle = MethodHandles.lookup().findStaticVarHandle(classOf[Static], "value", Integer.TYPE)
       |  def current = value
       |}
       |"""
      )

  @Test def rejectsInvalidLiteralLookups(): Unit = {
    def errorFor(lookup: String): String = compilationError(
      s"""|
          |import java.lang.invoke.MethodHandles
          |class Target { private var value: Int = 0 }
          |object Use { val handle = $lookup }
          |"""
    )

    val cases = Seq(
      errorFor(
        "MethodHandles.lookup().findVarHandle(classOf[Target], \"missing\", Integer.TYPE)"
      ) -> "does not contain field missing",
      errorFor(
        "MethodHandles.lookup().findVarHandle(classOf[Target], \"value\", java.lang.Long.TYPE)"
      ) -> "type does not match field value",
      errorFor(
        "MethodHandles.lookup().findStaticVarHandle(classOf[Target], \"value\", Integer.TYPE)"
      ) -> "cannot target instance field value",
      errorFor(
        "MethodHandles.lookup().findVarHandle(classOf[Target], \"value\", Integer.TYPE)"
      ) -> "cannot access field value"
    )
    cases.foreach {
      case (actual, expected) => assertTrue(actual, actual.contains(expected))
    }
  }

  @Test def rejectsDynamicLookupArguments(): Unit = {
    val name = compilationError(
      """|import java.lang.invoke.MethodHandles
         |class Target { var value = 0 }
         |object Use { val name = "value"; val handle = MethodHandles.lookup().findVarHandle(classOf[Target], name, Integer.TYPE) }
         |"""
    )
    assertTrue(name, name.contains("field name must be a literal string"))
  }
}
