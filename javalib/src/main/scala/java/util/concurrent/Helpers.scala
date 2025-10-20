/*
 * Written by Martin Buchholz with assistance from members of JCP
 * JSR-166 Expert Group and released to the public domain, as
 * explained at http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent
import java.util

/** Shared implementation code for java.util.concurrent. */
object Helpers {

  /** An implementation of Collection.toString() suitable for classes with
   *  locks. Instead of holding a lock for the entire duration of toString(), or
   *  acquiring a lock for each call to Iterator.next(), we hold the lock only
   *  during the call to toArray() (less disruptive to other threads accessing
   *  the collection) and follows the maxim "Never call foreign code while
   *  holding a lock".
   */
  private[concurrent] def collectionToString(c: util.Collection[?]): String = {
    val a = c.toArray()
    val size = a.length
    if (size == 0) return "[]"
    var charLength = 0
    // Replace every array element with its string representation
    for (i <- 0 until size) {
      val e = a(i)
      // Extreme compatibility with AbstractCollection.toString()
      val s =
        if (e eq c) "(this Collection)"
        else objectToString(e)
      a(i) = s
      charLength += s.length
    }
    toString(a, size, charLength)
  }

  /** Like Arrays.toString(), but caller guarantees that size > 0, each element
   *  with index 0 <= i < size is a non-null String, and charLength is the sum
   *  of the lengths of the input Strings.
   */
  private[concurrent] def toString(
      a: Array[AnyRef],
      size: Int,
      charLength: Int
  ) = { // assert a != null;
    // assert size > 0;
    // Copy each string into a perfectly sized char[]
    // Length of [ , , , ] == 2 * size
    val chars = new Array[Char](charLength + 2 * size)
    chars(0) = '['
    var j = 1
    for (i <- 0 until size) {
      if (i > 0) {
        chars({ j += 1; j - 1 }) = ','
        chars({ j += 1; j - 1 }) = ' '
      }
      val s = a(i).asInstanceOf[String]
      val len = s.length
      s.getChars(0, len, chars, j)
      j += len
    }
    chars(j) = ']'
    // assert j == chars.length - 1;
    new String(chars)
  }

  /** Optimized form of: key + "=" + val */
  private[concurrent] def mapEntryToString(key: Any, `val`: Any) = {
    val k = objectToString(key)
    val v = objectToString(`val`)
    val klen = k.length()
    val vlen = v.length()
    val chars = new Array[Char](klen + vlen + 1)
    k.getChars(0, klen, chars, 0)
    chars(klen) = '='
    v.getChars(0, vlen, chars, klen + 1)
    new String(chars)
  }
  private def objectToString(x: Any) = { // Extreme compatibility with StringBuilder.append(null)
    if (x == null) "null"
    else x.toString
  }
}
