package scala.scalanative
package optimizer

import org.junit.Test
import org.junit.Assert._

import scala.collection.mutable

import scala.scalanative.api.CompilationFailedException
import scala.scalanative.buildinfo.ScalaNativeBuildInfo._
import scala.reflect.ClassTag
import scala.scalanative.nir.Global.Member

class LocalNamesTest extends OptimizerSpec {
  import nir._

  override def optimize[T](
      entry: String,
      sources: Map[String, String],
      setupConfig: build.NativeConfig => build.NativeConfig = identity
  )(fn: (build.Config, linker.Result) => T) =
    super.optimize(
      entry,
      sources,
      setupConfig.andThen(
        _.withDebugMetadata(true)
          .withMode(build.Mode.releaseFull)
      )
    )(fn)

  // Ensure to use all the vals/vars, otherwise they might not be emmited by the compiler
  @Test def localNamesExistence(): Unit = super.optimize(
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
    """.stripMargin),
    setupConfig = _.withDebugMetadata(true)
  ) {
    case (config, result) =>
      def checkLocalNames(defns: Seq[Defn]) =
        findEntry(defns).foreach { defn =>
          val localNames = defn.debugInfo.localNames
          val lets = namedLets(defn).values
          val expectedLetNames =
            Seq("localVal", "localVar", "innerVal", "innerVar", "scoped")
          defn.insts.head match {
            case Inst.Label(
                  _,
                  Seq(
                    Val.Local(thisId, Type.Ref(Global.Top("Test$"), _, _)),
                    Val.Local(argsId, Type.Array(Rt.String, _))
                  )
                ) =>
              assertTrue("thisArg", localNames.get(thisId).contains("this"))
              assertTrue("argsArg", localNames.get(argsId).contains("args"))
            case _ => fail("Invalid input label")
          }
          val expectedNames = Seq("args", "this") ++ expectedLetNames
          assertContainsAll("lets defined", expectedLetNames, lets)
          assertContainsAll(
            "vals defined",
            expectedNames,
            defn.debugInfo.localNames.values
          )
          assertDistinct(lets)
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
    |object Test {
    |  class Foo()
    |
    |  @noinline def method(n: Int): String = n.toString
    |  @noinline def getInteger: Integer = 42
    |  @noinline def getArray: Array[Int] = Array(42)
    |  private var field: Int = _
    |
    |  def main(args: Array[String]): Unit = {
    |    val call = Test.method(0)
    |    val sizeOf = Intrinsics.sizeOf[String]
    |    val alignmentOf = Intrinsics.alignmentOf[String]
    |    val stackalloc = Intrinsics.stackalloc[Byte](sizeOf)
    |    val elem = Intrinsics.elemRawPtr(stackalloc, alignmentOf)
    |    val store = Intrinsics.storeInt(elem, Intrinsics.castRawSizeToInt(sizeOf))
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
    |    // val method: Int => String = Test.method _
    |    // val dynMethod = ???
    |    val module = scala.Predef
    |    val as = Test.asInstanceOf[Option[_]]
    |    val is = as.isInstanceOf[Some[_]]
    |    val copy = 42
    |    val intArg: Int = Intrinsics.castRawSizeToInt(sizeOf) + copy
    |    val box: Any = intArg.asInstanceOf[Integer]
    |    val unbox: Int = getInteger.asInstanceOf[Int]
    |    var `var` = unbox + 1
    |    while(`var` < 2) {
    |       val varStore = `var` = `var` + getInteger
    |    }
    |    val varLoad = `var`
    |    val arrayAlloc = new Array[Int](4)
    |    val arrayStore = arrayAlloc(0) = varLoad
    |    val arrayLoad = getArray(1)
    |    val arrayLength = getArray.length
    |
    |    // forced materialization
    |    println(sizeOf == alignmentOf)
    |    println(classalloc != null)
    |    println(fieldLoad == Intrinsics.loadInt(field))
    |    println(comp == is)
    |    println(conv == Intrinsics.loadFloat(field))
    |    println(box != getInteger)
    |    println(module != null)
    |    println(arrayLoad != `var`)
    |    println(arrayLength != varLoad )
    |    println(arrayAlloc)
    |  }
    |}""".stripMargin)
  ) {
    case (config, result) =>
      val platformInfo = codegen.PlatformInfo(config)
      val usesOpaquePointers = platformInfo.useOpaquePointers
      def checkLocalNames(defns: Seq[Defn], beforeLowering: Boolean) =
        findEntry(defns)
          .foreach { defn =>
            val lets = namedLets(defn)
            val stage = if (beforeLowering) "optimized" else "lowered"
            def checkHasLetEither[Optimized: ClassTag, Lowered: ClassTag](
                localName: String
            ): Unit = {
              if (beforeLowering) checkHasLet[Optimized](localName)
              else checkHasLet[Lowered](localName)
            }
            def checkHasLet[T: ClassTag](localName: String): Unit = {
              assertContains(s"hasLet in $stage", localName, lets.values)
              lets
                .collectFirst {
                  case (Inst.Let(_, op, _), `localName`) =>
                    val expectedTpe = implicitly[ClassTag[T]].runtimeClass
                    assertTrue(
                      s"$localName: ${op.getClass()} is not ${expectedTpe
                          .getName()} - $stage",
                      op.getClass() == expectedTpe
                    )
                }
                .getOrElse(fail(s"not found let $localName"))
            }
            def checkNotHasLet[T: ClassTag](localName: String): Unit = {
              assertFalse(
                s"should not contains $localName in ${lets.values.toSet} - $stage",
                lets.values.find(_ == localName).isDefined
              )
            }
            def checkHasVal(localName: String): Unit = {
              assertContainsAll(
                s"hasVal in $stage",
                Seq(localName),
                defn.debugInfo.localNames.values
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
            checkHasLetEither[Op.Classalloc, Op.Call]("classalloc")
            checkNotHasLet[Op.Fieldstore]("fieldStore")
            checkHasLetEither[Op.Fieldload, Op.Load]("fieldLoad")
            checkHasLetEither[Op.Field, Op.Copy]("field")
            // checkHasLet[Op.Method]("method")
            // checkHasLet[Op.Dynmethod]("dynMethod")
            checkHasLetEither[Op.Module, Op.Call]("module")
            if (usesOpaquePointers)
              checkHasLetEither[Op.As, Op.Copy]("as")
            else
              checkHasLetEither[Op.As, Op.Conv]("as")
            // lowered to if-else branch, `is` should be param
            if (beforeLowering) checkHasLet[Op.Is]("is")
            else checkHasVal("is")
            checkNotHasLet[Op.Copy]("copy") // optimized out
            checkHasLetEither[Op.SizeOf, Op.Copy]("sizeOf")
            checkNotHasLet[Op.AlignmentOf]("alignmentOf") // optimized out
            checkHasLetEither[Op.Box, Op.Call]("box") // optimized out
            checkHasLetEither[Op.Unbox, Op.Call]("unbox")
            checkNotHasLet[Op.Var]("var") // optimized out
            checkHasVal("var")
            checkNotHasLet[Op.Varstore]("varStore")
            checkNotHasLet[Op.Varload]("varLoad")
            checkHasLetEither[Op.Arrayalloc, Op.Call]("arrayAlloc")
            checkNotHasLet[Op.Arraystore]("arrayStore")
            checkHasLetEither[Op.Arrayload, Op.Load]("arrayLoad")
            checkHasLetEither[Op.Arraylength, Op.Load]("arrayLength")
            // Filter out inlined names
            assertDistinct(lets.values.toSeq.diff(Seq("buffer", "addr")))
          }
      checkLocalNames(result.defns, beforeLowering = true)
      afterLowering(config, result) {
        checkLocalNames(_, beforeLowering = false)
      }
  }

  @Test def delayedVars(): Unit = optimize(
    entry = "Test",
    sources = Map("Test.scala" -> """
    |import scala.scalanative.annotation.nooptimize
    |
    |object Test {
    |  @noinline @nooptimize def parse(v: String): Int = v.toInt
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
    |}""".stripMargin)
  ) {
    case (config, result) =>
      def checkLocalNames(defns: Seq[Defn]) =
        findEntry(defns)
          .foreach { defn =>
            val lets = namedLets(defn)
            val letsNames = lets.values.toSeq
            val expectedLets = Seq("bits", "a", "b")
            //  x,y vars are replsaced with params after if-else expr
            val asParams = Seq("x", "y")
            val expectedNames = expectedLets ++ asParams
            assertContainsAll("lets", expectedLets, letsNames)
            assertEquals("asParams", asParams, asParams.diff(letsNames))
            assertContainsAll("vals", expectedNames, defn.debugInfo.localNames.values)
            // allowed, delayed and duplicated in each if-else branch
            assertDistinct(letsNames.diff(Seq("b")))
            defn.insts
              .find {
                case Inst.Label(_, params) =>
                  asParams
                    .diff(params.map(_.id).flatMap(defn.debugInfo.localNames.get))
                    .isEmpty
                case _ => false
              }
              .getOrElse(fail("not found label with expected params"))
          }
      checkLocalNames(result.defns)
  }

  @Test def inlinedNames(): Unit = optimize(
    entry = "Test",
    sources = Map("Test.scala" -> """
    |import scala.scalanative.annotation.alwaysinline
    |object Test {
    |  @alwaysinline def fn1(n: Int, m: Int, p: Int): Int = {
    |    val temp = n * m
    |    val temp2 = (temp % 3) match {
    |      case 0 => n
    |      case 1 => val a = n * p; a + 1
    |      case 2 => val b = n * p; val c = b + n; c + 1
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
    |}""".stripMargin)
  ) {
    // TODO: How to effectively distinguish inlined `temp2` in `result` and `result2`? Maybe concatation of owner strings, eg. `result.temp2`
    // %3000007 <result> = imul[int] %17000001 <temp2> : int, %7000001 <argInt> : int
    // %3000008 <result2> = imul[int] %24000001 <temp2> : int, %7000001 <argInt> : int

    case (config, result) =>
      def checkLocalNames(defns: Seq[Defn]) =
        findEntry(defns)
          .foreach { defn =>
            val lets = namedLets(defn).values
            val expectedLets =
              Seq("argInt", "result", "result2", "temp", "a", "b", "c")
            // match merge block param
            val expectedNames = expectedLets ++ Seq("temp2")
            assertContainsAll("lets", expectedLets, lets)
            assertContainsAll("vals", expectedNames, defn.debugInfo.localNames.values)
          }
      checkLocalNames(result.defns)
  }

  @Test def inlinedNames2(): Unit = optimize(
    entry = "Test",
    sources = Map("Test.scala" -> """
    |import scala.scalanative.annotation._
    |
    |sealed trait Interface {
    |  def execute(arg: Int): Int = { val temp = arg * arg; temp % arg}
    |}
    |class Impl1 extends Interface {
    |  override def execute(arg: Int): Int = {val temp1 = arg * arg; temp1 + arg }
    |}
    |class Impl2 extends Interface {
    |  override def execute(arg: Int): Int = {val temp2 = super.execute(arg); temp2 * arg }
    |}
    |class Impl3 extends Impl2 {
    |  override def execute(arg: Int): Int = {val temp3 = super.execute(arg); temp3 * arg }
    |}
    |
    |object Test {
    |  @noinline def impls = Array(new Interface{}, new Impl1(), new Impl2(), new Impl3())
    |
    |  def main(args: Array[String]): Unit = {
    |    val argInt = args.size
    |    val impl: Interface = impls(argInt)
    |    val result = impl.execute(argInt)
    |    assert(result > 0)
    |  }
    |}""".stripMargin)
  ) {
    case (config, result) =>
      def checkLocalNames(defns: Seq[Defn]) =
        findEntry(defns)
          .foreach { defn =>
            val lets = namedLets(defn).values
            val expectedLets =
              Seq("argInt", "impl", "temp", "temp1", "temp2", "temp3")
            val expectedNames = expectedLets ++ Seq("result", "args")
            assertContainsAll("lets", expectedLets, lets)
            assertContainsAll("vals", expectedNames, defn.debugInfo.localNames.values)
          }
      checkLocalNames(result.defns)
  }

  @Test def polyInlinedNames(): Unit = optimize(
    entry = "Test",
    sources = Map("Test.scala" -> """
    |import scala.scalanative.annotation._
    |
    |sealed trait Interface {
    |  @noinline def execute(arg: Int): Int = { val temp = arg * arg; temp % arg}
    |}
    |class Impl1 extends Interface {
    |  @noinline override def execute(arg: Int): Int = {val temp1 = arg * arg; temp1 + arg }
    |}
    |class Impl2 extends Interface {
    |  @noinline override def execute(arg: Int): Int = {val temp2 = super.execute(arg); temp2 * arg }
    |}
    |
    |object Test {
    |  @noinline def impls = Array(new Interface{}, new Impl1(), new Impl2())
    |
    |  def main(args: Array[String]): Unit = {
    |    val argInt = args.size
    |    val impl: Interface = impls(argInt)
    |    val result = impl.execute(argInt)
    |    assert(result > 0)
    |  }
    |}""".stripMargin)
  ) {
    case (config, result) =>
      def checkLocalNames(defns: Seq[Defn]) =
        findEntry(defns)
          .foreach { defn =>
            val lets = namedLets(defn).values
            val expectedLets =
              Seq("argInt", "impl")
            val expectedNames = expectedLets ++ Seq("result")
            assertContainsAll("lets", expectedLets, lets)
            assertContainsAll("vals", expectedNames, defn.debugInfo.localNames.values)
          }
      checkLocalNames(result.defns)
  }

}
