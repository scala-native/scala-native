package scala.scalanative.misc
package greenthread

import scala.collection.mutable
import scala.scalanative.native._

trait Runnable {
  def run(): Unit
}

abstract class GreenThread extends Runnable {

  def fork(): Unit = {
    threadId = GreenThread.genId(this)
    CGreenThread.threadYield
    // no main thread
    if (threadId > 0) {
      GreenThread.runCallback(threadId)
    }
  }

  def join(): Unit = {
    CGreenThread.threadJoin(threadId)
  }

  var threadId: GreenThread.CThreadId = -1
  var isRunning: Boolean              = false
}

class GreenThreadMain extends GreenThread {
  def run(): Unit = {}
}

object GreenThread {
  type CThreadId = CInt

  val mainThread: GreenThread = new GreenThreadMain()

  def isThreadRunning(threadId: CThreadId): Boolean =
    threads(threadId).isRunning

  def threadStop(): Unit = CGreenThread.threadStop()

  def threadYield(): Unit = CGreenThread.threadYield()

  /// Private section

  private[GreenThread] def runCallback(threadId: CThreadId): Unit = {
    val current = CGreenThread.currentThreadId
    CGreenThread.threadFork(threadId)
    if (CGreenThread.currentThreadId != current) {
      threads(threadId).isRunning = true
      threads(threadId).run()
      threads(threadId).isRunning = false
      CGreenThread.threadStop();
    }
  }

  // Utils
  private var nextId = 0;
  def genId(t: GreenThread): CThreadId = {
    threads(nextId) = t
    val result = nextId
    nextId = nextId + 1
    result
  }

  val threads = new scala.Array[GreenThread](1000001) //todo: remove hardcoded number
  CGreenThread.threadInit()
  mainThread.threadId = GreenThread.genId(mainThread)
}

@extern
object CGreenThread {
  // Bindings

  @name("scalanative_misc_greenthreads_fork")
  def threadFork(param: GreenThread.CThreadId): CInt = extern

  @name("scalanative_misc_greenthreads_gtinit")
  def threadInit(): Unit = extern

  @name("scalanative_misc_greenthreads_currentthreadid")
  def currentThreadId(): GreenThread.CThreadId = extern

  @name("scalanative_misc_greenthreads_join")
  def threadJoin(param: GreenThread.CThreadId): Unit = extern

  @name("scalanative_misc_greenthreads_gtstop")
  def threadStop(): Unit = extern

  @name("scalanative_misc_greenthreads_gtyield")
  def threadYield(): CBool = extern
}
