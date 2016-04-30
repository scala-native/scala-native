package scala.scalanative
package native

import scala.language.dynamics
import runtime.undefined

/** An immutable unsafe pointer to unmanaged memory. */
final class Ptr[T] private () {
  /** Dereference a pointer. */
  def unary_! : T = undefined

  /** Store a value to the address pointed at by a pointer. */
  def := (value: T) = undefined

  /** Compute a derived pointer with given offset. */
  def apply(offset: CSize): Ptr[T] = undefined
}
