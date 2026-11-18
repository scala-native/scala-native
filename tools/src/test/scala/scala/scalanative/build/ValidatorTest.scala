package scala.scalanative.build

import java.nio.file.{Files, Path}

import org.junit.Assert._
import org.junit.Test

class ValidatorTest {

  private def validate(
      clang: Path,
      clangPP: Path,
      triple: Option[String]
  ): String = {
    val config = Config.empty
      .withMainClass(Some("Main"))
      .withLogger(Logger.nullLogger)
      .withCompilerConfig(
        _.withClang(clang).withClangPP(clangPP).withTargetTriple(triple)
      )
    try {
      Validator.validate(config)
      fail("expected BuildException")
      ""
    } catch { case ex: BuildException => ex.getMessage() }
  }

  private def withMissingToolchain(test: (Path, Path) => Unit): Unit = {
    val dir = Files.createTempDirectory("scalanative-missing-llvm")
    try test(dir.resolve("clang"), dir.resolve("clang++"))
    finally Files.delete(dir)
  }

  @Test def missingClangPathsFail(): Unit = withMissingToolchain {
    (clang, clangPP) =>
      for (triple <- Seq(None, Some("x86_64-unknown-linux-gnu"))) {
        val msg = validate(clang, clangPP, triple)
        assertTrue(msg, msg.contains(s"clang path '$clang' does not exist"))
        assertTrue(
          msg,
          msg.contains(s"clang++ path '$clangPP' does not exist")
        )
      }
  }

  private def withLlvmPrefix(test: (Path, Path) => Unit): Unit = {
    val root = Files.createTempDirectory("scalanative-llvm")
    try {
      val bin = Files.createDirectories(root.resolve("bin"))
      Files.createDirectories(root.resolve("include"))
      Files.createFile(bin.resolve("llvm-config"))
      test(root, Files.createFile(bin.resolve("clang")))
    } finally {
      Files
        .walk(root)
        .sorted(java.util.Comparator.reverseOrder[Path]())
        .forEach(Files.delete(_))
    }
  }

  @Test def llvmDirsOfConfiguredClangAreAccepted(): Unit = withLlvmPrefix {
    (prefix, clang) =>
      val options = Seq(s"-I${prefix.resolve("include")}", "-Qunused-arguments")
      assertEquals(Nil, Validator.mismatchedLlvmDirs(clang, options))
  }

  @Test def llvmDirsOfOtherToolchainAreReported(): Unit = withLlvmPrefix {
    (prefix, _) =>
      withLlvmPrefix { (_, otherClang) =>
        val include = s"-I${prefix.resolve("include")}"
        val lib = s"-L${prefix.resolve("lib")}"
        assertEquals(
          Seq(include -> prefix, lib -> prefix),
          Validator.mismatchedLlvmDirs(otherClang, Seq(include, lib, "-I/x"))
        )
      }
  }
}
