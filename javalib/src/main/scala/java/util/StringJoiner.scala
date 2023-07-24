// Ported from Scala.js commit: 57d71da dated: 2023-05-31
// This file has different implementations of add, merge, toString and length.

package java.util

import ScalaOps._

@inline
final class StringJoiner private (
    delimiter: String,
    prefix: String,
    suffix: String
) extends AnyRef {

  /** We need the delimiter of `other` for `merge`. */
  private val delimStr: String = delimiter

  /** The public constructor to be used from outside. */
  def this(
      delimiter: CharSequence,
      prefix: CharSequence,
      suffix: CharSequence
  ) =
    this(delimiter.toString(), prefix.toString(), suffix.toString())

  /** The custom value to return if empty, set by `setEmptyValue` (nullable). */
  private var emptyValue: String = null

  /** A list that holds the strings that have been added so far. */
  private val contents: List[CharSequence] = new ArrayList()

  /** Whether the string joiner is currently empty. */
  private def isEmpty: Boolean = contents.isEmpty()

  /** Alternate constructor with no prefix or suffix */
  def this(delimiter: CharSequence) = this(delimiter.toString(), "", "")

  def setEmptyValue(emptyValue: CharSequence): StringJoiner = {
    this.emptyValue = emptyValue.toString()
    this
  }

  override def toString(): String =
    if (isEmpty && emptyValue != null) emptyValue
    else contents.scalaOps.mkString(prefix, delimiter, suffix)

  def add(newElement: CharSequence): StringJoiner = {
    contents.add(if (newElement == null) "null" else newElement)
    this
  }

  def merge(other: StringJoiner): StringJoiner = {
    if (!other.isEmpty) { // if `other` is empty, `merge` has no effect
      contents.add(other.contents.scalaOps.mkString("", other.delimStr, ""))
    }
    this
  }

  def length(): Int =
    if (isEmpty && emptyValue != null) emptyValue.length()
    else if (isEmpty) prefix.length() + suffix.length()
    else
      prefix.length() + suffix.length() +
        delimiter.length() * (contents.size() - 1) +
        contents.scalaOps.foldLeft(0)((acc, part) => acc + part.length())
}
