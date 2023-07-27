package scala.scalanative
package compiler

import org.junit.Test
import org.junit.Assert._

import scala.collection.mutable

import scala.scalanative.api.CompilationFailedException
import scala.scalanative.linker.StaticForwardersSuite.compileAndLoad
import scala.scalanative.buildinfo.ScalaNativeBuildInfo._
import scala.reflect.ClassTag

class LocalNamesTest {
  import nir._

  case class LocalNames(vals: Seq[String], lets: Seq[String])
  object localNamesTraversal extends nir.Traverse {
    private val vals = mutable.UnrolledBuffer.empty[String]
    private val lets = mutable.UnrolledBuffer.empty[String]

    def apply(defn: Defn): LocalNames = synchronized {
      this.onDefn(defn)
      // UnrolledBuffer.toSeq does not create a copy in Scala 2.12!
      try LocalNames(vals = vals.toList, lets = lets.toList)
      finally { vals.clear(); lets.clear() }
    }

    override def onVal(value: Val): Unit = {
      value match {
        case Val.Local(_, _, name) => vals ++= name
        case _                     => ()
      }
      super.onVal(value)
    }

    override def onInst(inst: Inst): Unit = {
      inst match {
        case Inst.Let(_, name, _, _) => lets ++= name
        case _                       => ()
      }
      super.onInst(inst)
    }
  }

  def assertContainsAll[T](msg: String, expected: Seq[T], actual: Seq[T]) = {
    val diff = expected.diff(actual)
    assertTrue(s"$msg - not found ${diff} in $actual", diff.isEmpty)
  }

  private def findDefinition(linked: Seq[Defn]) = linked
    .find(
      _.name == Global
        .Top("Test$")
        .member(Rt.ScalaMainSig.copy(scope = Sig.Scope.Public))
    )
    .ensuring(_.isDefined, "Not found linked method")

  // Ensure to use all the vals/vars, otherwise they might not be emmited by the compiler
  @Test def localNamesExistence(): Unit = compileAndLoad(
    sources = "Test.scala" -> """
    |object Test {
    |  def main(args: Array[String]): Unit = {
    |    var localVar = args.size
    |    val localVal = localVar + this.##
    |    val scoped = {
    |      var innerVar = args.size
    |      val innerVal = innerVar + 1
    |      innerVal + localVal
    |     }
    |    assert(scoped != 0)
    |  }
    |}
    """.stripMargin
  ) { loaded =>
    def checkLocalNames(defns: Seq[Defn]) =
      findDefinition(defns)
        .ensuring(_.isDefined, "Not found tested method in linked result")
        .map(localNamesTraversal(_))
        .foreach {
          case LocalNames(vals, lets) =>
            val expectedLets =
              Seq("localVal", "localVar", "innerVal", "innerVar", "scoped")
            val expectedVals = Seq("args", "this") ++ expectedLets
            assertContainsAll("lets defined", expectedLets, lets)
            assertContainsAll("vals defined", expectedVals, vals)
            assertContainsAll("no lets duplicates", lets.distinct, lets)
        }
    checkLocalNames(loaded)
  }

