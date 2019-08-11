package scala.concurrent.impl

import java.util.concurrent.atomic.AtomicReference

/**
 * JavaScript specific implementation of AbstractPromise
 *
 * This basically implements a "CAS" in Scala for JavaScript. Its
 * implementation is trivial because there is no multi-threading.
 *
 * @author Tobias Schlatter
 */
abstract class AbstractPromise {
  private val state: AtomicReference[AnyRef] = new AtomicReference[AnyRef](null)
  protected final def updateState(oldState: AnyRef, newState: AnyRef): Boolean = state.compareAndSet(oldState, newState)
  protected final def getState: AnyRef = state.get()
}

object AbstractPromise {
  protected def updater = ???
}
