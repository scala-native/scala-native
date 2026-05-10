package scala.scalanative.codegen

import java.nio.file.Files

import org.junit.Assert._
import org.junit.Test

class ModuleFlagsTest extends CodeGenSpec {

  private val source = Map(
    "Main.scala" ->
      """|object Main {
         |  def main(args: Array[String]): Unit = println("ok")
         |}""".stripMargin
  )

  private val PicFlag = """!"PIC Level", i32 2"""
  private val PieFlag = """!"PIE Level", i32 2"""

  private def emittedIr(outfiles: Seq[java.nio.file.Path]): String =
    outfiles.iterator
      .map(p => new String(Files.readAllBytes(p)))
      .mkString("\n")

  @Test def emitsPicAndPieModuleFlagsOnElf(): Unit = codegen(
    entry = "Main",
    sources = source,
    setupConfig = _.withTargetTriple("aarch64-unknown-linux-gnu")
  ) {
    case (_, _, outfiles) =>
      val ir = emittedIr(outfiles)
      assertTrue(s"Expected `$PicFlag` in emitted IR", ir.contains(PicFlag))
      assertTrue(s"Expected `$PieFlag` in emitted IR", ir.contains(PieFlag))
  }

  @Test def omitsPicAndPieModuleFlagsOnWindows(): Unit = codegen(
    entry = "Main",
    sources = source,
    setupConfig = _.withTargetTriple("x86_64-pc-windows-msvc")
  ) {
    case (_, _, outfiles) =>
      val ir = emittedIr(outfiles)
      assertFalse(
        s"PIC Level must not be emitted on Windows",
        ir.contains(PicFlag)
      )
      assertFalse(
        s"PIE Level must not be emitted on Windows",
        ir.contains(PieFlag)
      )
  }
}
