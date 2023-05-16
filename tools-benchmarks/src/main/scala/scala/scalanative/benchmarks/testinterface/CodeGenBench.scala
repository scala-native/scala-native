package scala.scalanative
package benchmarks
package testinterface

import java.nio.file.{Path, Files}
import java.util.Comparator
import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations._
import org.openjdk.jmh.annotations.Mode._

import scala.scalanative.build._

@Fork(1)
@State(Scope.Benchmark)
@BenchmarkMode(Array(AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
abstract class CodeGenBench(nativeConfig: NativeConfig => NativeConfig) {
  var config: Config = _
  var linked: linker.Result = _

  @Setup(Level.Trial)
  def setup(): Unit = {
    val workdir = Files.createTempDirectory("codegen-bench")
    config = defaultConfig
      .withBaseDir(workdir)
      .withMainClass(Some(TestMain))
      .withCompilerConfig(nativeConfig)
    Files.createDirectories(config.workDir)

    val entries = build.ScalaNative.entries(config)
    util.Scope { implicit scope =>
      linked = ScalaNative.link(config, entries)
    }
  }

  @TearDown(Level.Trial)
  def cleanup(): Unit = {
    val workdir = config.baseDir
    Files
      .walk(workdir)
      .sorted(Comparator.reverseOrder())
      .forEach(Files.delete)
    linked = null
    config = null
  }

  @Benchmark
  def codeGen(): Unit = {
    val paths = ScalaNative.codegen(config, linked)
    assert(paths.nonEmpty)
  }
}

class CodeGen
    extends CodeGenBench(
      nativeConfig = _.withMultithreadingSupport(false)
        .withIncrementalCompilation(false)
    )
class CodeGenWithMultithreading
    extends CodeGenBench(
      nativeConfig = _.withMultithreadingSupport(true)
        .withGC(GC.Immix) // to ensure generation of safepoints
        .withIncrementalCompilation(false)
    )
