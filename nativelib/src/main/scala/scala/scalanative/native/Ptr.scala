// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Ptr.scala.gyb", line: 1)
package scala.scalanative
package native

import runtime._

/** The C `const T *` pointer. */
final abstract class Ptr[T] {

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

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Ptr.scala.gyb", line: 28)

  /** Get a derived pointer to the 1-th field of the struct. */
  def _1[F](implicit T: Tag[T], F: CField1[T, F]): Ptr[F] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Ptr.scala.gyb", line: 28)

  /** Get a derived pointer to the 2-th field of the struct. */
  def _2[F](implicit T: Tag[T], F: CField2[T, F]): Ptr[F] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Ptr.scala.gyb", line: 28)

  /** Get a derived pointer to the 3-th field of the struct. */
  def _3[F](implicit T: Tag[T], F: CField3[T, F]): Ptr[F] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Ptr.scala.gyb", line: 28)

  /** Get a derived pointer to the 4-th field of the struct. */
  def _4[F](implicit T: Tag[T], F: CField4[T, F]): Ptr[F] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Ptr.scala.gyb", line: 28)

  /** Get a derived pointer to the 5-th field of the struct. */
  def _5[F](implicit T: Tag[T], F: CField5[T, F]): Ptr[F] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Ptr.scala.gyb", line: 28)

  /** Get a derived pointer to the 6-th field of the struct. */
  def _6[F](implicit T: Tag[T], F: CField6[T, F]): Ptr[F] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Ptr.scala.gyb", line: 28)

  /** Get a derived pointer to the 7-th field of the struct. */
  def _7[F](implicit T: Tag[T], F: CField7[T, F]): Ptr[F] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Ptr.scala.gyb", line: 28)

  /** Get a derived pointer to the 8-th field of the struct. */
  def _8[F](implicit T: Tag[T], F: CField8[T, F]): Ptr[F] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Ptr.scala.gyb", line: 28)

  /** Get a derived pointer to the 9-th field of the struct. */
  def _9[F](implicit T: Tag[T], F: CField9[T, F]): Ptr[F] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Ptr.scala.gyb", line: 28)

  /** Get a derived pointer to the 10-th field of the struct. */
  def _10[F](implicit T: Tag[T], F: CField10[T, F]): Ptr[F] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Ptr.scala.gyb", line: 28)

  /** Get a derived pointer to the 11-th field of the struct. */
  def _11[F](implicit T: Tag[T], F: CField11[T, F]): Ptr[F] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Ptr.scala.gyb", line: 28)

  /** Get a derived pointer to the 12-th field of the struct. */
  def _12[F](implicit T: Tag[T], F: CField12[T, F]): Ptr[F] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Ptr.scala.gyb", line: 28)

  /** Get a derived pointer to the 13-th field of the struct. */
  def _13[F](implicit T: Tag[T], F: CField13[T, F]): Ptr[F] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Ptr.scala.gyb", line: 28)

  /** Get a derived pointer to the 14-th field of the struct. */
  def _14[F](implicit T: Tag[T], F: CField14[T, F]): Ptr[F] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Ptr.scala.gyb", line: 28)

  /** Get a derived pointer to the 15-th field of the struct. */
  def _15[F](implicit T: Tag[T], F: CField15[T, F]): Ptr[F] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Ptr.scala.gyb", line: 28)

  /** Get a derived pointer to the 16-th field of the struct. */
  def _16[F](implicit T: Tag[T], F: CField16[T, F]): Ptr[F] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Ptr.scala.gyb", line: 28)

  /** Get a derived pointer to the 17-th field of the struct. */
  def _17[F](implicit T: Tag[T], F: CField17[T, F]): Ptr[F] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Ptr.scala.gyb", line: 28)

  /** Get a derived pointer to the 18-th field of the struct. */
  def _18[F](implicit T: Tag[T], F: CField18[T, F]): Ptr[F] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Ptr.scala.gyb", line: 28)

  /** Get a derived pointer to the 19-th field of the struct. */
  def _19[F](implicit T: Tag[T], F: CField19[T, F]): Ptr[F] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Ptr.scala.gyb", line: 28)

  /** Get a derived pointer to the 20-th field of the struct. */
  def _20[F](implicit T: Tag[T], F: CField20[T, F]): Ptr[F] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Ptr.scala.gyb", line: 28)

  /** Get a derived pointer to the 21-th field of the struct. */
  def _21[F](implicit T: Tag[T], F: CField21[T, F]): Ptr[F] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Ptr.scala.gyb", line: 28)

  /** Get a derived pointer to the 22-th field of the struct. */
  def _22[F](implicit T: Tag[T], F: CField22[T, F]): Ptr[F] = undefined

// ###sourceLocation(file: "/Users/Denys/.src/native/nativelib/src/main/scala/scala/scalanative/native/Ptr.scala.gyb", line: 33)
}
