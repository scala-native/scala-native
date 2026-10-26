package scala.scalanative
package codegen

import java.nio.charset.StandardCharsets
import java.nio.file.Files

import org.junit.Assert._
import org.junit.Test

/** Two links of the same program generate the same LLVM IR. */
class DeterministicCodeGenTest extends CodeGenSpec {

  // Enough subclasses that an order by identity hash differs between links
  private val sources = Map("Test.scala" -> {
    val classes = (1 to 16)
      .map(i => s"final class C$i extends Base { def id: Int = $i }")
      .mkString("\n")
    val instances = (1 to 16).map(i => s"new C$i").mkString(", ")
    s"""|trait Base { def id: Int }
        |$classes
        |object Test {
        |  def main(args: Array[String]): Unit = {
        |    val all: Array[Base] = Array($instances)
        |    println(all.map(_.id).sum)
        |    println(all(args.length).isInstanceOf[C7])
        |  }
        |}
        |""".stripMargin
  })

  // The IR files' contents: their names come from the directory of the
  // sources, which each compilation of the test program writes anew
  private def generatedIR(): Seq[String] =
    codegen("Test", sources) {
      case (_, _, files) =>
        files
          .map(file =>
            new String(Files.readAllBytes(file), StandardCharsets.UTF_8)
          )
          .sorted
    }

  @Test def twoLinksGenerateTheSameIR(): Unit = {
    val first = generatedIR()
    val second = generatedIR()
    assertEquals("the number of IR files", first.size, second.size)
    val differing = first.zip(second).count { case (a, b) => a != b }
    assertEquals("IR files that differ between two links", 0, differing)
  }
}
