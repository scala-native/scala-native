package scala.scalanative
package benchmarks

import java.nio.file.{Path, Files}
import java.util.Comparator
import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations._
import org.openjdk.jmh.annotations.Mode._

import scala.scalanative.build._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

@Fork(1)
@State(Scope.Benchmark)
@BenchmarkMode(Array(AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
abstract class OptimizerBench(mode: build.Mode) {
  var config: Config = _
  var linked: linker.Result = _

  @Setup(Level.Trial)
  def setup(): Unit = {
    val workdir = Files.createTempDirectory("optimize-bench")
    config = defaultConfig
      .withBaseDir(workdir)
      .withMainClass(Some(TestMain))
      .withCompilerConfig(_.withMode(mode))

    val entries = build.ScalaNative.entries(config)
    util.Scope { implicit scope =>
      linked = Await.result(
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
    linked = null
    config = null
  }

  @Benchmark
  def optimize(): Unit = {
    val optimize = ScalaNative.optimize(config, linked)
    val optimized = Await.result(optimize, Duration.Inf)
    assert(optimized.unavailable.size == 0)
  }
}

class OptimizeDebug extends OptimizerBench(build.Mode.debug)
class OptimizeReleaseFast extends OptimizerBench(build.Mode.releaseFast)
// Commented out becouse of long build times ~13 min
// class OptimizeReleaseFull extends OptimizerBench(build.Mode.releaseFull)
