package java.util

class IdentityHashMap[K, V] extends HashMap[K, V] {

  override def boxKey(key: K): AnyRef =
    IdentityBox(key)
  override def unboxKey(box: AnyRef): K =
    box match {
      case IdentityBox(value) => value.asInstanceOf[K]
      case _                  => null.asInstanceOf[K]
    }

  // forwarders - https://github.com/scala-native/scala-native/issues/375
  override def containsKey(key: Any): Boolean     = super.containsKey(key)
  override def containsValue(value: Any): Boolean = super.containsValue(value)
  override def get(key: Any): V                   = super.get(key)
  override def put(key: K, value: V): V           = super.put(key, value)
  override def remove(key: Any): V                = super.remove(key)
  override def clear(): Unit                      = super.clear()
}
