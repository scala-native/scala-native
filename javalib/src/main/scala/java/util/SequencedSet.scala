package java.util

trait SequencedSet[E /* <: AnyRef */ ]
    extends SequencedCollection[E]
    with Set[E] {

  def reversed(): SequencedSet[E]
}
