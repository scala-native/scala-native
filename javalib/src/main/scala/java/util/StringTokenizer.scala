package java.util

import scala.annotation.tailrec

class StringTokenizer(
    string: String,
    private var delimiters: String,
    returnDelimiters: Boolean
) extends java.util.Enumeration[Object] {

  def this(string: String) = this(string, " \t\n\r\f", false)
  def this(string: String, delimeters: String) =
    this(string, delimeters, false)

  if (string == null) {
    throw new NullPointerException()
  }

  private var position = 0

  def countTokens(): Int = {
    var count = 0
    var inToken = false
    val length = string.length
    for (i <- position until length) {
      if (delimiters.indexOf(string.charAt(i), 0) >= 0) {
        if (returnDelimiters)
          count += 1
        if (inToken) {
          count += 1
          inToken = false
        }
      } else {
        inToken = true
      }
    }
    if (inToken)
      count += 1
    count
  }

  def hasMoreElements(): Boolean = hasMoreTokens()

  def hasMoreTokens(): Boolean = {
    if (delimiters == null)
      throw new NullPointerException()

    @tailrec
    def hasNonDelim(pos: Int, len: Int): Boolean = {
      if (pos == len) false
      else if (delimiters.indexOf(string.charAt(pos), 0) == -1) true
      else hasNonDelim(pos + 1, len)
    }

    val length = string.length
    if (position >= length) false
    else if (returnDelimiters) true
    else hasNonDelim(position, length)
  }

  def nextElement(): Object = nextToken()

  def nextToken(): String = {
    if (delimiters == null) {
      throw new NullPointerException()
    }
    var i = position
    val length = string.length

    if (i < length) {
      if (returnDelimiters) {
        if (delimiters.indexOf(string.charAt(i), 0) >= 0) {
          return String.valueOf(string.charAt({ position += 1; position - 1 }))
        }
        position += 1
        while (position < length) {
          if (delimiters.indexOf(string.charAt(position), 0) >= 0)
            return string.substring(i, position)
          position += 1
        }
        return string.substring(i)
      }

      while (i < length && delimiters.indexOf(string.charAt(i), 0) >= 0) i += 1

      position = i

      if (i < length) {
        position += 1
        while (position < length) {
          if (delimiters.indexOf(string.charAt(position), 0) >= 0)
            return string.substring(i, position)
          position += 1
        }
        return string.substring(i)
      }
    }
    throw new NoSuchElementException()
  }

  def nextToken(delims: String): String = {
    if (delims == null)
      throw new NullPointerException()

    delimiters = delims
    nextToken()
  }

}
