// Ported from Scala.js commit: 57d71da dated: 2023-05-31
// Extensively re-written for Scala Native.

package java.util

final class StringJoiner private (
    delimiter: String,
    prefixLength: Integer,
    suffix: String
) extends AnyRef {

  def this(delimiter: CharSequence) = this(delimiter.toString(), 0, "")

  def this(
      delimiter: CharSequence,
      prefix: CharSequence,
      suffix: CharSequence
  ) = {
    this(delimiter.toString(), prefix.length(), suffix.toString())
    if (prefixLength > 0)
      builder.append(prefix)
  }

  private val delimLength = delimiter.length()

  /* Avoid early builder enlargeBuffer() calls.
   * Add an arbitrary guestimate > default 16 excess capacity.
   */
  private val builder =
    new java.lang.StringBuilder(prefixLength + 40 + suffix.length())

  /* The custom value to return if empty, set by `setEmptyValue` (nullable).
   */
  private var emptyValue: String = null

  /* "true" before the first add(), even of "",  or merge() of non-empty
   * StringJoiner. See JDK StringJoiner documentation.
   *
   * A tricky bit:
   *   Adding an initial empty string ("") will set isEmpty to "false" but
   *   will not change builder.length(). Use former to determine when to
   *   use emptyValue or not.
   */
  private var isEmpty = true

  private def appendStemTo(other: StringJoiner) = {
    if (!isEmpty) // builder contains more than prefix, possibly only "".
      other.add(this.builder.substring(prefixLength))
  }

  def setEmptyValue(emptyValue: CharSequence): StringJoiner = {
    this.emptyValue = emptyValue.toString()
    this
  }

  override def toString(): String = {
    if (isEmpty && (emptyValue != null)) emptyValue
    else {
      if (suffix.length == 0)
        builder.toString()
      else { // avoid an extra String allocation.
        val len = builder.length()
        builder.append(suffix)
        val s = builder.toString()
        builder.setLength(len)
        s
      }
    }
  }

  def add(newElement: CharSequence): StringJoiner = {
    if (isEmpty)
      isEmpty = false
    else if (delimLength > 0)
      builder.append(delimiter)

    builder.append(if (newElement == null) "null" else newElement)
    this
  }

  def merge(other: StringJoiner): StringJoiner = {
    other.appendStemTo(this)
    this
  }

  def length(): Int = {
    if (isEmpty && (emptyValue != null)) emptyValue.length()
    else builder.length() + suffix.length()
  }

}
