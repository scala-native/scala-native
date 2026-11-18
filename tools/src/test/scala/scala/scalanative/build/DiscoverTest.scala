package scala.scalanative.build

import scala.sys.process._

import org.junit.Assert._
import org.junit.Test

class DiscoverTest {

  @Test def compileOptionsUseOnlyGivenIncludeDirs(): Unit = {
    assertEquals(
      Seq("-I/a", "-I/b", "-Qunused-arguments"),
      Discover.defaultCompileOptions(Seq("/a", "/b"))
    )
  }

  @Test def compileOptionsSkipLlvmConfigIncludeDir(): Unit = {
    val llvmIncludeDir = Discover
      .tryDiscover("llvm-config", "LLVM_BIN")
      .map(p => Process(Seq(p.toString, "--includedir")).!!.trim)
      .toOption
    for (dir <- llvmIncludeDir)
      assertFalse(Discover.compileOptions().contains(s"-I$dir"))
  }
}
