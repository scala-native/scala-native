package scala.scalanative.compat

import java.util.stream.WrappedScalaStream
import scala.collection.immutable
import scala.language.implicitConversions

private[scalanative] object ScalaStream {
  type Underlying[T] = immutable.LazyList[T]
  val Underlying = immutable.LazyList

  implicit class ScalaStreamImpl[T](val underyling: Underlying[T])
      extends AnyVal {
    def wrappedStream(closeHanlder: Option[Runnable] = None) =
      new WrappedScalaStream[T](underyling, closeHanlder)
  }

  implicit def seqToScalaStream[T](seq: Iterable[T]): Underlying[T] = {
    seq.to(Underlying)
  }

}
