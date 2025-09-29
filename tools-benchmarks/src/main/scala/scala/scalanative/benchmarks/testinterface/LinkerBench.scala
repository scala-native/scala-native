package scala.scalanative
package benchmarks

import java.nio.file.{Files, Path}
import java.util.Comparator
import java.util.concurrent.TimeUnit

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._

import org.openjdk.jmh.annotations.Mode._
import org.openjdk.jmh.annotations._

@Fork(1)
@State(Scope.Benchmark)
@BenchmarkMode(Array(AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
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
    val link = build.ScalaNative.link(config, entries)
    Await.result(link, Duration.Inf)
  }
}
