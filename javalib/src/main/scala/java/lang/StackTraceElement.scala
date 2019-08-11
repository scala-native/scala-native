package java.lang

import scala.scalanative.native.{
  fromCString,
  stackalloc,
  string,
  CChar,
  CString,
  CUnsignedLong,
  Ptr
}
import scala.scalanative.runtime.{ByteArray, unwind}

final class StackTraceElement private (_symbol: CString) {
  var symbol: Array[Byte] = _

  if (_symbol != null) {
    symbol = new Array[Byte](256)
    string.memcpy(symbol.asInstanceOf[ByteArray].at(0), _symbol, 256)
  }

  def this(className: String,
           methodName: String,
           fileName: String,
           lineNumber: Int) = {
    this(null)
    if (className == null)
      throw new NullPointerException("Declaring class is null")

    if (methodName == null)
      throw new NullPointerException("Method name is null")

    initDone = true
    _className = className
    _methodName = methodName
    _fileName = fileName
    _lineNumber = lineNumber
  }

  private var initDone: Boolean   = false
  private var _className: String  = _
  private var _methodName: String = _
  private var _fileName: String   = _
  private var _lineNumber: Int    = _

  def getClassName: String = {
    if (!initDone) initMembers()
    _className
  }

  def getMethodName: String = {
    if (!initDone) initMembers()
    _methodName
  }

  def getFileName: String = {
    if (!initDone) initMembers()
    _fileName
  }

  def getLineNumber: Int = {
    if (!initDone) initMembers()
    _lineNumber
  }

  // symbol is "classname::methodName_T1_..._TN"
  // where classname doesn't include colons and method name underscores.
  private def initMembers(): Unit = {
    val sym = fromCString(symbol.asInstanceOf[ByteArray].at(0))
    val (className, methodName) =
      sym.indexOf("::") match {
        case -1 =>
          ("<none>", sym)
        case sep =>
          sym.indexOf("_", sep) match {
            case -1 =>
              (sym.substring(0, sep), sym.substring(sep + 2))
            case end =>
              (sym.substring(0, sep), sym.substring(sep + 2, end))
          }
      }

    initDone = true
    _className = className
    _methodName = methodName
    _fileName = null
    _lineNumber = 0
    symbol = null
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
  private val cache =
    collection.mutable.HashMap.empty[CUnsignedLong, StackTraceElement]
  private def makeStackTraceElement(
      cursor: Ptr[scala.Byte]): StackTraceElement = {
    val name   = stackalloc[CChar](256)
    val offset = stackalloc[scala.Byte](8)

    unwind.get_proc_name(cursor, name, 256, offset)
    new StackTraceElement(name)
  }

  /**
   * Tries to retrieve a pre-computed `StackTraceElement`, or computes it.
   * We use `startIp` (ie. the address of the first instruction of the function)
   * as cache key.
   *
   * Computing stack traces is expensive because we need to collect names of
   * functions, and convert those names from C strings to Scala strings.
   */
  private[lang] def cached(cursor: Ptr[scala.Byte],
                           startIp: CUnsignedLong): StackTraceElement =
    cache.synchronized {
      cache.getOrElseUpdate(startIp, makeStackTraceElement(cursor))
    }

}
