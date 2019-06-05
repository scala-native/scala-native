package scala.scalanative
package regex

// Utilities to make JUnit act a little more like Go's "testing" package.
object GoTestUtils {
  def len[A](array: Array[A]): Int =
    if (array == null) 0
    else array.length

  def len(array: Array[Int]): Int =
    if (array == null) 0
    else array.length

  def len(array: Array[Byte]): Int =
    if (array == null) 0
    else array.length

  def utf8(s: String): Array[Byte] = s.getBytes("UTF-8")

  // Beware: logically this operation can fail, but Java doesn't detect it.
  def fromUTF8(b: Array[Byte]): String = new String(b, "UTF-8")

  // Convert |idx16|, which are Java (UTF-16) string indices, into the
  // corresponding indices in the UTF-8 encoding of |text|.
  def utf16IndicesToUtf8(idx16: Array[Int], text: String): Array[Int] = {
    val idx8 = new Array[Int](idx16.length)
    var i    = 0
    while (i < idx16.length) {
      idx8(i) =
        if (idx16(i) == -1) -1
        else text.substring(0, idx16(i)).getBytes("UTF-8").length // yikes

      {
        i += 1; i
      }
    }
    idx8
  }
}
