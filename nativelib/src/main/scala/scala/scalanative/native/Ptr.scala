package scala.scalanative
package native

import scala.language.dynamics
import runtime.{undefined, Tag}

/** The C `const T *` pointer. */
final class Ptr[T] private () {

  /** Dereference a pointer. */
  def unary_!(implicit tag: Tag[T]): T = undefined

  /** Store a value to the address pointed at by a pointer. */
  def `unary_!_=`(value: T)(implicit tag: Tag[T]): Unit = undefined

  /** Compute a derived pointer by adding given offset. */
  def +(offset: Word)(implicit tag: Tag[T]): Ptr[T] = undefined

  /** Compute a derived pointer by subtracting given offset. */
  def -(offset: Word)(implicit tag: Tag[T]): Ptr[T] = undefined

  /** Read a value at given offset. Equivalent to !(offset + word). */
  def apply(offset: Word)(implicit tag: Tag[T]): T = undefined

  /** Store a value to given offset. Equivalent to !(offset + word) = value. */
  def update(offset: Word, value: T)(implicit tag: Tag[T]): T = undefined
}
