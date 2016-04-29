package scala.scalanative
package native

import runtime.undefined

final class Ptr[+T] private () extends Dynamic {
  /** Dereference a pointer. */
  def unary_! : T = undefined

  /** Store a value to the address pointed at by a pointer. */
  def := (value: T) = undefined

  /** Compute a derived pointer with given offset. */
  def apply(offset: Int): T = undefined

  /** Compute a derived pointer to given field. */
  def selectDynamic(field: String): Ptr[Any] = undefined

  /** Compute a derived pointer to given field plus offset. */
  def applyDynamic(field: String)(offset: Int): Ptr[Any] = undefined
}