  @Test def opsNames(): Unit = compileAndLoad(
    sources = "Test.scala" -> """
    |import scala.scalanative.unsafe
    |import scala.scalanative.unsafe._
    |import scala.scalanative.runtime.Intrinsics
    |import scala.scalanative.runtime.toRawPtr
    |import scala.scalanative.unsigned._
    |object Test {
    |  class Foo()
    |
    |  @noinline def method(n: Int): String = n.toString
    |  private var field: Int = _
    |
    |  def main(args: Array[String]): Unit = {
    |    val call = Test.method(0)
    |    val sizeOf = Intrinsics.sizeOf[Long]
    |    val alignmentOf = Intrinsics.alignmentOf[Long]
    |    val stackalloc = Intrinsics.stackalloc(sizeOf)
    |    val elem = Intrinsics.elemRawPtr(stackalloc, alignmentOf)
    |    val store = Intrinsics.storeInt(elem, 42)
    |    val load = Intrinsics.loadInt(elem)
    |    // val extract = ???
    |    // val insert = ???
    |    val bin = Intrinsics.remUInt(load, 4)
    |    val comp = bin == 2
    |    val conv = Intrinsics.castIntToFloat(bin)
    |    // val fence = ???
    |    val classalloc = new Foo()
    |    val fieldStore = this.field = bin + classalloc.##
    |    val fieldLoad = this.field
    |    val field = Intrinsics.classFieldRawPtr[Test.type](this, "field")
    |    val method: Int => String = Test.method _
    |    // val dynMethod = ???
    |    val module = scala.Predef
    |    val as = Test.asInstanceOf[Option[_]]
    |    val is = as.isInstanceOf[Some[_]]
    |    // val copy = ???
    |    val box: Any = 1.asInstanceOf[Integer]
    |    val unbox: Int = box.asInstanceOf[Int]
    |    var `var` = unbox + 1
    |    val varStore = `var` = args.size
    |    val varLoad = `var`
    |    val arrayAlloc = new Array[Int](4)
    |    val arrayStore = arrayAlloc(0) = varLoad
    |    val arrayLoad = arrayAlloc(0)
    |    val arrayLength = arrayAlloc.length
    |
    |    // forced materialization
    |    assert(arrayLength != varLoad )
    |    assert(arrayLoad != `var`)
    |    assert(classalloc != null)
    |    assert(fieldLoad == Intrinsics.loadInt(field))
    |    assert(comp == is)
    |    assert(conv == 4.0f)
    |    assert(module != null)
    |    assert(sizeOf == alignmentOf)
    |  }
    |}""".stripMargin
  ) { loaded =>
    def checkLocalNames(defns: Seq[Defn]) =
      findDefinition(defns)
        .ensuring(_.isDefined, "Not found tested method in linked result")
        .map(defn => localNamesTraversal(defn) -> defn)
        .foreach {
          case (LocalNames(vals, lets), defn: Defn.Define) =>
            def checkHasLet[T: ClassTag](localName: String): Unit = {
              assertContainsAll("localName", Seq(localName), lets)
              defn.insts.collectFirst {
                case i @ Inst.Let(_, Some(`localName`), op, _) =>
                  val expectedTpe = implicitly[ClassTag[T]].runtimeClass
                  assertTrue(
                    s"$localName: ${op.getClass()} is not ${expectedTpe.getName()}",
                    op.getClass() == expectedTpe
                  )
              }
            }
            def checkNotHasLet[T: ClassTag](localName: String): Unit = {
              assertFalse(
                s"should not contains $localName in $lets",
                lets.contains(localName)
              )
            }
            checkHasLet[Op.Call]("call")
            checkHasLet[Op.Stackalloc]("stackalloc")
            checkHasLet[Op.Elem]("elem")
            // checkHasLet[Op.Extract]("extract")
            // checkHasLet[Op.Insert]("insert")
            checkNotHasLet[Op.Store]("store")
            checkHasLet[Op.Load]("load")
            // checkHasLet[Op.Fence]("fence")
            checkHasLet[Op.Bin]("bin")
            checkHasLet[Op.Comp]("comp")
            checkHasLet[Op.Conv]("conv")
            checkHasLet[Op.Classalloc]("classalloc")
            checkNotHasLet[Op.Fieldstore]("fieldStore")
            checkHasLet[Op.Fieldload]("fieldLoad")
            checkHasLet[Op.Field]("field")
            // checkHasLet[Op.Method]("method")
            // checkHasLet[Op.Dynmethod]("dynMethod")
            checkHasLet[Op.Module]("module")
            checkHasLet[Op.As]("as")
            checkHasLet[Op.Is]("is")
            // checkHasLet[Op.Copy]("copy")
            checkHasLet[Op.SizeOf]("sizeOf")
            checkHasLet[Op.AlignmentOf]("alignmentOf")
            checkHasLet[Op.Box]("box")
            checkHasLet[Op.Unbox]("unbox")
            checkHasLet[Op.Var]("var")
            checkNotHasLet[Op.Varstore]("varStore")
            checkHasLet[Op.Varload]("varLoad")
            checkHasLet[Op.Arrayalloc]("arrayAlloc")
            checkNotHasLet[Op.Arraystore]("arrayStore")
            checkHasLet[Op.Arrayload]("arrayLoad")
            checkHasLet[Op.Arraylength]("arrayLength")

            assertContainsAll("no lets duplicates", lets.distinct, lets)
          case _ => fail()
        }
    checkLocalNames(loaded)
  }

