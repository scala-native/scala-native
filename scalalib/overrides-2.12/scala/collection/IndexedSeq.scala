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

package scala
package collection

import generic._
import mutable.Builder

/** A base trait for indexed sequences.
 *  $indexedSeqInfo
 */
trait IndexedSeq[+A] extends Seq[A]
                    with GenericTraversableTemplate[A, IndexedSeq]
                    with IndexedSeqLike[A, IndexedSeq[A]] {
  override def companion: GenericCompanion[IndexedSeq] = IndexedSeq
  override def seq: IndexedSeq[A] = this
}

/** $factoryInfo
 *  The current default implementation of a $Coll is a `Vector`.
 *  @define coll indexed sequence
 *  @define Coll `IndexedSeq`
 */
object IndexedSeq extends IndexedSeqFactory[IndexedSeq] {
  // A single CBF which can be checked against to identify
  // an indexed collection type.
  @inline override def ReusableCBF: GenericCanBuildFrom[Nothing] = ReusableCBFInstance
  private object ReusableCBFInstance extends GenericCanBuildFrom[Nothing] {
    @inline override def apply() = newBuilder[Nothing]
  }

  @inline def newBuilder[A]: Builder[A, IndexedSeq[A]] = immutable.IndexedSeq.newBuilder[A]
  @inline implicit def canBuildFrom[A]: CanBuildFrom[Coll, A, IndexedSeq[A]] =
    ReusableCBF.asInstanceOf[GenericCanBuildFrom[A]]
}
