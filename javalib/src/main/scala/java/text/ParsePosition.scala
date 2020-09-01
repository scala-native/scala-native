package java.text

// Hand ported from Apache Harmony, URL:
//   https://github.com/apache/harmony/blob/trunk/classlib/modules/text/src/\
//         main/java/java/text/ParsePosition.java
//
// The port is close enough to the original that it inherits the
// Apache Harmony license through the Scala Native license.

class ParsePosition(private[this] var _index: Int) {
  private[this] var errorIndex = -1

  override def equals(obj: Any): Boolean =
    obj match {
      case that: ParsePosition =>
        (this.getIndex() == that.getIndex()) &&
          (this.getErrorIndex() == that.getErrorIndex())

      case _ => false
    }

  override def hashCode(): Int = _index + errorIndex

  def getErrorIndex(): Int = errorIndex

  def getIndex(): Int = _index

  def setErrorIndex(index: Int): Unit = { errorIndex = index }

  def setIndex(index: Int): Unit = { _index = index }

  override def toString(): String = {
    val idx      = getIndex()
    val errorIdx = getErrorIndex()
    s"java.text.ParsePosition[index=${idx},errorIndex=${errorIdx}]"
  }
}
