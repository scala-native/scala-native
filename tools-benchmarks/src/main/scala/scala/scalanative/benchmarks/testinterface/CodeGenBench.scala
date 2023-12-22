package scala.scalanative
package benchmarks
package testinterface

import java.nio.file.{Path, Files}
import java.util.Comparator
import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations._
import org.openjdk.jmh.annotations.Mode._

import scala.scalanative.build._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.scalanative.linker.ReachabilityAnalysis

@Fork(1)
@State(Scope.Benchmark)
@BenchmarkMode(Array(AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
abstract class CodeGenBench(nativeConfig: NativeConfig => NativeConfig) {
  var config: Config = _
  var analysis: ReachabilityAnalysis.Result = _

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
      analysis = Await.result(
        ScalaNative.link(config, entries),
        Duration.Inf
      )
    }
  }

  @TearDown(Level.Trial)
  def cleanup(): Unit = {
    val workdir = config.baseDir
    Files
      .walk(workdir)
      .sorted(Comparator.reverseOrder())
      .forEach(Files.delete)
    analysis = null
    config = null
  }

  @Benchmark
  def codeGen(): Unit = {
    val codegen = ScalaNative.codegen(config, analysis)
    val paths = Await.result(codegen, Duration.Inf)
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
        .withGC(GC.Immix) // to ensure generation of GC yieldpoints
        .withIncrementalCompilation(false)
    )

class CodeGenWithDebugMetadata
    extends CodeGenBench(
      nativeConfig = _.withDebugMetadata(true)
        .withIncrementalCompilation(false)
    )
