/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

import java.util.concurrent.locks.ReentrantLock

object CyclicBarrier {
  private[concurrent] class Generation() {
    var broken = false // initially false
  }
}
class CyclicBarrier(
    /* The number of parties */
    val parties: Int,
    /* The command to run when tripped */
    val barrierCommand: Runnable
) {

  def this(parties: Int) = this(parties, null)

  /* Number of parties still waiting. Counts down from parties to 0 on each
   *  generation. It is reset to parties on each new generation or when broken.
   */
  private var count: Int = parties
  if (count <= 0) throw new IllegalArgumentException

  /* The lock for guarding barrier entry */
  final private val lock = new ReentrantLock

  /* Condition to wait on until tripped */
  final private val trip = lock.newCondition()

  /* The current generation */
  private var generation = new CyclicBarrier.Generation

  /* Updates state on barrier trip and wakes up everyone. Called only while
   *  holding lock.
   */
  private def nextGeneration(): Unit = { // signal completion of last generation
    trip.signalAll()
    // set up next generation
    count = parties
    generation = new CyclicBarrier.Generation
  }

  /* Sets current barrier generation as broken and wakes up everyone. Called
   *  only while holding lock.
   */
  private def breakBarrier(): Unit = {
    generation.broken = true
    count = parties
    trip.signalAll()
  }

  /* Main barrier code, covering the various policies.*/
  @throws[InterruptedException]
  @throws[BrokenBarrierException]
  @throws[TimeoutException]
  private def dowait(timed: Boolean, _nanos: Long): Int = {
    var nanos = _nanos
    val lock = this.lock
    lock.lock()
    try {
      val g = generation
      if (g.broken) throw new BrokenBarrierException
      if (Thread.interrupted()) {
        breakBarrier()
        throw new InterruptedException
      }
      count -= 1
      val index = count
      if (index == 0) { // tripped
        val command = barrierCommand
        if (command != null)
          try command.run()
          catch {
            case ex: Throwable =>
              breakBarrier()
              throw ex
          }
        nextGeneration()
        return 0
      }
      // loop until tripped, broken, interrupted, or timed out

      while (true) {
        try
          if (!timed) trip.await()
          else if (nanos > 0L) nanos = trip.awaitNanos(nanos)
        catch {
          case ie: InterruptedException =>
            if ((g eq generation) && !g.broken) {
              breakBarrier()
              throw ie
            } else { // We're about to finish waiting even if we had not
              // been interrupted, so this interrupt is deemed to
              // "belong" to subsequent execution.
              Thread.currentThread().interrupt()
            }
        }
        if (g.broken) throw new BrokenBarrierException
        if (g ne generation) return index
        if (timed && nanos <= 0L) {
          breakBarrier()
          throw new TimeoutException
        }
      }
    } finally lock.unlock()
    -1 // unreachable
  }

  def getParties(): Int = parties

  @throws[InterruptedException]
  @throws[BrokenBarrierException]
  def await(): Int =
    try dowait(false, 0L)
    catch {
      case toe: TimeoutException => throw new Error(toe) // cannot happen
    }

  @throws[InterruptedException]
  @throws[BrokenBarrierException]
  @throws[TimeoutException]
  def await(timeout: Long, unit: TimeUnit): Int =
    dowait(true, unit.toNanos(timeout))

  def isBroken(): Boolean = {
    val lock = this.lock
    lock.lock()
    try generation.broken
    finally lock.unlock()
  }

  def reset(): Unit = {
    val lock = this.lock
    lock.lock()
    try {
      breakBarrier() // break the current generation
      nextGeneration() // start a new generation
    } finally lock.unlock()
  }

  def getNumberWaiting(): Int = {
    val lock = this.lock
    lock.lock()
    try parties - count
    finally lock.unlock()
  }
}
