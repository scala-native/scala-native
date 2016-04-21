package scala.runtime

class BoxedUnit extends java.io.Serializable {
  def readResolve(): Object = BoxedUnit.UNIT
  override def equals(that: Any): Boolean = this eq that.asInstanceOf[AnyRef]
  override def hashCode(): Int = 0
  override def toString: String = "()"
}
object BoxedUnit {
  final val UNIT = new BoxedUnit()
  final val TYPE = classOf[Unit]
}
