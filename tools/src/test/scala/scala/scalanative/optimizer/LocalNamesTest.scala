package scala.scalanative
package optimizer

import org.junit.Test
import org.junit.Assert._

import scala.collection.mutable

import scala.scalanative.api.CompilationFailedException
import scala.scalanative.buildinfo.ScalaNativeBuildInfo._
import scala.reflect.ClassTag

class LocalNamesTest extends OptimizerSpec {
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

  def afterLowering(config: build.Config, optimized: => linker.Result)(
      fn: Seq[Defn] => Unit
  ): Unit = {
    import scala.scalanative.codegen._
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent._
    val defns = optimized.defns
    implicit def logger: build.Logger = config.logger
    implicit val platform: PlatformInfo = PlatformInfo(config)
    implicit val meta: Metadata =
      new Metadata(optimized, config.compilerConfig, Nil)
    val lowered = llvm.CodeGen.lower(defns)
    Await.result(lowered.map(fn), duration.Duration.Inf)
  }

  // Ensure to use all the vals/vars, otherwise they might not be emmited by the compiler
  @Test def localNamesExistence(): Unit = optimize(
    entry = "Test",
    sources = Map("Test.scala" -> """
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
    """.stripMargin)
  ) {
    case (config, result) =>
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
      checkLocalNames(result.defns)
      afterLowering(config, result)(checkLocalNames)
  }

  @Test def opsNames(): Unit = optimize(
    entry = "Test",
    sources = Map("Test.scala" -> """
    |import scala.scalanative.unsafe
    |import scala.scalanative.unsafe._
    |import scala.scalanative.annotation.nooptimize
    |import scala.scalanative.runtime.Intrinsics
    |import scala.scalanative.runtime.toRawPtr
    |import scala.scalanative.unsigned._
    |
    |object Hack {
    |  @nooptimize def run(): Any = Test.main(Array.empty[String])
    |}
    |
    |object Test {
    |  class Foo()
    |  assert(Hack.run() != null)
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
    |}""".stripMargin),
    setupConfig = _.withMode(build.Mode.ReleaseFull)
  ) {
    case (config, result) =>
      def checkLocalNames(defns: Seq[Defn], beforeLowering: Boolean) =
        findDefinition(defns)
          .ensuring(_.isDefined, "Not found tested method in linked result")
          .map(defn => localNamesTraversal(defn) -> defn)
          .foreach {
            case (LocalNames(vals, lets), defn: Defn.Define) =>
              val stage = if (beforeLowering) "optimized" else "lowered"
              def checkHasLetEither[Optimized: ClassTag, Lowered: ClassTag](
                  localName: String
              ): Unit = {
                if (beforeLowering) checkHasLet[Optimized](localName)
                else checkHasLet[Lowered](localName)
              }
              def checkHasLet[T: ClassTag](localName: String): Unit = {
                assertContainsAll(s"hasLet in $stage)", Seq(localName), lets)
                defn.insts.collectFirst {
                  case i @ Inst.Let(_, Some(`localName`), op, _) =>
                    val expectedTpe = implicitly[ClassTag[T]].runtimeClass
                    assertTrue(
                      s"$localName: ${op.getClass()} is not ${expectedTpe
                          .getName()} - $stage",
                      op.getClass() == expectedTpe
                    )
                }
              }
              def checkNotHasLet[T: ClassTag](localName: String): Unit = {
                assertFalse(
                  s"should not contains $localName in $lets - $stage",
                  lets.contains(localName)
                )
              }
              def checkHasVal(localName: String): Unit = {
                assertContainsAll(s"hasVal in $stage", Seq(localName), vals)
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
              checkHasLetEither[Op.Classalloc, Op.Call]("classalloc")
              checkNotHasLet[Op.Fieldstore]("fieldStore")
              checkHasLetEither[Op.Fieldload, Op.Load]("fieldLoad")
              checkHasLetEither[Op.Field, Op.Copy]("field")
              // checkHasLet[Op.Method]("method")
              // checkHasLet[Op.Dynmethod]("dynMethod")
              checkHasLetEither[Op.Module, Op.Call]("module")
              checkHasLetEither[Op.As, Op.Copy]("as")
              if (beforeLowering) checkHasLet[Op.Is]("is")
              else checkHasVal("is") // lowered to if-else branch, `is` should be param
              // checkHasLet[Op.Copy]("copy")
              checkHasLetEither[Op.SizeOf, Op.Copy]("sizeOf")
              checkHasLetEither[Op.AlignmentOf, Op.Copy]("alignmentOf")
              checkHasLetEither[Op.Box, Op.Call]("box")
              checkHasLetEither[Op.Unbox, Op.Call]("unbox")
              checkHasLetEither[Op.Var, Op.Stackalloc]("var")
              checkNotHasLet[Op.Varstore]("varStore")
              checkHasLetEither[Op.Varload, Op.Load]("varLoad")
              checkHasLetEither[Op.Arrayalloc, Op.Call]("arrayAlloc")
              checkNotHasLet[Op.Arraystore]("arrayStore")
              checkHasLetEither[Op.Arrayload, Op.Load]("arrayLoad")
              checkHasLetEither[Op.Arraylength, Op.Load]("arrayLength")

              assertContainsAll("no lets duplicates", lets.distinct, lets)
            case _ => fail()
          }
      checkLocalNames(result.defns, beforeLowering = true)
      afterLowering(config, result) {
        checkLocalNames(_, beforeLowering = false)
      }
  }

