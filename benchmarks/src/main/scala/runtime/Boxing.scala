package runtime

import java.util.concurrent.TimeUnit

import scala.collection.mutable
import scala.compiletime.uninitialized

import scala.scalanative.libc.stdlib
import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*

import microbench.*

/** Primitive boxes the optimizer should be able to remove: values carried
 *  around a loop or across a branch as `Any`, as an erased type parameter, as
 *  an unsigned integer or as a `Ptr`. `hashMapLookup` is the control: its boxes
 *  are passed to calls that are not inlined, so they have to stay.
 */
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
class Boxing:
  @Param(Array("1024")) var size: Int = 0

  private var ints: Array[Int] = uninitialized
  private var bytes: Ptr[Byte] = uninitialized
  private var map: mutable.HashMap[Int, Int] = uninitialized

  @Setup(Level.Trial) def prepare(): Unit =
    ints = Array.tabulate(size)(i => (i * 31) ^ (i >>> 3))
    bytes = stdlib.malloc(size.toUSize)
    var i = 0
    while i < size do { bytes(i) = i.toByte; i += 1 }
    map = mutable.HashMap.empty
    ints.foreach(i => map(i) = i)

  @TearDown(Level.Trial) def release(): Unit = stdlib.free(bytes)

  /** Erased accumulator of an inlined generic fold. */
  @Benchmark def genericFoldInt(): Int = BoxingOps.fold(ints, 0)(_ + _)

  @Benchmark def genericFoldLong(): Long = BoxingOps.fold(ints, 0L)(_ + _)

  @Benchmark def genericIterateDouble(): Double =
    BoxingOps.iterate(size, 1.0)(x => x * 1.0001 + 0.5)

  /** A local of type `Any` updated in a loop. */
  @Benchmark def anyAccumulator(): Int =
    var acc: Any = 0
    var i = 0
    while i < ints.length do
      acc = acc.asInstanceOf[Int] + ints(i)
      i += 1
    acc.asInstanceOf[Int]

  /** A box stored in a non-escaping object mutated in a loop. */
  @Benchmark def cellAccumulator(): Long =
    val cell = new BoxingCell[Long](0L)
    var i = 0
    while i < ints.length do { cell.value = cell.value + ints(i); i += 1 }
    cell.value

  /** Boxes created on both sides of a branch, by a generic helper. */
  @Benchmark def branchMerge(): Int =
    var sum = 0
    var i = 0
    while i < ints.length do
      val x = ints(i)
      sum += BoxingOps.choose(x > 0, x, -x)
      i += 1
    sum

  /** Boxes created on both sides of a branch, as `Any`. */
  @Benchmark def branchMergeAny(): Int =
    var sum = 0
    var i = 0
    while i < ints.length do
      val x = ints(i)
      val v: Any = if (x & 1) == 0 then x >> 1 else x * 3 + 1
      sum += v.asInstanceOf[Int]
      i += 1
    sum

  /** Unsigned integers are boxes: an unsigned local in a loop. */
  @Benchmark def unsignedSum(): Long =
    var acc = 0.toUInt
    var i = 0
    while i < ints.length do { acc += ints(i).toUInt; i += 1 }
    acc.toLong

  @Benchmark def unsignedLongHash(): Long =
    var h = 1469598103934665603L.toULong
    var i = 0
    while i < ints.length do
      h = (h ^ ints(i).toULong) * 1099511628211L.toULong
      i += 1
    h.toLong

  /** A pointer incremented until it reaches the end, see #1529. */
  @Benchmark def pointerWalk(): Int =
    var ptr = bytes
    val end = bytes + size
    var sum = 0
    while ptr != end do
      sum += !ptr
      ptr += 1
    sum

  /** Collections API over primitives. */
  @Benchmark def arrayFoldLeft(): Int = ints.foldLeft(0)(_ + _)

  @Benchmark def rangeFoldLeft(): Int = (0 until size).foldLeft(0)(_ + _)

  /** Control: boxes passed to calls that are not inlined. */
  @Benchmark def hashMapLookup(): Int =
    var sum = 0
    var i = 0
    while i < ints.length do { sum += map.getOrElse(ints(i), 0); i += 1 }
    sum

/** Generic helpers small enough to be inlined: once the closure is known, all
 *  that is left of the erasure is the box travelling around the loop.
 */
private object BoxingOps:
  def fold[T](xs: Array[Int], z: T)(op: (T, Int) => T): T =
    var acc = z
    var i = 0
    while i < xs.length do { acc = op(acc, xs(i)); i += 1 }
    acc

  def choose[T](cond: Boolean, a: T, b: T): T = if cond then a else b

  def iterate[T](n: Int, z: T)(step: T => T): T =
    var acc = z
    var i = 0
    while i < n do { acc = step(acc); i += 1 }
    acc

private final class BoxingCell[T](var value: T)
