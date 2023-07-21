// Ported from Scala.js commit: 57d71da dated: 2023-05-31
// This file has many differences, using CharSequence instead of String,
// and different implementations of add, merge, toString and length.

package java.util

import java.lang.StringBuilder

@inline
final class StringJoiner private (
    delimiter: CharSequence,
    prefix: CharSequence,
    suffix: CharSequence
) extends AnyRef {

  /** None of the arguments should be null. */
  Objects.requireNonNull(delimiter, "The delimiter must not be null")
  Objects.requireNonNull(prefix, "The prefix must not be null")
  Objects.requireNonNull(suffix, "The suffix must not be null")

  /** We need the fields to be immutable. */
  private val delimStr: String = delimiter.toString()
  private val prefixStr: String = prefix.toString()
  private val suffixStr: String = suffix.toString()

  /** The custom value to return if empty, set by `setEmptyValue` (nullable). */
  private var emptyValue: String = null

  /** A list that holds (in reverse) strings that have been added so far. */
  private var contents: scala.collection.immutable.List[CharSequence] = Nil

  /** Whether the string joiner is currently empty. */
  private var isEmpty: Boolean = true

  /** Alternate constructor with no prefix or suffix */
  def this(delimiter: CharSequence) = this(delimiter, "", "")

  def setEmptyValue(emptyValue: CharSequence): StringJoiner = {
    this.emptyValue = emptyValue.toString()
    this
  }

  /** Utility function used in build and length. Calculates the length of
   *  strings in the list, plus the delimiters in between them.
   */
  private def lengthOf(
      parts: scala.collection.immutable.List[CharSequence],
      delim: CharSequence
  ): Int = {
    val delimLength = delim.length
    parts match {
      case Nil => 0
      case head :: tail =>
        tail.foldLeft(head.length)((acc, part) =>
          acc + delimLength + part.length
        )
    }
  }

  /** Utility function used in toString and merge. Builds up the "middle" part
   *  of a join, without prefix, suffix, emptyValue. We assume the parts are
   *  given in reverse order.
   */
  private def build(
      parts: scala.collection.immutable.List[CharSequence],
      delim: CharSequence
  ): CharSequence = {
    // allocate exactly as much as required to skip internal buffer resize
    val size = lengthOf(parts, delim)
    val builder = new StringBuilder(size)

    parts.reverse match {
      case Nil =>
      case head :: tail => {
        builder.append(head)
        tail.foreach(part => builder.append(delim).append(part))
      }
    }
    builder
  }

  override def toString(): String =
    if (isEmpty && emptyValue != null) emptyValue
    else {
      // allocate exactly as much as required to skip internal buffer resize
      val builder = new StringBuilder(length())

      builder.append(prefixStr)
      builder.append(build(contents, delimStr))
      builder.append(suffixStr)

      builder.toString()
    }

  def add(newElement: CharSequence): StringJoiner = {
    contents ::= (if (newElement == null) "null" else newElement)
    isEmpty = false
    this
  }

  def merge(other: StringJoiner): StringJoiner = {
    if (!other.isEmpty) { // if `other` is empty, `merge` has no effect
      contents ::= build(other.contents, other.delimStr)
      isEmpty = false
    }
    this
  }

  def length(): Int = {
    val baseLength =
      if (isEmpty && emptyValue != null) emptyValue.length
      else prefixStr.length + suffixStr.length
    baseLength + lengthOf(contents, delimStr)
  }
}
