/*
 * Scala (https://www.scala-lang.org)
 *
 * Copyright EPFL and Lightbend, Inc.
 *
 * Licensed under Apache License 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package scala.collection.compat.immutable

import java.util.Arrays

import scala.annotation.unchecked.uncheckedVariance
import scala.collection.AbstractSeq
import scala.collection.generic._
import scala.collection.immutable.IndexedSeq
import scala.collection.mutable.{ArrayBuilder, Builder, WrappedArrayBuilder}
import scala.reflect.ClassTag
import scala.util.hashing.MurmurHash3

/**
 * An immutable array.
 *
 * Supports efficient indexed access and has a small memory footprint.
 *
 *  @define Coll `ArraySeq`
 *  @define coll wrapped array
 *  @define orderDependent
 *  @define orderDependentFold
 *  @define mayNotTerminateInf
 *  @define willNotTerminateInf
 */
abstract class ArraySeq[+T] extends AbstractSeq[T] with IndexedSeq[T] {

  override protected[this] def thisCollection: ArraySeq[T] = this

  /** The tag of the element type */
  protected[this] def elemTag: ClassTag[T]

  /** The length of the array */
  def length: Int

  /** The element at given index */
  def apply(index: Int): T

  /** The underlying array */
  def unsafeArray: Array[T @uncheckedVariance]

  override def stringPrefix = "ArraySeq"

  /** Clones this object, including the underlying Array. */
  override def clone(): ArraySeq[T] = ArraySeq unsafeWrapArray unsafeArray.clone()

  /** Creates new builder for this collection ==> move to subclasses
   */
  override protected[this] def newBuilder: Builder[T, ArraySeq[T]] =
	ArraySeq.newBuilder[T](elemTag)

}

/** A companion object used to create instances of `ArraySeq`.
 */
object ArraySeq {
  // This is reused for all calls to empty.
  private val EmptyArraySeq           = new ofRef[AnyRef](new Array[AnyRef](0))
  def empty[T <: AnyRef]: ArraySeq[T] = EmptyArraySeq.asInstanceOf[ArraySeq[T]]

  def newBuilder[T](implicit elemTag: ClassTag[T]): Builder[T, ArraySeq[T]] =
	new WrappedArrayBuilder[T](elemTag).mapResult(w => unsafeWrapArray(w.array))

  def apply[T](elems: T*)(implicit elemTag: ClassTag[T]): ArraySeq[T] = {
	val b = newBuilder[T]
	b ++= elems
	b.result()
  }

  def unapplySeq[T](seq: ArraySeq[T]): Some[ArraySeq[T]] = Some(seq)

  /**
   * Wrap an existing `Array` into an `ArraySeq` of the proper primitive specialization type
   * without copying.
   *
   * Note that an array containing boxed primitives can be wrapped in an `ArraySeq` without
   * copying. For example, `val a: Array[Any] = Array(1)` is an array of `Object` at runtime,
   * containing `Integer`s. An `ArraySeq[Int]` can be obtained with a cast:
   * `ArraySeq.unsafeWrapArray(a).asInstanceOf[ArraySeq[Int]]`. The values are still
   * boxed, the resulting instance is an [[ArraySeq.ofRef]]. Writing
   * `ArraySeq.unsafeWrapArray(a.asInstanceOf[Array[Int]])` does not work, it throws a
   * `ClassCastException` at runtime.
   */
  def unsafeWrapArray[T](x: Array[T]): ArraySeq[T] =
	(x.asInstanceOf[Array[_]] match {
	  case null              => null
	  case x: Array[AnyRef]  => new ofRef[AnyRef](x)
	  case x: Array[Int]     => new ofInt(x)
	  case x: Array[Double]  => new ofDouble(x)
	  case x: Array[Long]    => new ofLong(x)
	  case x: Array[Float]   => new ofFloat(x)
	  case x: Array[Char]    => new ofChar(x)
	  case x: Array[Byte]    => new ofByte(x)
	  case x: Array[Short]   => new ofShort(x)
	  case x: Array[Boolean] => new ofBoolean(x)
	  case x: Array[Unit]    => new ofUnit(x)
	}).asInstanceOf[ArraySeq[T]]

