package scala.scalanative.codegen

import org.junit.Test
import org.junit.Assert._

class LoweringTest extends CodeGenSpec {

  @Test def issue4010(): Unit = codegen(
    entry = "Main",
    sources = Map("Main.scala" -> """
    |object Main {
    |  def main(args: Array[String]): Unit = {
    |    val foo = 42
    |    println(Option.empty[Any].getOrElse(throw new RuntimeException(s"foo=$foo")))
    |  }
    |}""".stripMargin),
    setupConfig = _.withOptimize(false)
  ) {
    case (config, result, _) =>
      assertFalse(config.compilerConfig.optimize)
      assertTrue(result.isSuccessful)
  }
}
