package java.util

trait SequencedSet[E /* <: AnyRef */ ]
    extends SequencedCollection[E]
    with Set[E] {
  /* Commented out until we're able to provide reversed views for collections
  override def reversed(): SequencedSet[E]
   */
}
