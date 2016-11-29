package scala.scalanative
package util

/** Scoped implicit lifetime.
 *
 *  The main idea behind the Scope is to encode resource lifetimes through
 *  a concept of an implicit scope. Scopes are necessary to acquire resources.
 *  They are responsible for disposal of the resources once the evaluation exits
 *  the demarkated block in the source code.
 *
 *  See https://www.youtube.com/watch?v=MV2eJkwarT4 for details.
 */
@annotation.implicitNotFound(msg = "Resource acquisition requires a scope.")
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
    val scope = new Impl()
    try f(scope)
    finally scope.close()
  }

  /** Scope that never closes. Resources allocated in this scope are
   *  going to be acquired as long as application is running.
   */
  val forever: Scope = new Impl {
    override def close(): Unit =
      throw new UnsupportedOperationException("Can't close forever Scope.")
  }

  private sealed class Impl extends Scope {
    private[this] var resources: List[Resource] = Nil

    def acquire(res: Resource): Unit = {
      resources ::= res
    }

    def close(): Unit = resources match {
      case Nil =>
        ()

      case first :: rest =>
        resources = rest
        try first.close()
        finally close()
    }
  }
}
