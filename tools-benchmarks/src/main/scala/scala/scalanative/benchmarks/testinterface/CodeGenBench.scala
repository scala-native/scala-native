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
      .withWorkdir(workdir)
      .withMainClass(TestMain)
      .withCompilerConfig(nativeConfig)
    Files.createDirectories(config.workdir)

    val entries = build.core.ScalaNative.entries(config)
    util.Scope { implicit scope =>
      linked = core.ScalaNative.link(config, entries)
    }
  }

  @TearDown(Level.Trial)
  def cleanup(): Unit = {
    val workdir = config.workdir
    Files
      .walk(workdir)
      .sorted(Comparator.reverseOrder())
      .forEach(Files.delete)
    linked = null
    config = null
  }

  @Benchmark
  def codeGen(): Unit = {
    val paths = core.ScalaNative.codegen(config, linked)
    assert(paths.nonEmpty)
  }
}

class CodeGen
    extends CodeGenBench(
      nativeConfig = _.withIncrementalCompilation(false)
    )
