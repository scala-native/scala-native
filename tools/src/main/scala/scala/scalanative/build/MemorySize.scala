package scala.scalanative.build

import scala.language.implicitConversions

/** An amount of memory, in bytes. Units are binary: a kilobyte is 1024 bytes.
 *
 *  {{{
 *  import scala.scalanative.build.MemorySize.syntax._
 *  val heap = 512.mb
 *  val same = MemorySize.parse("512m")
 *  }}}
 */
final class MemorySize private (val bytes: Long)
    extends Ordered[MemorySize]
    with Serializable {

  override def compare(that: MemorySize): Int =
    java.lang.Long.compare(bytes, that.bytes)

  override def equals(other: Any): Boolean = other match {
    case that: MemorySize => bytes == that.bytes
    case _                => false
  }

  override def hashCode(): Int = bytes.##

  /** The size in the notation accepted by [[MemorySize.parse]], using the
   *  largest unit that represents it exactly.
   */
  override def toString: String = {
    def exact(shift: Int) = bytes != 0 && (bytes & ((1L << shift) - 1)) == 0
    if (exact(30)) s"${bytes >> 30}g"
    else if (exact(20)) s"${bytes >> 20}m"
    else if (exact(10)) s"${bytes >> 10}k"
    else bytes.toString
  }
}

object MemorySize {

  /** @throws java.lang.IllegalArgumentException if `value` is negative */
  def bytes(value: Long): MemorySize = {
    require(value >= 0, s"Memory size cannot be negative: $value")
    new MemorySize(value)
  }

  /** @throws java.lang.IllegalArgumentException
   *    if `value` is negative or too large
   */
  def kb(value: Long): MemorySize = scaled(value, 10, "kb")

  /** @throws java.lang.IllegalArgumentException
   *    if `value` is negative or too large
   */
  def mb(value: Long): MemorySize = scaled(value, 20, "mb")

  /** @throws java.lang.IllegalArgumentException
   *    if `value` is negative or too large
   */
  def gb(value: Long): MemorySize = scaled(value, 30, "gb")

  /** Parses the notation of the `GC_*_SIZE` environment variables: decimal
   *  digits followed by an optional `k`, `m` or `g` (in either case).
   *
   *  @throws java.lang.IllegalArgumentException
   *    if `value` is not a valid size
   */
  def parse(value: String): MemorySize =
    tryParse(value).fold(
      msg => throw new IllegalArgumentException(msg),
      identity
    )

  private[build] def tryParse(value: String): Either[String, MemorySize] = {
    def invalid = Left(
      s"'$value' is not a valid memory size, expected digits with an optional k, m or g suffix"
    )
    val (digits, shift) = value.lastOption.map(_.toLower) match {
      case Some('k') => (value.init, 10)
      case Some('m') => (value.init, 20)
      case Some('g') => (value.init, 30)
      case _         => (value, 0)
    }
    if (digits.isEmpty || !digits.forall(c => c >= '0' && c <= '9')) invalid
    else
      try {
        val count = digits.toLong
        if (count > (Long.MaxValue >> shift)) invalid
        else Right(new MemorySize(count << shift))
      } catch { case _: NumberFormatException => invalid }
  }

  private def scaled(value: Long, shift: Int, unit: String): MemorySize = {
    require(
      value >= 0 && value <= (Long.MaxValue >> shift),
      s"Memory size is negative or too large: $value $unit"
    )
    new MemorySize(value << shift)
  }

  /** Adds `bytes`, `kb`, `mb` and `gb` to integers, for example `128.mb`. */
  trait Syntax {
    implicit def intToMemorySizeOps(value: Int): MemorySizeOps =
      new MemorySizeOps(value.toLong)
    implicit def longToMemorySizeOps(value: Long): MemorySizeOps =
      new MemorySizeOps(value)
  }

  /** `import scala.scalanative.build.MemorySize.syntax._` enables `128.mb`. */
  object syntax extends Syntax

  final class MemorySizeOps(private val value: Long) extends AnyVal {
    def bytes: MemorySize = MemorySize.bytes(value)
    def kb: MemorySize = MemorySize.kb(value)
    def mb: MemorySize = MemorySize.mb(value)
    def gb: MemorySize = MemorySize.gb(value)
  }
}