  implicit def canBuildFrom[T](implicit m: ClassTag[T]): CanBuildFrom[ArraySeq[_], T, ArraySeq[T]] =
	new CanBuildFrom[ArraySeq[_], T, ArraySeq[T]] {
	  def apply(from: ArraySeq[_]): Builder[T, ArraySeq[T]] =
		ArrayBuilder.make[T]()(m) mapResult ArraySeq.unsafeWrapArray[T]
	  def apply: Builder[T, ArraySeq[T]] =
		ArrayBuilder.make[T]()(m) mapResult ArraySeq.unsafeWrapArray[T]
	}

  @SerialVersionUID(3L)
  final class ofRef[T <: AnyRef](val unsafeArray: Array[T]) extends ArraySeq[T] with Serializable {
	lazy val elemTag         = ClassTag[T](unsafeArray.getClass.getComponentType)
	def length: Int          = unsafeArray.length
	def apply(index: Int): T = unsafeArray(index)
	def update(index: Int, elem: T) { unsafeArray(index) = elem }
	override def hashCode = MurmurHash3.arrayHash(unsafeArray, MurmurHash3.seqSeed)
	override def equals(that: Any) = that match {
	  case that: ofRef[_] =>
		arrayEquals(unsafeArray.asInstanceOf[Array[AnyRef]],
		  that.unsafeArray.asInstanceOf[Array[AnyRef]])
	  case _ => super.equals(that)
	}
  }

  @SerialVersionUID(3L)
  final class ofByte(val unsafeArray: Array[Byte]) extends ArraySeq[Byte] with Serializable {
	def elemTag                 = ClassTag.Byte
	def length: Int             = unsafeArray.length
	def apply(index: Int): Byte = unsafeArray(index)
	def update(index: Int, elem: Byte) { unsafeArray(index) = elem }
	override def hashCode = MurmurHash3.arrayHash(unsafeArray, MurmurHash3.seqSeed)
	override def equals(that: Any) = that match {
	  case that: ofByte => Arrays.equals(unsafeArray, that.unsafeArray)
	  case _            => super.equals(that)
	}
  }

  @SerialVersionUID(3L)
  final class ofShort(val unsafeArray: Array[Short]) extends ArraySeq[Short] with Serializable {
	def elemTag                  = ClassTag.Short
	def length: Int              = unsafeArray.length
	def apply(index: Int): Short = unsafeArray(index)
	def update(index: Int, elem: Short) { unsafeArray(index) = elem }
	override def hashCode = MurmurHash3.arrayHash(unsafeArray, MurmurHash3.seqSeed)
	override def equals(that: Any) = that match {
	  case that: ofShort => Arrays.equals(unsafeArray, that.unsafeArray)
	  case _             => super.equals(that)
	}
  }

  @SerialVersionUID(3L)
  final class ofChar(val unsafeArray: Array[Char]) extends ArraySeq[Char] with Serializable {
	def elemTag                 = ClassTag.Char
	def length: Int             = unsafeArray.length
	def apply(index: Int): Char = unsafeArray(index)
	def update(index: Int, elem: Char) { unsafeArray(index) = elem }
	override def hashCode = MurmurHash3.arrayHash(unsafeArray, MurmurHash3.seqSeed)
	override def equals(that: Any) = that match {
	  case that: ofChar => Arrays.equals(unsafeArray, that.unsafeArray)
	  case _            => super.equals(that)
	}
  }

  @SerialVersionUID(3L)
  final class ofInt(val unsafeArray: Array[Int]) extends ArraySeq[Int] with Serializable {
	def elemTag                = ClassTag.Int
	def length: Int            = unsafeArray.length
	def apply(index: Int): Int = unsafeArray(index)
	def update(index: Int, elem: Int) { unsafeArray(index) = elem }
	override def hashCode = MurmurHash3.arrayHash(unsafeArray, MurmurHash3.seqSeed)
	override def equals(that: Any) = that match {
	  case that: ofInt => Arrays.equals(unsafeArray, that.unsafeArray)
	  case _           => super.equals(that)
	}
  }

