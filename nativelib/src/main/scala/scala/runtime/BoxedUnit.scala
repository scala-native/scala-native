package scala.runtime

class BoxedUnit extends java.io.Serializable {
  override def equals(that: Any): Boolean = this eq that.asInstanceOf[AnyRef]
  override def hashCode(): Int            = 0
  override def toString: String           = "()"
}
object BoxedUnit {
  def UNIT = scala.scalanative.runtime.undefined
  val TYPE = classOf[Unit]
}
