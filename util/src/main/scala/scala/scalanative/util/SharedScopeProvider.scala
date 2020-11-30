package scala.scalanative.util
import java.util.concurrent.atomic.AtomicReference
import java.util.function.UnaryOperator

/** Unsafe manually managed scope provider allowing to share them between multiple contexts */
final class SharedScopeProvider[Key] {

  /** Registers shared usage of scope and executes given function.
   *  If there its the no other usages of shared scope it would be closed
   *  @param usageId unique identifier of scope usage
   */
  def identifiedBy[T](usageId: Key)(fn: Scope => T): T = {
    try fn(use(usageId))
    finally tryClose(usageId)
  }

  private[this] val usages       = new AtomicReference[Set[Key]](Set.empty[Key])
  private[this] val scope: Scope = Scope.unsafe

  private def tryClose(usageId: Key): Unit = {
    usages.getAndUpdate {
      new UnaryOperator[Set[Key]] {
        override def apply(s: Set[Key]): Set[Key] = {
          val updated = s - usageId
          if (updated.isEmpty) {
            scope.close()
          }
          updated
        }
      }
    }
  }

  private def use(usageId: Key): Scope = {
    usages.getAndUpdate(new UnaryOperator[Set[Key]] {
      override def apply(s: Set[Key]): Set[Key] = s + usageId
    })
    scope
  }

}
