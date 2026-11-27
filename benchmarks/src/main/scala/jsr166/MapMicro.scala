package jsr166

import java.util.concurrent.{ConcurrentHashMap, ConcurrentSkipListMap, TimeUnit}
import java.util as ju

import scala.compiletime.uninitialized

import microbench.*

/** One complete work cycle (11 operations per element) of JSR166
 *  `loops/MapMicroBenchmark` (Doug Lea, public domain, CC0): put half, get all
 *  (scrambled), put rest, iterate keys and values, mixed gets, replacing puts,
 *  removes. Timing is per cycle; JMH replaces the original self-timing loop.
 *
 *  The `size`s are the original's first sizes, chosen at halfway points of the
 *  HashMap default resizes (9 * 4^k).
 */
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
class MapMicro:
  @Param(
    Array(
      "HashMap",
      "LinkedHashMap",
      "TreeMap",
      "ConcurrentHashMap",
      "ConcurrentSkipListMap"
    )
  )
  var map: String = ""
  @Param(Array("Integer", "String")) var key: String = ""
  @Param(Array("36", "576", "9216")) var size: Int = 0

  private var items: Array[AnyRef] = uninitialized
  private var searches: Array[AnyRef] = uninitialized
  private var elementClass: Class[?] = uninitialized

  @Setup(Level.Trial) def prepare(): Unit =
    val random = new ju.Random(3122688L)
    items = key match
      case "Integer" =>
        elementClass = classOf[Integer]
        Array.tabulate[AnyRef](size)(i => Integer.valueOf(i))
      case "String" =>
        elementClass = classOf[String]
        Array.tabulate[AnyRef](size)(i => String.valueOf(i))
    scramble(items, random)
    searches = items.clone()
    scramble(searches, random)

  private def scramble(a: Array[AnyRef], random: ju.Random): Unit =
    var i = a.length
    while i > 1 do
      val j = random.nextInt(i)
      i -= 1
      val t = a(i); a(i) = a(j); a(j) = t

  private def newMap(): ju.Map[AnyRef, AnyRef] = map match
    case "HashMap"               => new ju.HashMap()
    case "LinkedHashMap"         => new ju.LinkedHashMap()
    case "TreeMap"               => new ju.TreeMap()
    case "ConcurrentHashMap"     => new ConcurrentHashMap()
    case "ConcurrentSkipListMap" => new ConcurrentSkipListMap()

  @Benchmark def cycle(): Long =
    val m = newMap()
    val ins = items
    val keys = searches
    val len = size
    val half = len / 2
    val quarter = half / 2
    var sum = 0
    var checkSum = 0L
    def touch(v: AnyRef): Unit = if elementClass.isInstance(v) then sum += 1

    var i = 0
    while i < half do
      val x = ins(i); if m.put(x, x) == null then sum += 1; i += 1
    checkSum += sum ^ (sum << 1)
    sum += len - half
    i = 0
    while i < len do { touch(m.get(keys(i))); i += 1 }
    checkSum += sum ^ (sum << 2)
    i = half
    while i < len do
      val x = ins(i); if m.put(x, x) == null then sum += 1; i += 1
    checkSum += sum ^ (sum << 3)
    val ks = m.keySet().iterator()
    while ks.hasNext() do touch(ks.next())
    checkSum += sum ^ (sum << 4)
    val vs = m.values().iterator()
    while vs.hasNext() do touch(vs.next())
    checkSum += sum ^ (sum << 5)
    i = len - 1
    while i >= 0 do { touch(m.get(keys(i))); i -= 1 }
    checkSum += sum ^ (sum << 6)
    i = 0
    while i < len do { touch(m.get(ins(i))); i += 1 }
    checkSum += sum ^ (sum << 7)
    i = 0
    while i < len do
      val x = keys(i); if m.put(x, ins(i)) eq x then sum += 1; i += 1
    checkSum += sum ^ (sum << 8)
    i = 0
    while i < len do
      if ins(i) eq m.get(keys(i)) then sum += 1; i += 1
    checkSum += sum ^ (sum << 9)
    i = len - 1
    while i >= 0 do { touch(m.get(ins(i))); i -= 1 }
    checkSum += sum ^ (sum << 10)
    i = len - 1
    while i >= 0 do
      if ins(i) eq m.get(keys(i)) then sum += 1; i -= 1
    checkSum += sum ^ (sum << 11)
    i = 0
    while i < quarter do
      if m.remove(keys(i)) != null then sum += 1; i += 1
    i = 0
    while i < quarter do
      val x = keys(i); if m.put(x, x) == null then sum += 1; i += 1
    m.clear()
    sum += len - (quarter * 2)
    checkSum += sum ^ (sum << 12)
    // The original's sanity check: every operation hit.
    if sum != len * 11 then
      throw new AssertionError(s"$map/$key/$size: sum $sum != ${len * 11}")
    checkSum
