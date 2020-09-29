package scala.scalanative
import scala.collection.mutable
import scala.language.implicitConversions

package object nir {
  implicit def bufferToSeq[T](buf: mutable.UnrolledBuffer[T]): Seq[T] =
    buf.toSeq
}
