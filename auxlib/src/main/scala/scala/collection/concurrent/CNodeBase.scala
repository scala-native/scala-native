package scala.collection.concurrent

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.runtime.Intrinsics.classFieldRawPtr
import scala.scalanative.runtime.fromRawPtr

private[concurrent] abstract class CNodeBase[K <: AnyRef, V <: AnyRef]
    extends MainNode[K, V] {
  @volatile var csize: Int = -1

  final val updater: AtomicIntegerFieldUpdater[CNodeBase[_, _]] =
    new IntrinsicAtomicIntegerFieldUpdater(obj =>
      fromRawPtr(
        classFieldRawPtr(obj.asInstanceOf[CNodeBase[AnyRef, AnyRef]], "csize")
      )
    )

  @alwaysinline
  def CAS_SIZE(oldval: Int, nval: Int) =
    updater.compareAndSet(this, oldval, nval)

  @alwaysinline
  def WRITE_SIZE(nval: Int): Unit = updater.set(this, nval)

  @alwaysinline
  def READ_SIZE: Int = updater.get(this)
}
