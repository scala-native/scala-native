package jsr166

import java.util.concurrent.TimeUnit
import java.util as ju

import scala.compiletime.uninitialized

import microbench.*

/** Sorting, after JSR166 `loops/ArraysSort`, `BoxedLongSort`, `ScalarLongSort`
 *  and `PriorityQueueSort` (Doug Lea, public domain, CC0). Each invocation
 *  sorts a fresh copy of the same random input.
 */
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
class Sorting:
  @Param(Array("1000", "100000")) var size: Int = 0

  private var longs: Array[Long] = uninitialized
  private var boxed: Array[java.lang.Long] = uninitialized
  private var strings: Array[String] = uninitialized

  @Setup(Level.Trial) def prepare(): Unit =
    val random = new ju.Random(42L)
    longs = Array.fill(size)(random.nextLong())
    boxed = longs.map(java.lang.Long.valueOf)
    strings = longs.map(java.lang.Long.toString(_, 36))

  @Benchmark def scalarLong(): Array[Long] =
    val a = longs.clone()
    ju.Arrays.sort(a)
    a

  @Benchmark def boxedLong(): Array[java.lang.Long] =
    val a = boxed.clone()
    ju.Arrays.sort(a.asInstanceOf[Array[AnyRef]])
    a

  @Benchmark def stringArray(): Array[String] =
    val a = strings.clone()
    ju.Arrays.sort(a.asInstanceOf[Array[AnyRef]])
    a

  @Benchmark def scalaSorted(): Array[Long] = longs.sorted

  @Benchmark def priorityQueue(): Long =
    val q = new ju.PriorityQueue[java.lang.Long](size)
    var i = 0
    while i < size do { q.add(boxed(i)); i += 1 }
    var last = Long.MinValue
    while !q.isEmpty() do last = q.poll()
    last
