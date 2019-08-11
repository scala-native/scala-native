package scala.scalanative.runtime

import scala.scalanative.runtime.ThreadBase._

abstract class ThreadBase {
  def threadModuleBase: ThreadModuleBase
  def initMainThread(): Unit
  private var lockState                           = Normal
  final def getLockState: Int                     = lockState
  private[runtime] def setLockState(s: Int): Unit = lockState = s
  // only here to implement holdsLock
  private[runtime] var locks = new scala.Array[Monitor](8)
  private[runtime] var size  = 0
  final def holdsLock(obj: Object): scala.Boolean = {
    if (size == 0) {
      false
    } else {
      val target = Monitor(obj)
      var i: Int = 0
      while (i < size && locks(i) != target) {
        i += 1
      }
      i < size
    }
  }
  final def freeAllLocks(): Unit = {
    var i: Int = 0
    while (i < size) {
      locks(i).exit()
      i += 1
    }
  }
}

object ThreadBase {
  private val threadModule: ThreadModuleBase =
    Thread.currentThread().asInstanceOf[ThreadBase].threadModuleBase
  final val Normal       = 0
  final val Blocked      = 1
  final val Waiting      = 2
  final val TimedWaiting = 3

  protected[runtime] def initMainThread(): Unit = threadModule.initMainThread()
  protected[runtime] def shutdownCheckLoop(): Unit =
    threadModule.shutdownCheckLoop()
  protected[runtime] def mainThreadEnds(): Unit = threadModule.mainThreadEnds()

  /**
   *
   * @return currentThread or `null` if:<br>
   *         1. it is a non-Scala thread<br>
   *         2. Scala thinks it is dead<br>
   *         3. it has not properly initialized because it is handling a signal
   */
  protected[runtime] def currentThreadInternal(): Thread with ThreadBase =
    threadModule.currentThreadInternal()
}

abstract class ThreadModuleBase {
  protected[runtime] def shutdownCheckLoop(): Unit
  protected[runtime] def initMainThread(): Unit
  protected[runtime] def mainThreadEnds(): Unit
  protected[runtime] def currentThreadInternal(): Thread with ThreadBase
}
