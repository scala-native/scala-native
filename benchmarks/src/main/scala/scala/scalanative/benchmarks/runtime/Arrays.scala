package scala.scalanative.benchmarks.runtime

import java.util.concurrent.TimeUnit
import java.util as ju

import scala.compiletime.uninitialized

import microbench.*

/** Bulk array operations implemented in the runtime/javalib:
 *  `System.arraycopy`, `clone`, `Arrays.fill`/`equals`/`hashCode`, and
 *  reference-array stores (covariant store checks).
 */
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
class Arrays:
  @Param(Array("16", "1024", "65536")) var length: Int = 0

  private var bytes: Array[Byte] = uninitialized
  private var bytesDst: Array[Byte] = uninitialized
  private var bytesCopy: Array[Byte] = uninitialized
  private var ints: Array[Int] = uninitialized
  private var intsDst: Array[Int] = uninitialized
  private var refs: Array[AnyRef] = uninitialized
  private var refsDst: Array[AnyRef] = uninitialized
  private var strings: Array[String] = uninitialized
  private var stringsAsObjects: Array[AnyRef] = uninitialized

  @Setup(Level.Trial) def prepare(): Unit =
    val random = new ju.Random(1L)
    bytes = new Array[Byte](length)
    random.nextBytes(bytes)
    bytesDst = new Array[Byte](length)
    bytesCopy = bytes.clone()
    ints = Array.fill(length)(random.nextInt())
    intsDst = new Array[Int](length)
    strings = Array.tabulate(length)(_.toString)
    refs = strings.map(s => s: AnyRef)
    refsDst = new Array[AnyRef](length)
    // Static type Array[AnyRef], runtime type Array[String]: stores are checked.
    stringsAsObjects = new Array[String](length).asInstanceOf[Array[AnyRef]]

  @Benchmark def copyBytes(): Array[Byte] =
    System.arraycopy(bytes, 0, bytesDst, 0, length); bytesDst

  @Benchmark def copyInts(): Array[Int] =
    System.arraycopy(ints, 0, intsDst, 0, length); intsDst

  @Benchmark def copyRefs(): Array[AnyRef] =
    System.arraycopy(refs, 0, refsDst, 0, length); refsDst

  /** Overlapping copy within one array (memmove semantics). */
  @Benchmark def copyOverlapping(): Array[Int] =
    System.arraycopy(ints, 0, ints, 1, length - 1); ints

  /** `String[]` to `Object[]`-typed `String[]`: per-element store checks. */
  @Benchmark def copyCheckedRefs(): Array[AnyRef] =
    System.arraycopy(strings, 0, stringsAsObjects, 0, length); stringsAsObjects

  @Benchmark def cloneInts(): Array[Int] = ints.clone()

  @Benchmark def fillInts(): Array[Int] =
    ju.Arrays.fill(intsDst, 42); intsDst

  @Benchmark def equalsBytes(): Boolean = ju.Arrays.equals(bytes, bytesCopy)

  @Benchmark def hashCodeInts(): Int = ju.Arrays.hashCode(ints)

  /** Element-wise covariant stores in a loop. */
  @Benchmark def storeChecked(): Array[AnyRef] =
    var i = 0
    while i < length do { stringsAsObjects(i) = strings(i); i += 1 }
    stringsAsObjects
