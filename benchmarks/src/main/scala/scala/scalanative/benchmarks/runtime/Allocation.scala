package scala.scalanative.benchmarks.runtime

import java.util.concurrent.TimeUnit

import scala.compiletime.uninitialized

import microbench.*

/** Allocation fast path and GC throughput. To compare collectors, build with a
 *  different `Jmh / nativeConfig ~= (_.withGC(GC.boehm))`, or vary the heap
 *  with `GC_INITIAL_HEAP_SIZE` / `GC_MAXIMUM_HEAP_SIZE`.
 */
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
class Allocation:
  /** Array length in elements. */
  @Param(Array("4", "256", "16384")) var length: Int = 0

  /** Number of live cells kept around, so collections must trace something. */
  @Param(Array("0", "100000")) var retained: Int = 0

  private var live: Array[Cell] = uninitialized
  private var cursor = 0
  private var seed = 1

  @Setup(Level.Trial) def prepare(): Unit =
    live = Array.tabulate(retained)(i => new Cell(i, null))

  @Benchmark def smallObject(): Point =
    seed += 1
    new Point(seed, seed + 1)

  @Benchmark def intArray(): Array[Int] = new Array[Int](length)

  @Benchmark def refArray(): Array[AnyRef] = new Array[AnyRef](length)

  /** Builds a short linked list per call: young garbage with internal pointers.
   */
  @Benchmark def linkedList(): Cell =
    var head: Cell = null
    var i = 0
    while i < 16 do
      head = new Cell(i, head)
      i += 1
    head

  /** Overwrites retained slots: old-to-young pointers (write barrier /
   *  remembered set).
   */
  @Benchmark def mutateRetained(): Cell =
    if live.length == 0 then new Cell(seed, null)
    else
      cursor += 1
      if cursor >= live.length then cursor = 0
      val fresh = new Cell(cursor, null)
      live(cursor).next = fresh
      fresh

private final class Point(val x: Int, val y: Int)
private final class Cell(var value: Int, var next: Cell)
