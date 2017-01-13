package java.lang

final class StackTraceElement private[lang] (private[lang] val symbol: String) {
  // symbol is "classname::methodName_T1_..._TN"
  // where classname doesn't include colons and method name underscores.

  lazy val getClassName: String =
    symbol.substring(0, symbol.indexOf("::"))

  lazy val getMethodName: String =
    symbol.substring(symbol.indexOf("::") + 2, symbol.indexOf("_"))

  def isNativeMethod: scala.Boolean = false

  override def toString: String =
    symbol

  override def hashCode: scala.Int =
    symbol.hashCode

  override def equals(that: Any): scala.Boolean =
    that match {
      case that: StackTraceElement =>
        symbol == that.symbol
      case _ =>
        false
    }
}
