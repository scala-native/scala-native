package scala.collection

package object compat {
  type ScalaStream[+T] = scala.collection.immutable.LazyList[T]
  val ScalaStream = scala.collection.immutable.LazyList
}