package java.lang.ref

/* Subclassed only by the reference types of this package, as on the JVM.
 *
 * The garbage collector reads and clears the referent of weak references in
 * place: it must stay the only and first field of Reference, so that it
 * follows the object header in every reference object. See
 * codegen.FieldLayout and WeakReferenceObject in ObjectHeader.h.
 */
abstract class Reference[T] private[ref] (
    @volatile private[ref] var _gc_modified_referent: T
) {
  def get(): T = _gc_modified_referent
  def clear(): Unit = _gc_modified_referent = null.asInstanceOf[T]
  def isEnqueued(): Boolean = false
  def enqueue(): Boolean = false
  private[ref] def markDequeued(): Unit = ()
}