  @SerialVersionUID(3L)
  final class ofLong(val unsafeArray: Array[Long]) extends ArraySeq[Long] with Serializable {
	def elemTag                 = ClassTag.Long
	def length: Int             = unsafeArray.length
	def apply(index: Int): Long = unsafeArray(index)
	def update(index: Int, elem: Long) { unsafeArray(index) = elem }
	override def hashCode = MurmurHash3.arrayHash(unsafeArray, MurmurHash3.seqSeed)
	override def equals(that: Any) = that match {
	  case that: ofLong => Arrays.equals(unsafeArray, that.unsafeArray)
	  case _            => super.equals(that)
	}
  }

  @SerialVersionUID(3L)
  final class ofFloat(val unsafeArray: Array[Float]) extends ArraySeq[Float] with Serializable {
	def elemTag                  = ClassTag.Float
	def length: Int              = unsafeArray.length
	def apply(index: Int): Float = unsafeArray(index)
	def update(index: Int, elem: Float) { unsafeArray(index) = elem }
	override def hashCode = MurmurHash3.arrayHash(unsafeArray, MurmurHash3.seqSeed)
	override def equals(that: Any) = that match {
	  case that: ofFloat => Arrays.equals(unsafeArray, that.unsafeArray)
	  case _             => super.equals(that)
	}
  }

  @SerialVersionUID(3L)
  final class ofDouble(val unsafeArray: Array[Double]) extends ArraySeq[Double] with Serializable {
	def elemTag                   = ClassTag.Double
	def length: Int               = unsafeArray.length
	def apply(index: Int): Double = unsafeArray(index)
	def update(index: Int, elem: Double) { unsafeArray(index) = elem }
	override def hashCode = MurmurHash3.arrayHash(unsafeArray, MurmurHash3.seqSeed)
	override def equals(that: Any) = that match {
	  case that: ofDouble => Arrays.equals(unsafeArray, that.unsafeArray)
	  case _              => super.equals(that)
	}
  }

  @SerialVersionUID(3L)
  final class ofBoolean(val unsafeArray: Array[Boolean])
	extends ArraySeq[Boolean]
	  with Serializable {
	def elemTag                    = ClassTag.Boolean
	def length: Int                = unsafeArray.length
	def apply(index: Int): Boolean = unsafeArray(index)
	def update(index: Int, elem: Boolean) { unsafeArray(index) = elem }
	override def hashCode = MurmurHash3.arrayHash(unsafeArray, MurmurHash3.seqSeed)
	override def equals(that: Any) = that match {
	  case that: ofBoolean => Arrays.equals(unsafeArray, that.unsafeArray)
	  case _               => super.equals(that)
	}
  }

  @SerialVersionUID(3L)
  final class ofUnit(val unsafeArray: Array[Unit]) extends ArraySeq[Unit] with Serializable {
	def elemTag                 = ClassTag.Unit
	def length: Int             = unsafeArray.length
	def apply(index: Int): Unit = unsafeArray(index)
	def update(index: Int, elem: Unit) { unsafeArray(index) = elem }
	override def hashCode = MurmurHash3.arrayHash(unsafeArray, MurmurHash3.seqSeed)
	override def equals(that: Any) = that match {
	  case that: ofUnit => unsafeArray.length == that.unsafeArray.length
	  case _            => super.equals(that)
	}
  }

  private[this] def arrayEquals(xs: Array[AnyRef], ys: Array[AnyRef]): Boolean = {
	if (xs eq ys)
	  return true
	if (xs.length != ys.length)
	  return false

	val len = xs.length
	var i   = 0
	while (i < len) {
	  if (xs(i) != ys(i))
		return false
	  i += 1
	}
	true
  }
}