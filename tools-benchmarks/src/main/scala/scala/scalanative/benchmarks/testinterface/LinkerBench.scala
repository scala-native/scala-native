package scala.scalanative
package benchmarks

import java.nio.file.{Path, Files}
import java.util.Comparator
import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations._
import org.openjdk.jmh.annotations.Mode._

@Fork(1)
@State(Scope.Benchmark)
@BenchmarkMode(Array(AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
class LinkerBench {
  var workdir: Path = _

  @Setup(Level.Iteration)
  def setup(): Unit = {
    workdir = Files.createTempDirectory("linker-bench")
  }

  @TearDown(Level.Iteration)
  def cleanup(): Unit = {
    Files
      .walk(workdir)
      .sorted(Comparator.reverseOrder())
      .forEach(Files.delete)
    workdir = null
  }

  @Benchmark
  def link(): Unit = util.Scope { implicit scope =>
    val config = defaultConfig
      .withBaseDir(workdir)
      .withMainClass(Some(TestMain))

    val entries = build.ScalaNative.entries(config)
    val linked = build.ScalaNative.link(config, entries)
    assert(linked.unavailable.size == 0)
  }
}
