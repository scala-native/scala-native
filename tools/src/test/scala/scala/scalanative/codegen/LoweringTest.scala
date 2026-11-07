package scala.scalanative.codegen

import java.nio.file.Files

import org.junit.Assert._
import org.junit.Test

class LoweringTest extends CodeGenSpec {

  @Test def issue4010(): Unit = codegen(
    entry = "Main",
    sources = Map(
      "Main.scala" ->
        """|
           |object Main {
           |  def main(args: Array[String]): Unit = {
           |    val foo = 42
           |    println(Option.empty[Any].getOrElse(throw new RuntimeException(s"foo=$foo")))
           |  }
           |}""".stripMargin
    ),
    setupConfig = _.withOptimize(false)
  ) {
    case (config, result, _) =>
      assertFalse(config.compilerConfig.optimize)
      assertTrue(result.isSuccessful)
  }

  // With opaque pointers, `bitcast ptr %x to ptr` is a no-op and should not be
  // emitted by the code generator. Lower rewrites such redundant bitcasts as
  // copies which are elided at codegen time.
  @Test def noRedundantPtrBitcast(): Unit = codegen(
    entry = "Main",
    sources = Map(
      "Main.scala" ->
        """|
           |object Main {
           |  def main(args: Array[String]): Unit = {
           |    val s = Set(1, 2, 3) ++ Set(3, 4, 5)
           |    println(s.size)
           |  }
           |}""".stripMargin
    )
  ) {
    case (_, _, outfiles) =>
      val pattern = """bitcast ptr %\S+ to ptr\b""".r
      val offending =
        outfiles.iterator.flatMap { path =>
          val content = new String(Files.readAllBytes(path))
          pattern
            .findAllMatchIn(content)
            .map(m => s"${path.getFileName}: ${m.matched}")
        }.toList
      assertTrue(
        s"Found redundant `bitcast ptr %x to ptr` in generated IR:\n${offending.mkString("\n")}",
        offending.isEmpty
      )
  }

  // Blocker call types must include the module receiver.
  @Test def blockerCallTypesMatchDefinitions(): Unit = lowered(
    entry = "Main",
    sources = Map(
      "Main.scala" ->
        """|
           |object Main {
           |  def main(args: Array[String]): Unit = println(args.length)
           |}""".stripMargin
    ),
    setupConfig = _.withMultithreading(true)
  ) { defns =>
    def definedType(name: scala.scalanative.nir.Global) =
      defns.collectFirst {
        case defn: scala.scalanative.nir.Defn.Define if defn.name == name =>
          defn.ty
      }
    for {
      (name, callTy) <- Seq(
        Lower.RuntimeBlockerBeginName -> Lower.RuntimeBlockerBeginTy,
        Lower.RuntimeBlockerEndName -> Lower.RuntimeBlockerEndTy
      )
    } {
      val defined = definedType(name)
      assertTrue(s"$name is not defined", defined.isDefined)
      val scala.scalanative.nir.Type.Function(definedArgs, definedRet) =
        defined.get: @unchecked
      val scala.scalanative.nir.Type.Function(callArgs, callRet) =
        callTy: @unchecked
      assertEquals(s"$name arity", definedArgs.size, callArgs.size)
      assertEquals(s"$name result", definedRet, callRet)
      assertEquals(
        s"$name Boolean arguments",
        definedArgs.map(_ == scala.scalanative.nir.Type.Bool),
        callArgs.map(_ == scala.scalanative.nir.Type.Bool)
      )
    }
  }
}
