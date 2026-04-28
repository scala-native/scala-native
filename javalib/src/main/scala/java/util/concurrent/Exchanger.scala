/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

import java.util.concurrent.locks.ReentrantLock

import scala.scalanative.annotation.safePublish

class Exchanger[V] {

  private final class Node(val item: V) {
    var matched = false
    var matchItem: V = _
  }

  @safePublish
  private final val lock = new ReentrantLock()

  private final val available = lock.newCondition()

  private var slot: Node = _

  @throws[InterruptedException]
  def exchange(x: V): V = {
    val lock = this.lock
    lock.lockInterruptibly()
    try {
      val other = slot
      if (other == null) {
        val node = new Node(x)
        slot = node
        while (!node.matched) {
          try available.await()
          catch {
            case ex: InterruptedException =>
              if (!node.matched && (slot eq node)) {
                slot = null
                available.signalAll()
                throw ex
              }
              Thread.currentThread().interrupt()
          }
        }
        node.matchItem
      } else {
        slot = null
        other.matchItem = x
        other.matched = true
        available.signalAll()
        other.item
      }
    } finally lock.unlock()
  }

  @throws[InterruptedException]
  @throws[TimeoutException]
  def exchange(x: V, timeout: Long, unit: TimeUnit): V = {
    var nanos = unit.toNanos(timeout)
    val lock = this.lock
    lock.lockInterruptibly()
    try {
      val other = slot
      if (other == null) {
        val node = new Node(x)
        slot = node
        while (!node.matched) {
          if (nanos <= 0L) {
            if (slot eq node) {
              slot = null
              available.signalAll()
              throw new TimeoutException()
            }
          }
          try nanos = available.awaitNanos(nanos)
          catch {
            case ex: InterruptedException =>
              if (!node.matched && (slot eq node)) {
                slot = null
                available.signalAll()
                throw ex
              }
              Thread.currentThread().interrupt()
          }
        }
        node.matchItem
      } else {
        slot = null
        other.matchItem = x
        other.matched = true
        available.signalAll()
        other.item
      }
    } finally lock.unlock()
  }
}
