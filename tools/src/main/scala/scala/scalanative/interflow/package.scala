package scala.scalanative

import scala.collection.mutable

package object interflow {

  type Addr = Long

  implicit class MutMapOps[K, V](val map: mutable.Map[K, V]) extends AnyVal {
    def addMissing(other: Iterable[(K, V)]): Unit = other.foreach {
      case (key, value) => map.getOrElseUpdate(key, value)
    }
  }

}
