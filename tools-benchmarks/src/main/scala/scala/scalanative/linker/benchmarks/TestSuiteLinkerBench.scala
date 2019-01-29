package scala.scalanative.linker.benchmarks

import java.nio.file.{Path, Paths, Files}
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

import scala.scalanative.build
import scala.scalanative.internal.build.TestSuiteBuildInfo

import org.openjdk.jmh.annotations._
import org.openjdk.jmh.annotations.Mode.SampleTime

@State(Scope.Benchmark)
abstract class TestSuiteLinkerBench
    extends scala.scalanative.linker.ReachabilitySuite {
  var workdir: Path = _

  @Setup(Level.Trial) def spawn(): Unit = {
    workdir = Files.createTempDirectory("linker-benchmarks")
  }

  @Benchmark
  def linkTestSuite(): Unit = {
    val classpath = TestSuiteBuildInfo.fullTestSuiteClasspath.map(_.toPath)
    val config = build.Config.empty
      .withWorkdir(workdir)
      .withClassPath(classpath)
      .withMainClass("scala.scalanative.testinterface.TestMain$")
      .withNativelib(classpath.find(_.toString.contains("nativelib")).head)
    val entries = build.ScalaNative.entries(config)
    val linked  = build.ScalaNative.link(config, entries)
    assert(linked.unavailable.size == 0)
  }
}

@Fork(1)
@State(Scope.Benchmark)
@BenchmarkMode(Array(SampleTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 30, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
class HotTestSuiteLinkerBench extends TestSuiteLinkerBench
