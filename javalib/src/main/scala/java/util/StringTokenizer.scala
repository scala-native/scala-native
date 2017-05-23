package java.util

// Ported from Apache Harmony

class StringTokenizer(string: String,
                      private var delimiters: String,
                      returnDelimiters: Boolean)
    extends Enumeration[Object] {
  private var position: Int = 0

  def this(string: String) = this(string, " \t\n\r\f", false)
  def this(string: String, delimiters: String) =
    this(string, delimiters, false)

  if (string == null) {
    throw new NullPointerException()
  }

  def countTokens(): Int = {
    var count   = 0
    var inToken = false
    var i       = position
    var length  = string.length()
    while (i < length) {
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
      i += 1
    }
    if (inToken)
      count += 1
    count
  }

  def hasMoreElements(): Boolean =
    hasMoreTokens()

  def hasMoreTokens(): Boolean = {
    if (delimiters == null) {
      throw new NullPointerException()
    }
    val length = string.length();
    if (position < length) {
      if (returnDelimiters)
        return true // there is at least one character and even if
      // it is a delimiter it is a token

      // otherwise find a character which is not a delimiter
      var i = position
      while (i < length) {
        if (delimiters.indexOf(string.charAt(i), 0) == -1)
          return true
        i += 1
      }
    }
    return false
  }

  def nextElement(): Object =
    nextToken()

  def nextToken(): String = {
    if (delimiters == null) {
      throw new NullPointerException()
    }
    var i      = position
    val length = string.length()

    if (i < length) {
      if (returnDelimiters) {
        if (delimiters.indexOf(string.charAt(position), 0) >= 0) {
          val r = String.valueOf(string.charAt(position))
          position += 1
          return r
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
      position = i;
      if (i < length) {
        position += 1
        while (position < length) {
          if (delimiters.indexOf(string.charAt(position), 0) >= 0)
            return string.substring(i, position);
          position += 1
        }
        return string.substring(i);
      }
    }
    throw new NoSuchElementException();
  }

  def nextToken(delims: String): String = {
    this.delimiters = delims
    nextToken
  }

}
