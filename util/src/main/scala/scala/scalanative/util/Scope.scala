package scala.scalanative
package util

import java.util.concurrent.atomic.AtomicReference
import java.util.function.UnaryOperator
import scala.annotation.implicitNotFound

/** Scoped implicit lifetime.
 *
 *  The main idea behind the Scope is to encode resource lifetimes through a
 *  concept of an implicit scope. Scopes are necessary to acquire resources.
 *  They are responsible for disposal of the resources once the evaluation exits
 *  the demarkated block in the source code.
 *
 *  See https://www.youtube.com/watch?v=MV2eJkwarT4 for details.
 */
@implicitNotFound(msg = "Resource acquisition requires a scope.")
trait Scope {

  /** Push resource onto the resource stack. */
  def acquire(res: Resource): Unit

  /** Clean up all the resources in FIFO order. */
  def close(): Unit
}

object Scope {

  /** Opens an implicit scope, evaluates the function and cleans up all the
   *  resources as soon as execution leaves the demercated block.
   */
  def apply[T](f: Scope => T): T = {
    val scope = new Impl
    try f(scope)
    finally scope.close()
  }

  /** Scope that never closes. Resources allocated in this scope are going to be
   *  acquired as long as application is running.
   */
  val forever: Scope = new Impl {
    override def close(): Unit =
      throw new UnsupportedOperationException("Can't close forever Scope.")
  }

  /** Unsafe manually managed scope. */
  def unsafe(): Scope = new Impl {}

  private sealed class Impl extends Scope {
    type Resources = List[Resource]

    private val resources = new AtomicReference[Resources](Nil)

    def acquire(res: Resource): Unit = {
      resources.getAndUpdate {
        new UnaryOperator[Resources] {
          override def apply(t: Resources): Resources = res :: t
        }
      }
    }

    def close(): Unit = {
      def loop(resources: Resources): Unit = resources match {
        case Nil =>
          ()
        case first :: rest =>
          try first.close()
          finally loop(rest)
      }
      loop(resources.getAndSet(Nil))
    }
  }

}