  @Test def inlinedNames(): Unit = optimize(
    entry = "Test",
    sources = Map("Test.scala" -> """
    |import scala.scalanative.annotation._
    |object Hack {
    |  @nooptimize def run(): Any = Test.main(Array.empty[String])
    |}
    |
    |object Test {
    |  assert(Hack.run() != null)
    |
    |  @alwaysinline def fn1(n: Int, m: Int, p: Int): Int = {
    |    val temp = n * m
    |    val temp2 = (temp % 3) match {
    |      case 0 => n
    |      case 1 => val a = n * m; a
    |      case 2 => val b = n * m; val c = b + n; c
    |      case _ => 42
    |    }
    |    temp2 * n
    |  }
    |
    |  def main(args: Array[String]): Unit = {
    |    val argInt = args.size
    |    val result = fn1(argInt, argInt * 2, 42)
    |    val result2 =  fn1(argInt, argInt * 21, 37)
    |    assert(result == result2)
    |  }
    |}""".stripMargin),
    setupConfig = _.withMode(build.Mode.ReleaseFull)
  ) {
    // TODO: How to effectively distinguish inlined `temp2` in `result` and `result2`? Maybe concatation of owner strings, eg. `result.temp2`
    // %3000007 <result> = imul[int] %17000001 <temp2> : int, %7000001 <argInt> : int
    // %3000008 <result2> = imul[int] %24000001 <temp2> : int, %7000001 <argInt> : int

    case (config, result) =>
      def checkLocalNames(defns: Seq[Defn]) =
        findDefinition(defns)
          .ensuring(_.isDefined, "Not found tested method in linked result")
          .map(defn => localNamesTraversal(defn) -> defn)
          .foreach {
            case (LocalNames(vals, lets), defn: Defn.Define) =>
              val expectedLets =
                Seq(
                  "argInt",
                  "result",
                  "result2",
                  "temp",
                  "c"
                ) // a,b are optimized out
              val expectedVals =
                expectedLets ++ Seq("temp2") // match merge block param
              assertContainsAll("lets", expectedLets, lets)
              assertContainsAll("vals", expectedVals, vals)
              assertContainsAll("no lets duplicates", lets.distinct, lets)
            case _ => fail()
          }
      checkLocalNames(result.defns)
  }

  @Test def delayedVars(): Unit = optimize(
    entry = "Test",
    sources = Map("Test.scala" -> """
    | import scala.scalanative.annotation._
    | object Hack {
    |   @nooptimize def run(): Any = Test.main(Array.empty[String])
    | }
    |
    |object Test {
    |  @noinline @nooptimize def parse(v: String): Int = v.toInt
    |  assert(Hack.run() != null)
    |  def main(args: Array[String]): Unit = {
    |    val bits = parse(args(0))
    |    val a = parse(args(1))
    |    val b = bits & 0xFF
    |    var x = 0
    |    var y = 0L
    |    if (a == 0) {
    |      x = bits
    |      y = b
    |    } else {
    |      x = a
    |      y = b | (1L << 0xFF)
    |    }
    |    assert(x != y)
    |  }  
    |}""".stripMargin),
    setupConfig = _.withMode(build.Mode.ReleaseFull)
  ) {
    case (config, result) =>
      def checkLocalNames(defns: Seq[Defn]) =
        findDefinition(defns)
          .ensuring(_.isDefined, "Not found tested method in linked result")
          .map(defn => localNamesTraversal(defn) -> defn)
          .foreach {
            case (LocalNames(vals, lets), defn: Defn.Define) =>
              val expectedLets = Seq("bits", "a", "b")
              //  x,y vars are replsaced with params after if-else expr
              val asParams = Seq("x", "y")
              val expectedVals = expectedLets ++ asParams
              assertContainsAll("lets", expectedLets, lets)
              assertEquals("asParams", asParams, asParams.diff(lets))
              assertContainsAll("vals", expectedVals, vals)
              assertContainsAll("no lets duplicates", lets.distinct, lets)
              defn.insts
                .collectFirst {
                  case Inst.Label(_, params)
                      if asParams.diff(params.flatMap(_.localName)).isEmpty =>
                    ()
                }
                .getOrElse(fail("not found label with expected params"))
            case _ => fail()
          }
      checkLocalNames(result.defns)
  }

}
