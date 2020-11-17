package scala.scalanative.compat
import scala.language.implicitConversions

object StreamsCompat {
  type SStream[T] = scalanative.compat.ScalaStream.Underlying[T]
  val SStreamImpl = scalanative.compat.ScalaStream
  val SStream     = SStreamImpl.Underlying

  implicit class ArrayToScalaStream[T](val arr: Array[T]) extends AnyVal {
    def toScalaStream: SStream[T] = SStreamImpl.seqToScalaStream[T](arr)
  }

  implicit class IterableToScalaStream[T](val seq: Iterable[T]) extends AnyVal {
    def toScalaStream: SStream[T] = SStreamImpl.seqToScalaStream[T](seq)
  }

}
