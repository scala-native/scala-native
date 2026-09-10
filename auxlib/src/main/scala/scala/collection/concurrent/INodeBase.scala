// Ported from Scala 2.13.10

package scala.collection.concurrent

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater

object INodeBase {
  final val updater
      : AtomicReferenceFieldUpdater[INodeBase[?, ?], MainNode[?, ?]] =
    AtomicReferenceFieldUpdater
      .newUpdater[INodeBase[?, ?], MainNode[?, ?]](
        classOf[INodeBase[?, ?]],
        classOf[MainNode[?, ?]],
        "mainnode"
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
