/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */


/**
 * Core Scala types. They are always available without an explicit import.
 * @contentDiagram hideNodes "scala.Serializable"
 */
package object scala {
  type Throwable = java.lang.Throwable
  type Exception = java.lang.Exception
  type Error     = java.lang.Error

  type RuntimeException                = java.lang.RuntimeException
  type NullPointerException            = java.lang.NullPointerException
  type ClassCastException              = java.lang.ClassCastException
  type IndexOutOfBoundsException       = java.lang.IndexOutOfBoundsException
  type ArrayIndexOutOfBoundsException  = java.lang.ArrayIndexOutOfBoundsException
  type StringIndexOutOfBoundsException = java.lang.StringIndexOutOfBoundsException
  type UnsupportedOperationException   = java.lang.UnsupportedOperationException
  type IllegalArgumentException        = java.lang.IllegalArgumentException
  type NoSuchElementException          = java.util.NoSuchElementException
  type NumberFormatException           = java.lang.NumberFormatException
  type AbstractMethodError             = java.lang.AbstractMethodError
  type InterruptedException            = java.lang.InterruptedException

  // A dummy used by the specialization annotation.
  lazy val AnyRef = new Specializable {
    override def toString = "object AnyRef"
  }

  type TraversableOnce[+A] = scala.collection.TraversableOnce[A]

  type Traversable[+A] = scala.collection.Traversable[A]
  lazy val Traversable = scala.collection.Traversable

  type Iterable[+A] = scala.collection.Iterable[A]
  lazy val Iterable = scala.collection.Iterable

  type Seq[+A] = scala.collection.Seq[A]
  lazy val Seq = scala.collection.Seq

  type IndexedSeq[+A] = scala.collection.IndexedSeq[A]
  lazy val IndexedSeq = scala.collection.IndexedSeq

  type Iterator[+A] = scala.collection.Iterator[A]
  lazy val Iterator = scala.collection.Iterator

  type BufferedIterator[+A] = scala.collection.BufferedIterator[A]

  type List[+A] = scala.collection.immutable.List[A]
  lazy val List = scala.collection.immutable.List

  lazy val Nil = scala.collection.immutable.Nil

  type ::[A] = scala.collection.immutable.::[A]
  lazy val :: = scala.collection.immutable.::

  lazy val +: = scala.collection.+:
  lazy val :+ = scala.collection.:+

  type Stream[+A] = scala.collection.immutable.Stream[A]
  lazy val Stream = scala.collection.immutable.Stream
  lazy val #:: = scala.collection.immutable.Stream.#::

  type Vector[+A] = scala.collection.immutable.Vector[A]
  lazy val Vector = scala.collection.immutable.Vector

  type StringBuilder = scala.collection.mutable.StringBuilder
  lazy val StringBuilder = scala.collection.mutable.StringBuilder

  type Range = scala.collection.immutable.Range
  lazy val Range = scala.collection.immutable.Range

  // Numeric types which were moved into scala.math.*

  type BigDecimal = scala.math.BigDecimal
  lazy val BigDecimal = scala.math.BigDecimal

  type BigInt = scala.math.BigInt
  lazy val BigInt = scala.math.BigInt

  type Equiv[T] = scala.math.Equiv[T]
  lazy val Equiv = scala.math.Equiv

  type Fractional[T] = scala.math.Fractional[T]
  lazy val Fractional = scala.math.Fractional

  type Integral[T] = scala.math.Integral[T]
  lazy val Integral = scala.math.Integral

  type Numeric[T] = scala.math.Numeric[T]
  lazy val Numeric = scala.math.Numeric

  type Ordered[T] = scala.math.Ordered[T]
  lazy val Ordered = scala.math.Ordered

  type Ordering[T] = scala.math.Ordering[T]
  lazy val Ordering = scala.math.Ordering

  type PartialOrdering[T] = scala.math.PartialOrdering[T]
  type PartiallyOrdered[T] = scala.math.PartiallyOrdered[T]

  type Either[+A, +B] = scala.util.Either[A, B]
  lazy val Either = scala.util.Either

  type Left[+A, +B] = scala.util.Left[A, B]
  lazy val Left = scala.util.Left

  type Right[+A, +B] = scala.util.Right[A, B]
  lazy val Right = scala.util.Right

  // Annotations which we might move to annotation.*
/*
  type SerialVersionUID = annotation.SerialVersionUID
  type deprecated = annotation.deprecated
  type deprecatedName = annotation.deprecatedName
  type inline = annotation.inline
  type native = annotation.native
  type noinline = annotation.noinline
  type remote = annotation.remote
  type specialized = annotation.specialized
  type transient = annotation.transient
  type throws  = annotation.throws
  type unchecked = annotation.unchecked.unchecked
  type volatile = annotation.volatile
  */
}
