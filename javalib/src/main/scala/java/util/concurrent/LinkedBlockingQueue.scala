package java.util.concurrent

import java.util._
import java.util.concurrent.atomic._
import java.util.concurrent.locks._

class LinkedBlockingQueue[E]
    extends AbstractQueue[E]
    with BlockingQueue[E]
    with java.io.Serializable {

  // def this()
  // def this(c: Collection[_ <: E])
  // def this(capacity: Int)
  override def clear(): Unit = ???
  override def contains(o: Any): Boolean = ???
  def drainTo(c: Collection[_ >: E]): Int = ???
  def drainTo(c: Collection[_ >: E], maxElements: Int): Int = ???
  def iterator(): Iterator[E] = ???
  def offer(e: E): Boolean = ???
  def offer(e: E, timeout: Long, unit: TimeUnit): Boolean = ???
  def peek(): E = ???
  def poll(): E = ???
  def poll(timeout: Long, unit: TimeUnit): E = ???
  def put(e: E): Unit = ???
  def remainingCapacity(): Int = ???
  override def remove(o: Any): Boolean = ???
  def size(): Int = ???
  def spliterator(): Spliterator[E] = ???
  def take(): E = ???
  override def toArray(): Array[AnyRef] = ???
  override def toArray[T <: AnyRef](a: Array[T]): Array[T] = ???
}