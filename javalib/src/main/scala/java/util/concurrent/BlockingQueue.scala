package java.util.concurrent

import java.util.Collection
import java.util.Queue

trait BlockingQueue[E] extends Queue[E] {
  def add(e: E): Boolean
  def offer(e: E): Boolean
  def put(e: E): Unit
  def offer(e: E, timeout: Long, unit: TimeUnit): Boolean
  def take(): E
  def poll(timeout: Long, unit: TimeUnit): E
  def remainingCapacity(): Int
  def remove(o: Any): Boolean
  def contains(o: Any): Boolean
  def drainTo(c: Collection[_ >: E]): Int
  def drainTo(c: Collection[_ >: E], maxElements: Int): Int
}
