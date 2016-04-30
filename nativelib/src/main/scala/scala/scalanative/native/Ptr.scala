package scala.scalanative
package native

import scala.language.dynamics
import runtime.undefined

/** The C `const T *` pointer. */
final class Ptr[T] private () {
  /** Dereference a pointer. */
  def unary_! : T = undefined

  /** Store a value to the address pointed at by a pointer. */
  def `unary_!_=` (value: T) = undefined

  /** Compute a derived pointer by adding given offset. */
  def +(offset: UWord): Ptr[T] = undefined

  /** Compute a derived pointer by substricting given offset. */
  def -(offset: UWord): Ptr[T] = undefined
}
