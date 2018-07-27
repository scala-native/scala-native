/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */
package scala
package collection


import generic._


/** A trait for all iterable collections which may possibly
 *  have their operations implemented in parallel.
 *
 *  @author Martin Odersky
 *  @author Aleksandar Prokopec
 *  @since 2.9
 */
trait GenIterable[+A]
extends GenIterableLike[A, GenIterable[A]]
   with GenTraversable[A]
   with GenericTraversableTemplate[A, GenIterable]
{
  def seq: Iterable[A]
  override def companion: GenericCompanion[GenIterable] = GenIterable
}


object GenIterable extends GenTraversableFactory[GenIterable] {
  @inline override def ReusableCBF: GenericCanBuildFrom[Nothing] = ReusableCBFInstance
  private object ReusableCBFInstance extends GenericCanBuildFrom[Nothing] {
    @inline override def apply() = newBuilder[Nothing]
  }

  @inline implicit def canBuildFrom[A] = ReusableCBF.asInstanceOf[GenericCanBuildFrom[A]]
  @inline def newBuilder[A] = Iterable.newBuilder
}

