package scala.scalanative

import scala.collection.mutable

package object interflow {

  private[interflow] type Addr = Long

  /** Result of an instruction that never completes, like the call of an inlined
   *  method that does not return. Whatever makes it leave the block has been
   *  emitted already, with unwind edges of its own, and the code that follows
   *  it cannot be reached.
   */
  private[interflow] val NoValue: nir.Val = nir.Val.Zero(nir.Type.Nothing)

  private[interflow] implicit class MutMapOps[K, V](val map: mutable.Map[K, V])
      extends AnyVal {
    def addMissing(other: Iterable[(K, V)]): Unit = other.foreach {
      case (key, value) => map.getOrElseUpdate(key, value)
    }
  }

}
