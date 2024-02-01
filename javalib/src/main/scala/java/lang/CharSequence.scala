package java.lang

import java.util.{Spliterator, Spliterators}
import java.util.stream.{IntStream, StreamSupport}
import java.util.function.IntConsumer

trait CharSequence {

  /* sub classes, particularly those with fast access to an internal array,
   * should override the default implementations of chars() and
   * codePoints() to avoid the cost of the frequent charAt(index) calls
   * below.
   */

  def chars(): IntStream = {

    val characteristics =
      (Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED)

    val src = this
    val len = this.length()
    val spl = new Spliterators.AbstractIntSpliterator(
      len,
      characteristics
    ) {
      var index = 0

      /* Qualify the return type so that signatures match.
       * Otherwise, java.lang.Boolean is found because this file is
       * in the java.lang package.  Such knowledge was won by a few
       * wasted hours of debugging.
       */

      def tryAdvance(action: IntConsumer): scala.Boolean = {
        val remaining = len - index
        if (remaining <= 0) false
        else {
          action.accept(src.charAt(index).toInt)
          index += 1
          true
        }
      }
    }

    StreamSupport.intStream(spl, parallel = false)
  }

  def codePoints(): IntStream = {

    /* These characteristics may be incomplete.
     *
     * What _is_ certain is that they should not contain either SIZED or
     * SUBSIZED.
     *
     * this.length() gives a good upper bound estimate of the size, so
     * one would think that the spliterator should be SIZED. This
     * spliterators reason for existence is to combine surrogate pairs,
     * when found, into one code point. This means that the real size
     * is not known. It may be less than the estimate.
     *
     * Marking the spliterator as SIZED causes toArray() methods on
     * the resultant stream to have more slots than stream elements
     * when surrogate pairs are combined. This causes tests which
     * check that the array size and number of elements match to fail
     * and other woes. Just don't do it, Nancy.
     */

    val characteristics = Spliterator.ORDERED // No SIZED or SUBSIZED allowed

    val src = this
    val len = this.length()
    val spl = new Spliterators.AbstractIntSpliterator(
      len,
      characteristics
    ) {
      var index = 0

      var haveHighSurrogate = false
      var highSurrogate: Char = _

      /* qualify the return type so that signatures match.
       * See rationale in method chars() above.
       */

      def tryAdvance(action: IntConsumer): scala.Boolean = {
        val remaining = len - index
        if (remaining <= 0) false
        else {
          val ch = src.charAt(index)

          if (Character.isHighSurrogate(ch)) {
            if (!haveHighSurrogate && (remaining > 0)) {
              highSurrogate = ch
              haveHighSurrogate = true
            } else {
              haveHighSurrogate = false
              action.accept(highSurrogate.toInt)
            }
          } else if (Character.isLowSurrogate(ch)) {
            if (!haveHighSurrogate) {
              action.accept(ch.toInt)
            } else {
              haveHighSurrogate = false
              action.accept(Character.toCodePoint(highSurrogate, ch))
            }
          } else {
            action.accept(ch.toInt)
          }

          index += 1
          true
        }
      }
    }

    StreamSupport.intStream(spl, parallel = false)
  }

  def length(): scala.Int
  def charAt(index: scala.Int): scala.Char
  def subSequence(start: scala.Int, end: scala.Int): CharSequence
  def toString(): String
}
