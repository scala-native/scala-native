package scala.scalanative
package runtime

import native._

object Ops {
  def load[T](ty: Class[T], ptr: Ptr[_]): T = undefined
  def store[T](ty: Class[T], ptr: Ptr[_], value: T): Unit = undefined
  def elem[A, B](ty: Class[A], ptr: Ptr[_], indexes: Long*): Ptr[B] = undefined
}
