package java.util.stream

import java.util.Iterator

trait BaseStream[+T, +S <: BaseStream[T, S]] extends AutoCloseable {
  def close(): Unit
  def isParallel(): Boolean
  def iterator: Iterator[_ <: T]
  // TODO:
  // def onClose(closeHandler: Runnable): S
  def parallel(): S
  def sequential(): S
  // TODO:
  // def spliterator(): Spliterator[T]
  def unordered(): S
}
