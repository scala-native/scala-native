package runtime

import java.util.concurrent.TimeUnit

import scala.util.boundary
import scala.util.boundary.break
import scala.util.control.NoStackTrace

import microbench.*

/** Throw/catch cost: with and without stack-trace capture, unwinding through
 *  `depth` frames, and `boundary`/`break` (Scala 3 non-local exit).
 */
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
class Exceptions:
  @Param(Array("1", "16")) var depth: Int = 0
  private var seed = 0

  private def throwFast(d: Int, v: Int): Int =
    if d <= 0 then throw new Fast(v) else throwFast(d - 1, v) + 1

  private def throwSlow(d: Int, v: Int): Int =
    if d <= 0 then throw new Slow(v) else throwSlow(d - 1, v) + 1

  private def tryFinally(d: Int, v: Int, acc: Array[Int]): Int =
    if d <= 0 then throw new Fast(v)
    else
      try tryFinally(d - 1, v, acc) + 1
      finally acc(0) += 1

  @Benchmark def noStackTrace(): Int =
    seed += 1
    try throwFast(depth, seed)
    catch case e: Fast => e.value

  @Benchmark def withStackTrace(): Int =
    seed += 1
    try throwSlow(depth, seed)
    catch case e: Slow => e.value

  /** Unwinding runs a `finally` block in every frame. */
  @Benchmark def throughFinally(): Int =
    seed += 1
    val acc = Array(0)
    try tryFinally(depth, seed, acc)
    catch case e: Fast => e.value + acc(0)

  /** `try` that never throws: the cost of having a handler on the hot path. */
  @Benchmark def tryNoThrow(): Int =
    seed += 1
    try seed * 3
    catch case e: Fast => -1

  /** `break` lexically inside its `boundary`: compiled to a local jump, no
   *  throw.
   */
  @Benchmark def boundaryLocalBreak(): Int =
    seed += 1
    boundary:
      var i = 0
      while true do
        if i == depth then break(seed + i)
        i += 1
      -1

  private def breakFrom(d: Int, v: Int)(using boundary.Label[Int]): Int =
    if d <= 0 then break(v) else breakFrom(d - 1, v) + 1

  /** `break` from `depth` frames down: a non-local exit, implemented with an
   *  exception.
   */
  @Benchmark def boundaryNonLocalBreak(): Int =
    seed += 1
    boundary:
      breakFrom(depth, seed)

private final class Fast(val value: Int)
    extends RuntimeException
    with NoStackTrace
private final class Slow(val value: Int) extends RuntimeException
