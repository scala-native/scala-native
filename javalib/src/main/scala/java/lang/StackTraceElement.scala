package java.lang

final class StackTraceElement(val getClassName: String,
                              val getMethodName: String,
                              val getFileName: String,
                              val getLineNumber: Int) {

  if (getClassName == null) {
    throw new NullPointerException("Declaring class is null")
  }
  if (getMethodName == null) {
    throw new NullPointerException("Method name is null")
  }

  def isNativeMethod: scala.Boolean = false

  override def toString: String = {
    val (file, line) =
      if (getFileName == null) ("Unknown Source", "")
      else if (getLineNumber <= 0) (getFileName, "")
      else (getFileName, ":" + getLineNumber)
    s"$getClassName.$getMethodName($file$line)"
  }

  override def hashCode: scala.Int =
    toString.##

  override def equals(that: Any): scala.Boolean =
    that match {
      case that: StackTraceElement =>
        getClassName == that.getClassName &&
          getMethodName == that.getMethodName &&
          getFileName == that.getFileName &&
          getLineNumber == that.getLineNumber
      case _ =>
        false
    }
}

private[lang] object StackTraceElement {
  object Fail extends scala.util.control.NoStackTrace

  def fromSymbol(sym: String): StackTraceElement = {
    val chars      = sym.toArray
    var pos        = 0
    var className  = ""
    var methodName = ""

    def readSymbol(): Unit = {
      if (read != '_') fail
      if (read != 'S') fail
      readGlobal()
    }

    def readGlobal(): Unit = read() match {
      case 'M' =>
        className = readIdent()
        readSig()
      case ch =>
        fail
    }

    def readSig(): Unit = read() match {
      case 'R' =>
        methodName = "<init>"
      case 'D' | 'P' | 'C' | 'G' =>
        methodName = readIdent()
      case 'K' =>
        readSig()
      case ch =>
        fail
    }

    def readIdent(): String = {
      val len   = readNumber()
      val start = pos
      pos += len
      sym.substring(start, pos)
    }

    def readNumber(): Int = {
      val start  = pos
      var number = 0
      while ('0' <= chars(pos) && chars(pos) <= '9') {
        number = number * 10 + (chars(pos) - '0').toInt
        pos += 1
      }
      if (start == pos) fail
      number
    }

    def fail: Nothing =
      throw Fail

    def read(): Int = {
      val value = chars(pos)
      pos += 1
      value
    }

    try {
      readSymbol()
    } catch {
      case _: Throwable =>
        className = "<none>"
        methodName = sym
    }

    new StackTraceElement(className, methodName, null, 0)
  }
}
