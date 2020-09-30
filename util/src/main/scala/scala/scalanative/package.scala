package scala
import scala.collection.mutable
import scala.language.implicitConversions

package object scalanative {
  implicit def bufferToSeq[T](
      buf: mutable.UnrolledBuffer[T]): collection.Seq[T] =
    buf.toSeq

  implicit def seqToImmutableSeq[T](
      seq: collection.Seq[T]): collection.immutable.Seq[T] = seq.toIndexedSeq
}
