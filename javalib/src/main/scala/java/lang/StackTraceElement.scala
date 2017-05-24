package java.lang

final class StackTraceElement(val getClassName: String,
                              val getMethodName: String,
                              val getFileName: String,
                              val getLineNumber: Int) {

  if (getClassName == null)
    throw new NullPointerException("Declaring class is null")

  if (getMethodName == null)
    throw new NullPointerException("Method name is null")

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
  // symbol is "classname::methodName_T1_..._TN"
  // where classname doesn't include colons and method name underscores.
  def fromSymbol(symbol: String): StackTraceElement = {
    val (className, methodName) =
      symbol.indexOf("::") match {
        case -1 =>
          ("<none>", symbol)
        case sep =>
          symbol.indexOf("_", sep) match {
            case -1 =>
              (symbol.substring(0, sep), symbol.substring(sep + 2))
            case end =>
              (symbol.substring(0, sep), symbol.substring(sep + 2, end))
          }
      }
    new StackTraceElement(className, methodName, null, 0)
  }
}
