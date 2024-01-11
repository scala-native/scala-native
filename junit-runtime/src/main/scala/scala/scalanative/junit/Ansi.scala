package scala.scalanative
package junit

// Ported from Scala.js

private[junit] object Ansi {

  private final val NORMAL = "\u001B[0m"

  def c(s: String, colorSequence: String): String =
    if (colorSequence == null) s
    else colorSequence + s + NORMAL

  def filterAnsi(s: String): String = {
    if (s == null) {
      null
    } else {
      var r: String = ""
      val len = s.length
      var i = 0
      while (i < len) {
        val c = s.charAt(i)
        if (c == '\u001B') {
          i += 1
          while (i < len && s.charAt(i) != 'm')
            i += 1
        } else {
          r += c
        }
        i += 1
      }
      r
    }
  }

  final val RED = "\u001B[31m"
  final val YELLOW = "\u001B[33m"
  final val BLUE = "\u001B[34m"
  final val MAGENTA = "\u001B[35m"
  final val CYAN = "\u001B[36m"
}
