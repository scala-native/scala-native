package org.scalanative.testsuite.javalib.lang

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/** A virtual thread started with exception capture. Use `thread` to join (with
 *  any timeout), then `rethrowException()` to fail the test if the VT threw.
 */
class VirtualThreadExecution(
    val thread: Thread,
    exceptionRef: AtomicReference[Option[Throwable]]
) {
  /** If an exception was captured from the VT, rethrow it (so the test fails).
   */
  def rethrowException(): Unit = {
    exceptionRef.get().foreach(throw _)
  }

  /** Join the thread with the given timeout in milliseconds. */
  def join(timeoutMs: Long): Unit =
    thread.join(timeoutMs, 0)
}

/** Run `body` on a new virtual thread. Any uncaught exception is captured.
 *  Returns a `VirtualThreadExecution`: use `.thread` to join (e.g.
 *  `ctx.thread.join()` or `ctx.join(timeoutMs)`), then
 *  `ctx.rethrowException()`.
 */
def startVirtualThread(body: Runnable): VirtualThreadExecution = {
  val ref = new AtomicReference[Option[Throwable]](None)
  val thread = Thread
    .ofVirtual()
    .uncaughtExceptionHandler((_, ex) => ref.set(Some(ex)))
    .start(body)
  new VirtualThreadExecution(thread, ref)
}

/** Run `body` on a new virtual thread, join with the given timeout (ms), then
 *  rethrow any uncaught exception. Use when the whole VT task can be expressed
 *  as a block and you want a single call to run-and-join-and-fail.
 */
def runOnVirtualThread(timeoutMs: Long)(body: => Unit): Unit = {
  val ctx = startVirtualThread(() => body)
  ctx.join(timeoutMs)
  ctx.rethrowException()
}
