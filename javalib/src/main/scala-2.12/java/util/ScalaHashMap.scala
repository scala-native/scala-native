package java.util

import scala.collection.mutable

private[util] object ScalaHashMap {
  def apply[K, V](initialCapacity: Int): mutable.HashMap[K, V] =
    new {
      override protected val initialSize: Int = initialCapacity
    } with mutable.HashMap[K, V]
}
