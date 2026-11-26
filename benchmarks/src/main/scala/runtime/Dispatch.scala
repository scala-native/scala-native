package runtime

import java.util.concurrent.TimeUnit

import scala.compiletime.uninitialized

import microbench.*

/** Call-site polymorphism: virtual (vtable) vs interface (itable) dispatch,
 *  with 1, 2 or 8 receiver classes cycling through one call site, plus type
 *  tests, casts and pattern matches over the same receivers.
 */
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
class Dispatch:
  /** Number of distinct receiver classes at the call site. */
  @Param(Array("1", "2", "8")) var morphism: Int = 0

  private var shapes: Array[Shape] = uninitialized
  private var traits: Array[HasArea] = uninitialized
  private var anys: Array[Any] = uninitialized

  @Setup(Level.Trial) def prepare(): Unit =
    val random = new java.util.Random(7L)
    shapes = Array.tabulate(1024) { i =>
      (random.nextInt(morphism): @annotation.switch) match
        case 0 => new S0(i)
        case 1 => new S1(i)
        case 2 => new S2(i)
        case 3 => new S3(i)
        case 4 => new S4(i)
        case 5 => new S5(i)
        case 6 => new S6(i)
        case _ => new S7(i)
    }
    traits = shapes.map(_.asInstanceOf[HasArea])
    anys = shapes.map(s => s: Any)

  @Benchmark def virtualCall(): Int =
    var sum = 0
    var i = 0
    while i < shapes.length do { sum += shapes(i).area; i += 1 }
    sum

  @Benchmark def interfaceCall(): Int =
    var sum = 0
    var i = 0
    while i < traits.length do { sum += traits(i).areaT; i += 1 }
    sum

  /** `isInstanceOf` against a trait: an interface subtype test. */
  @Benchmark def traitTypeTest(): Int =
    var n = 0
    var i = 0
    while i < anys.length do {
      if anys(i).isInstanceOf[HasArea] then n += 1; i += 1
    }
    n

  /** `isInstanceOf` against one final class: an exact class test. */
  @Benchmark def classTypeTest(): Int =
    var n = 0
    var i = 0
    while i < anys.length do { if anys(i).isInstanceOf[S1] then n += 1; i += 1 }
    n

  @Benchmark def checkedCast(): Int =
    var sum = 0
    var i = 0
    while i < anys.length do { sum += anys(i).asInstanceOf[Shape].area; i += 1 }
    sum

  @Benchmark def patternMatch(): Int =
    var sum = 0
    var i = 0
    while i < anys.length do
      sum += (anys(i) match
        case s: S0      => 1
        case s: S1      => 2
        case s: S2      => 3
        case s: S3      => 4
        case s: HasArea => s.areaT
        case _          => 0)
      i += 1
    sum

private abstract class Shape:
  def area: Int
private trait HasArea:
  def areaT: Int

private final class S0(v: Int) extends Shape with HasArea {
  def area = v; def areaT = v
}
private final class S1(v: Int) extends Shape with HasArea {
  def area = v + 1; def areaT = v + 1
}
private final class S2(v: Int) extends Shape with HasArea {
  def area = v * 2; def areaT = v * 2
}
private final class S3(v: Int) extends Shape with HasArea {
  def area = v - 3; def areaT = v - 3
}
private final class S4(v: Int) extends Shape with HasArea {
  def area = v ^ 4; def areaT = v ^ 4
}
private final class S5(v: Int) extends Shape with HasArea {
  def area = v | 5; def areaT = v | 5
}
private final class S6(v: Int) extends Shape with HasArea {
  def area = v << 1; def areaT = v << 1
}
private final class S7(v: Int) extends Shape with HasArea {
  def area = v >> 1; def areaT = v >> 1
}
