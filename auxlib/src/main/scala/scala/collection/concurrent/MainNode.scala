package scala.collection.concurrent

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.runtime.Intrinsics.classFieldRawPtr
import scala.scalanative.runtime.fromRawPtr

object MainNode {
  final val updater
      : AtomicReferenceFieldUpdater[MainNode[?, ?], MainNode[?, ?]] =
    new IntrinsicAtomicReferenceFieldUpdater(obj =>
      fromRawPtr(
        classFieldRawPtr(obj.asInstanceOf[MainNode[AnyRef, AnyRef]], "prev")
      )
    )
}

private[concurrent] abstract class MainNode[K <: AnyRef, V <: AnyRef]
    extends BasicNode {
  import MainNode.updater

  @volatile var prev: MainNode[K, V] = _

  def cachedSize(ct: Object): Int

  // standard contract
  def knownSize(): Int

  @alwaysinline
  def CAS_PREV(oldval: MainNode[K, V], nval: MainNode[K, V]) =
    updater.compareAndSet(this, oldval, nval)

  @alwaysinline
  def WRITE_PREV(nval: MainNode[K, V]): Unit = updater.set(this, nval)

  @deprecated
  @alwaysinline def READ_PREV(): MainNode[K, V] =
    updater.get(this).asInstanceOf[MainNode[K, V]]
}
