package java.util

class IdentityHashMap[K, V] extends HashMap[K, V] {

  override def boxKey(key: K): AnyRef =
    IdentityBox(key)

  override def unboxKey(box: AnyRef): K =
    box match {
      case IdentityBox(value) => value.asInstanceOf[K]
      case _                  => null.asInstanceOf[K]
    }
}
