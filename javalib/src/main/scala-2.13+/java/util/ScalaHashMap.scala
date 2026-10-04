package java.util

import scala.collection.mutable

private[util] object ScalaHashMap {
  def apply[K, V](initialCapacity: Int): mutable.HashMap[K, V] =
    new mutable.HashMap[K, V](initialCapacity, 0.75)
}
