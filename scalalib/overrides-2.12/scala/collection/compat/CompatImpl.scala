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

package scala.collection.compat

import scala.reflect.ClassTag
import scala.collection.generic.CanBuildFrom
import scala.collection.{immutable => i, mutable => m}
import scala.language.higherKinds

/* builder optimized for a single ++= call, which returns identity on result if possible
 * and defers to the underlying builder if not.
 */
private final class IdentityPreservingBuilder[A, CC[X] <: TraversableOnce[X]](
  that: m.Builder[A, CC[A]])(implicit ct: ClassTag[CC[A]])
  extends m.Builder[A, CC[A]] {

  //invariant: ruined => (collection == null)
  var collection: CC[A] = null.asInstanceOf[CC[A]]
  var ruined            = false

  private[this] def ruin(): Unit = {
	if (collection != null) that ++= collection
	collection = null.asInstanceOf[CC[A]]
	ruined = true
  }

  override def ++=(elems: TraversableOnce[A]): this.type =
	elems match {
	  case ct(ca) if collection == null && !ruined => {
		collection = ca
		this
	  }
	  case _ => {
		ruin()
		that ++= elems
		this
	  }
	}

  def +=(elem: A): this.type = {
	ruin()
	that += elem
	this
  }

  def clear(): Unit = {
	collection = null.asInstanceOf[CC[A]]
	if (ruined) that.clear()
	ruined = false
  }

  def result(): CC[A] = if (collection == null) that.result() else collection
}

private[compat] object CompatImpl {
  def simpleCBF[A, C](f: => m.Builder[A, C]): CanBuildFrom[Any, A, C] =
	new CanBuildFrom[Any, A, C] {
	  def apply(from: Any): m.Builder[A, C] = apply()
	  def apply(): m.Builder[A, C]          = f
	}

  type ImmutableBitSetCC[X] = ({ type L[_] = i.BitSet })#L[X]
  type MutableBitSetCC[X]   = ({ type L[_] = m.BitSet })#L[X]
}