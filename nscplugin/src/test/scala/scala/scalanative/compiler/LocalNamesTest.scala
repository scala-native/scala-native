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

  def assertContainsAll[T](
      msg: String,
      expected: Iterable[T],
      actual: Iterable[T]
  ) = {
    val left = expected.toSeq
    val right = actual.toSeq
    val diff = left.diff(right)
    assertTrue(s"$msg - not found ${diff} in $right", diff.isEmpty)
  }

  def assertContains[T](msg: String, expected: T, actual: Iterable[T]) = {
    assertTrue(
      s"$msg - not found ${expected} in ${actual.toSeq}",
      actual.find(_ == expected).isDefined
    )
  }

  def assertDistinct(localNames: Iterable[LocalName]) = {
    val duplicated =
      localNames.groupBy(identity).filter(_._2.size > 1).map(_._1)
    assertTrue(s"Found duplicated names of ${duplicated}", duplicated.isEmpty)
  }

  def namedLets(defn: nir.Defn.Define): Map[Inst.Let, LocalName] =
    defn.insts.collect {
      case inst: Inst.Let if defn.localNames.contains(inst.id) =>
        inst -> defn.localNames(inst.id)
    }.toMap

  private object TestMain {
    val companionMain = Global
      .Top("Test$")
      .member(Rt.ScalaMainSig.copy(scope = Sig.Scope.Public))

    def unapply(name: Global): Boolean = name == companionMain
  }
  private def findDefinition(linked: Seq[Defn]) = linked
    .collectFirst {
      case defn @ Defn.Define(_, TestMain(), _, _, _) =>
        defn
    }
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
      findDefinition(defns).foreach { defn =>
        val lets = namedLets(defn).values
        val expectedLetNames =
          Seq("localVal", "localVar", "innerVal", "innerVar", "scoped")
        val expectedNames = Seq("args", "this") ++ expectedLetNames
        assertContainsAll("lets defined", expectedLetNames, lets)
        assertContainsAll("vals defined", expectedNames, defn.localNames.values)
        assertDistinct(lets)
        defn.insts.head match {
          case Inst.Label(
                _,
                Seq(
                  Val.Local(thisId, Type.Ref(Global.Top("Test$"), _, _)),
                  Val.Local(argsId, Type.Array(Rt.String, _))
                )
              ) =>
            assertTrue("thisArg", defn.localNames.get(thisId).contains("this"))
            assertTrue("argsArg", defn.localNames.get(argsId).contains("args"))
          case _ => fail("Invalid input label")
        }
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
    |    assert(comp && conv == 4.0f)
    |    // val fence = ???
    |    val classalloc = new Foo()
    |    val fieldStore = this.field = bin + classalloc.##
    |    val fieldLoad = this.field
    |    val field = Intrinsics.classFieldRawPtr[Test.type](this, "field")
    |    // val method: Int => String = Test.method _
    |    // val dynMethod = ???
    |    val module = scala.Predef
    |    val as = Test.asInstanceOf[Option[_]]
    |    val is = as.isInstanceOf[Some[_]]
    |    val copy = 42
    |    val box: Any = 1.asInstanceOf[Integer]
    |    val unbox: Int = box.asInstanceOf[Int]
    |    var `var` = unbox + 1
    |    val varStore = `var` = args.size
    |    val varLoad = `var`
    |    val arrayAlloc = new Array[Int](4)
    |    val arrayStore = arrayAlloc(0) = varLoad
    |    val arrayLoad = arrayAlloc(0)
    |    val arrayLength = arrayAlloc.length
    |  }
    |}""".stripMargin
  ) { loaded =>
    def checkLocalNames(defns: Seq[Defn]) =
      findDefinition(defns)
        .foreach { defn =>
          def checkHasLet[T: ClassTag](localName: String): Unit = {
            assertContains("localName", localName, defn.localNames.values)
            namedLets(defn)
              .collectFirst { case (inst, `localName`) => inst }
              .map {
                case Inst.Let(_, op, _) =>
                  val expectedTpe = implicitly[ClassTag[T]].runtimeClass
                  assertTrue(
                    s"$localName: ${op.getClass()} is not ${expectedTpe.getName()}",
                    op.getClass() == expectedTpe
                  )
              }
              .getOrElse(fail(s"Not found let with name $localName"))
          }
          def checkNotHasLet[T: ClassTag](localName: String): Unit = {
            assertFalse(
              s"should not contains $localName in ${defn.localNames.values.toSeq}",
              defn.localNames.values.find(_ == localName).isDefined
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
          if (scalaVersion.startsWith("3."))
            checkHasLet[Op.Fieldload]("fieldLoad")
          else // unable to express in Scala 2
            checkHasLet[Op.Call]("fieldLoad")
          checkHasLet[Op.Field]("field")
          // checkHasLet[Op.Method]("method")
          // checkHasLet[Op.Dynmethod]("dynMethod")
          checkHasLet[Op.Module]("module")
          checkHasLet[Op.As]("as")
          checkHasLet[Op.Is]("is")
          checkHasLet[Op.Copy]("copy")
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

          assertDistinct(namedLets(defn).values)
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
        .foreach { defn =>
          val lets = namedLets(defn).values
          val expectedLets = Seq("n", "a")
          assertContainsAll("lets defined", expectedLets, lets)
          // switch result defined as param
          val expectedVals = Seq("args", "switchResult") ++ expectedLets
          assertContainsAll(
            "vals defined",
            expectedVals,
            defn.localNames.values
          )

          defn.insts
            .collect {
              case label @ Inst.Label(_, Seq(param))
                  if defn.localNames.get(param.id).contains("switchResult") =>
                label
            }
            .ensuring(_.size == 1, "switchResult is not a merge label argument")
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
        .foreach { defn =>
          val lets = namedLets(defn).values
          val expectedLets = Seq("a")
          assertContainsAll("lets defined", expectedLets, lets)
          // switch result defined as param
          val expectedVals = Seq("args", "matchResult") ++ expectedLets
          assertContainsAll(
            "vals defined",
            expectedVals,
            defn.localNames.values
          )
          // exclude synthetic names introduced in Scala 2
          assertDistinct(lets.toSeq.diff(Seq("x3")))

          defn.insts
            .filter {
              case Inst.Label(_, Seq(param)) =>
                defn.localNames.get(param.id).contains("matchResult")
              case _ => false
            }
            .ensuring(_.size == 1, "matchResult is not a merge label argument")

        }
    checkLocalNames(loaded)
  }

  // TODO
  // @Test def identReference(): Unit = compileAndLoad(
  //   sources = "Test.scala" -> """
  //   |object Test {
  //   |  def main(args: Array[String]): Unit = {
  //   |    val n: Option[String] = args.headOption
  //   |    val x = n
  //   |  }
  //   |}
  //   """.stripMargin
  // ) { loaded =>
  //   def checkLocalNames(defns: Seq[Defn]) =
  //     findDefinition(defns)
  //       .foreach { defn =>
  //         val lets = namedLets(defn).values
  //         // Ensure each of vals n and x has it's own let
  //         val expectedLets = Seq("n", "x")
  //         assertContainsAll("lets defined", expectedLets, lets)
  //         val expectedVals = expectedLets
  //         assertContainsAll(
  //           "vals defined",
  //           expectedVals,
  //           defn.localNames.values
  //         )
  //         assertEquals("no lets duplicates", lets.toSeq.distinct, lets.toSeq)
  //       }
  //   checkLocalNames(loaded)
  // }

}
