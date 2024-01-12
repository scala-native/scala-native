package java.io

// Ported from Apache Harmony

class LineNumberReader(in: Reader, sz: Int) extends BufferedReader(in, sz) {
  def this(in: Reader) = this(in, 4096)

  private var lineNumber: Int = 0
  private var lastWasCR: Boolean = false
  private var markedLineNumber: Int = -1
  private var markedLastWasCR: Boolean = false

  override def mark(readAheadLimit: Int): Unit = {
    super.mark(readAheadLimit)
    markedLineNumber = getLineNumber()
    markedLastWasCR = lastWasCR
  }

  def getLineNumber(): Int =
    lineNumber

  override def read(): Int = {
    var ch = super.read()
    if (ch == '\n' && lastWasCR) {
      ch = super.read()
    }
    lastWasCR = false
    ch match {
      case '\r' =>
        ch = '\n'
        lastWasCR = true
        lineNumber += 1
      case '\n' =>
        lineNumber += 1
    }
    ch
  }

  override def read(buf: Array[Char], off: Int, len: Int): Int = {
    val read = super.read(buf, off, len)
    if (read == -1) -1
    else {
      var i = 0
      while (i < read) {
        val ch = buf(i + off)
        if (ch == '\r') {
          lineNumber += 1
          lastWasCR = true
        } else if (ch == '\n') {
          if (!lastWasCR) {
            lineNumber += 1
          }
          lastWasCR = false
        } else {
          lastWasCR = false
        }
        i += 1
      }
      read
    }
  }

  override def readLine(): String = {
    if (lastWasCR) {
      chompNewLine()
      lastWasCR = false
    }
    val result = super.readLine()
    if (result != null) {
      lineNumber += 1
    }
    result
  }

  override def reset(): Unit = {
    super.reset()
    lineNumber = markedLineNumber
    lastWasCR = markedLastWasCR
  }

  def setLineNumber(lineNumber: Int): Unit =
    this.lineNumber = lineNumber

  override def skip(n: Long): Long = {
    if (n < 0) throw new IllegalArgumentException()
    var i = 0
    var eof = false
    while (i < n && !eof) {
      if (read() == -1) eof = true
      i += 1
    }
    i
  }

}
