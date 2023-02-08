// Ported from Scala 2.13.10

package scala.collection.concurrent

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater

import scala.scalanative.runtime.Intrinsics.classFieldRawPtr
import scala.scalanative.runtime.fromRawPtr

object INodeBase {
  final val updater
      : AtomicReferenceFieldUpdater[INodeBase[_, _], MainNode[_, _]] =
    new IntrinsicAtomicReferenceFieldUpdater(obj =>
      fromRawPtr(classFieldRawPtr(obj, "mainnode"))
    )

  final val RESTART = new Object {}
  final val NO_SUCH_ELEMENT_SENTINEL = new Object {}
}

private[concurrent] abstract class INodeBase[K <: AnyRef, V <: AnyRef](
    generation: Gen
) extends BasicNode {
  @volatile var mainnode: MainNode[K, V] = _
  final var gen: Gen = generation

  def prev(): BasicNode = null
}
