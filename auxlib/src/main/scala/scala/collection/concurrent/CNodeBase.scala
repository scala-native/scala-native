package scala.collection.concurrent

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater

import scala.scalanative.annotation.alwaysinline

private[concurrent] abstract class CNodeBase[K <: AnyRef, V <: AnyRef]
    extends MainNode[K, V] {
  @volatile var csize: Int = -1

  final val updater: AtomicIntegerFieldUpdater[CNodeBase[?, ?]] =
    AtomicIntegerFieldUpdater
      .newUpdater[CNodeBase[?, ?]](
        classOf[CNodeBase[?, ?]],
        "csize"
      )

  @alwaysinline
  def CAS_SIZE(oldval: Int, nval: Int) =
    updater.compareAndSet(this, oldval, nval)

  @alwaysinline
  def WRITE_SIZE(nval: Int): Unit = updater.set(this, nval)

  @alwaysinline
  def READ_SIZE: Int = updater.get(this)
}
