package scala.scalanative
package native

import scala.language.dynamics
import scala.reflect.ClassTag
import runtime.undefined

/** The C `const T *` pointer. */
final class Ptr[T] private () {

  /** Dereference a pointer. */
  def unary_!(implicit ct: ClassTag[T]): T = undefined

  /** Store a value to the address pointed at by a pointer. */
  def `unary_!_=`(value: T)(implicit ct: ClassTag[T]): Unit = undefined

  /** Compute a derived pointer by adding given offset. */
  def +(offset: Word)(implicit ct: ClassTag[T]): Ptr[T] = undefined

  /** Compute a derived pointer by substricting given offset. */
  def -(offset: Word)(implicit ct: ClassTag[T]): Ptr[T] = undefined

  /** Read a value at given offset. Equivalent to !(offset + word). */
  def apply(offset: Word)(implicit ct: ClassTag[T]): T = undefined

  /** Store a value to given offset. Equivalent to !(offset + word) = value. */
  def update(offset: Word, value: T)(implicit ct: ClassTag[T]): T = undefined
}
