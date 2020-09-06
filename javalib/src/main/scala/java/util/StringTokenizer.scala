package java.util

class StringTokenizer(
  str: String,
  private var delim: String,
  returnDelims: Boolean
) extends java.util.Enumeration[Object] {

  def this(str: String) = this(str, " \t\n\r\f", false)
  def this(str: String, delim: String) = this(str, delim, false)

  private var position: Int = 0
  private val length: Int = str.length

  def countTokens(): Int = {
    var count: Int = 0
    var inToken: Boolean = false
    var i: Int = position

    while(i < length) {
      if (isDelim(str.charAt(i))) {
        if (returnDelims)
          count += 1

        if (inToken) {
          count += 1
          inToken = false
        }
      } else {
        inToken = true
      }

      i += 1
    }

    if (inToken)
      count += 1

    count
  }

  def hasMoreElements(): Boolean = hasMoreTokens()

  def hasMoreTokens(): Boolean = {
    if (position >= length) false
    else if (returnDelims) true
    else {
      var hasDelim: Boolean = false
      var i: Int = position
      while (i < length && !hasDelim) {
        if (isDelim(str.charAt(i))) hasDelim = true
        i += 1
      }

      hasDelim
    }
  }

  def nextElement(): Object = nextToken()

  def nextToken(): String = {
    ensureAvailable()

    if (returnDelims && nextIsDelim) {
      val ret = String.valueOf(currentChar)
      position += 1
      ret
    } else {
      // Skip consecutive delims
      while (position < length && nextIsDelim) position += 1

      ensureAvailable()

      val start: Int = position
      while (position < length && !nextIsDelim) position += 1
      str.substring(start, position)
    }
  }

  def nextToken(delim: String): String = {
    this.delim = delim
    nextToken()
  }

  private def ensureAvailable(): Unit = {
    if (position >= length)
      throw new NoSuchElementException()
  }

  private def nextIsDelim: Boolean = isDelim(currentChar)
  @inline private def isDelim(ch: Char): Boolean = delim.indexOf(ch, 0) >= 0
  @inline private def currentChar: Char = str.charAt(position)
}
