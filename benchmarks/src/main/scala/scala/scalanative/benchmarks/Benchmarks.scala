package scala.scalanative.benchmarks

import microbench.{run, suite}

/** Entry point of the benchmarks (`benchmarks/run <JMH options>`).
 *
 *  Scala Native cannot enumerate classes at runtime, so every benchmark class
 *  is listed here; `suite[T]` derives its harness at compile time.
 *
 *  TODO: drop this list for the sbt-microbench plugin's discovery once the
 *  build is updated to sbt 2.1.2, see `benchmarks` in project/Build.scala.
 */
@main def Benchmarks(args: String*): Unit = run(args)(
  suite[classic.GCBench],
  suite[issues.GameOfLife],
  suite[jsr166.MapMicro],
  suite[jsr166.Sorting],
  suite[runtime.Allocation],
  suite[runtime.Arrays],
  suite[runtime.Dispatch],
  suite[runtime.Exceptions],
  suite[runtime.Monitors],
  suite[runtime.Strings]
)