  @Test def switchMatch(): Unit = compileAndLoad(
    sources = "Test.scala" -> """
    |import scala.annotation.switch
    |object Test {
    |  def main(args: Array[String]): Unit = {
    |    val n = args.size
    |    val switchResult = ((n % 3): @switch) match {
    |      case 0 => n
    |      case 1 => n * 42
    |      case 2 => val a = n * 37; a
    |    }
    |    assert(switchResult != 0)
    |  }
    |}
    """.stripMargin
  ) { loaded =>
    def checkLocalNames(defns: Seq[Defn]) =
      findDefinition(defns)
        .ensuring(_.isDefined, "Not found tested method in linked result")
        .map(defn => localNamesTraversal(defn) -> defn)
        .foreach {
          case (LocalNames(vals, lets), defn: nir.Defn.Define) =>
            val expectedLets = Seq("n", "a")
            assertContainsAll("lets defined", expectedLets, lets)
            // switch result defined as param
            val expectedVals = Seq("args", "switchResult") ++ expectedLets
            assertContainsAll("vals defined", expectedVals, vals)
            assertContainsAll("no lets duplicates", lets.distinct, lets)

            defn.insts
              .collect {
                case label @ Inst.Label(_, Seq(param))
                    if param.localName.contains("switchResult") =>
                  label
              }
              .ensuring(
                _.size == 1,
                "switchResult is not a merge label argument"
              )
          case _ => fail()
        }
    checkLocalNames(loaded)
  }

  @Test def matchMatch(): Unit = compileAndLoad(
    sources = "Test.scala" -> """
    |object Test {
    |  def main(args: Array[String]): Unit = {
    |    val n: Option[String] = args.headOption
    |    val matchResult = n match {
    |      case None => 1
    |      case Some("") => 2
    |      case Some(v) => val a = v.length; a
    |    }
    |    assert(matchResult != 0)
    |  }
    |}
    """.stripMargin
  ) { loaded =>
    def checkLocalNames(defns: Seq[Defn]) =
      findDefinition(defns)
        .ensuring(_.isDefined, "Not found tested method in linked result")
        .map(defn => localNamesTraversal(defn) -> defn)
        .foreach {
          case (LocalNames(vals, lets), defn: nir.Defn.Define) =>
            val expectedLets = Seq("a")
            assertContainsAll("lets defined", expectedLets, lets)
            // switch result defined as param
            val expectedVals = Seq("args", "matchResult") ++ expectedLets
            assertContainsAll("vals defined", expectedVals, vals)
            assertContainsAll("no lets duplicates", lets.distinct, lets)

            defn.insts
              .filter {
                case Inst.Label(_, Seq(param)) =>
                  param.localName.contains("matchResult")
                case _ => false
              }
              .ensuring(
                _.size == 1,
                "matchResult is not a merge label argument"
              )
          case _ => fail()
        }
    checkLocalNames(loaded)
  }

  @Test def identReference(): Unit = compileAndLoad(
    sources = "Test.scala" -> """
    |object Test {
    |  def main(args: Array[String]): Unit = {
    |    val n: Option[String] = args.headOption
    |    val x = n
    |  }
    |}
    """.stripMargin
  ) { loaded =>
    def checkLocalNames(defns: Seq[Defn]) =
      findDefinition(defns)
        .ensuring(_.isDefined, "Not found tested method in linked result")
        .map(localNamesTraversal(_))
        .foreach {
          case LocalNames(vals, lets) =>
            // Ensure each of vals n and x has it's own let
            val expectedLets = Seq("n", "x")
            assertContainsAll("lets defined", expectedLets, lets)
            val expectedVals = expectedLets
            assertContainsAll("vals defined", expectedVals, vals)
            assertContainsAll("no lets duplicates", lets.distinct, lets)
        }
    checkLocalNames(loaded)
  }

}
