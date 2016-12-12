package scala

// Ported from Scala.js.
// Modified to use collection.mutable.Map instead of java.util.WeakHashMap.

final class Symbol private (val name: String) extends Serializable {
  override def toString(): String = "'" + name

  @throws(classOf[java.io.ObjectStreamException])
  private def readResolve(): Any = Symbol.apply(name)
  override def hashCode = name.hashCode()
  override def equals(other: Any) = this eq other.asInstanceOf[AnyRef]
}

object Symbol extends UniquenessCache[Symbol] {
  override def apply(name: String): Symbol = super.apply(name)
  protected def valueFromKey(name: String): Symbol = new Symbol(name)
  protected def keyFromValue(sym: Symbol): Option[String] = Some(sym.name)
}

private[scala] abstract class UniquenessCache[V] {
  private val cache = collection.mutable.Map.empty[String, V]

  protected def valueFromKey(k: String): V
  protected def keyFromValue(v: V): Option[String]

  def apply(name: String): V =
    cache.getOrElseUpdate(name, valueFromKey(name))

  def unapply(other: V): Option[String] = keyFromValue(other)
}
