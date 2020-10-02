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


/** A trait for all sequences which may possibly
 *  have their operations implemented in parallel.
 *
 *  @author Martin Odersky
 *  @author Aleksandar Prokopec
 *  @since 2.9
 */
trait GenSeq[+A]
extends GenSeqLike[A, GenSeq[A]]
   with GenIterable[A]
   with Equals
   with GenericTraversableTemplate[A, GenSeq]
{
  def seq: Seq[A]
  override def companion: GenericCompanion[GenSeq] = GenSeq
}


object GenSeq extends GenTraversableFactory[GenSeq] {
  @inline override def ReusableCBF: GenericCanBuildFrom[Nothing] = ReusableCBFInstance
  private object ReusableCBFInstance extends GenericCanBuildFrom[Nothing] {
    @inline override def apply() = newBuilder[Nothing]
  }

  @inline implicit def canBuildFrom[A] = ReusableCBF.asInstanceOf[GenericCanBuildFrom[A]]
  @inline def newBuilder[A] = Seq.newBuilder
}
