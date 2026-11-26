package scala.scalanative.benchmarks.runtime

import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

import scala.compiletime.uninitialized

import microbench.*

/** `java.lang.String` / `StringBuilder` / number formatting paths of javalib.
 */
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
class Strings:
  /** Length of the test strings. */
  @Param(Array("16", "1024")) var length: Int = 0

  private var ascii: String = uninitialized
  private var asciiCopy: String = uninitialized
  private var unicode: String = uninitialized
  private var csv: String = uninitialized
  private var utf8: Array[Byte] = uninitialized
  private var number = 0
  private var double = 0.0

  @Setup(Level.Trial) def prepare(): Unit =
    ascii = ("abcdefghijklmnopqrstuvwxyz" * (length / 26 + 1)).take(length)
    asciiCopy = new String(ascii.toCharArray)
    unicode = ("zażółć gęślą jaźń ∑ 😀 " * (length / 20 + 1)).take(length)
    csv = (0 until (length / 4 max 2)).mkString(",")
    utf8 = unicode.getBytes(StandardCharsets.UTF_8)
    number = 123456789
    double = Math.PI * 1e7

  /** Fresh String each call, so the cached hash is never reused. */
  @Benchmark def hashCodeFresh(): Int = new String(ascii.toCharArray).hashCode

  @Benchmark def equalsSameContent(): Boolean = ascii == asciiCopy

  @Benchmark def indexOf(): Int = ascii.indexOf("xyz", length / 2)

  @Benchmark def builderAppend(): String =
    val sb = new java.lang.StringBuilder()
    var i = 0
    while i < length do { sb.append(i).append(','); i += 8 }
    sb.toString

  @Benchmark def interpolation(): String =
    s"value=$number name=$ascii x=${number + 1}"

  @Benchmark def intToString(): String = Integer.toString(number)

  @Benchmark def doubleToString(): String = java.lang.Double.toString(double)

  @Benchmark def parseDouble(): Double =
    java.lang.Double.parseDouble("3141592.6535897932")

  @Benchmark def split(): Array[String] = csv.split(",")

  @Benchmark def toUpperCase(): String = ascii.toUpperCase()

  @Benchmark def encodeUtf8(): Array[Byte] =
    unicode.getBytes(StandardCharsets.UTF_8)

  @Benchmark def decodeUtf8(): String = new String(utf8, StandardCharsets.UTF_8)
