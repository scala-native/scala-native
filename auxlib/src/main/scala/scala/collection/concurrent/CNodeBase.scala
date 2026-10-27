package scala.collection.concurrent

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater

import scala.scalanative.annotation.alwaysinline

private[concurrent] object CNodeBase {
  final val updater: AtomicIntegerFieldUpdater[CNodeBase[?, ?]] =
    AtomicIntegerFieldUpdater
      .newUpdater[CNodeBase[?, ?]](
        classOf[CNodeBase[?, ?]],
        "csize"
      )
}

private[concurrent] abstract class CNodeBase[K <: AnyRef, V <: AnyRef]
    extends MainNode[K, V] {
  import CNodeBase.updater

  @volatile var csize: Int = -1

  @alwaysinline
  def CAS_SIZE(oldval: Int, nval: Int) =
    updater.compareAndSet(this, oldval, nval)

  @alwaysinline
  def WRITE_SIZE(nval: Int): Unit = updater.set(this, nval)

  @alwaysinline
  def READ_SIZE: Int = updater.get(this)
}
