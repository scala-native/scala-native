package scala.scalanative

import java.nio.file.Path
import scala.collection.mutable
import scala.reflect.ClassTag

package object io {
  private[io] def jIteratorToSeq[T: ClassTag](
      it: java.util.Iterator[T]): Seq[T] = {
    val buf = mutable.UnrolledBuffer.empty[T]
    while (it.hasNext) {
      buf += it.next()
    }
    buf.toSeq
  }
  def packageNameFromPath(path: Path): String = {
    val fileName = path.getFileName.toString
    val base     = fileName.split('.').init.mkString(".")

    Option(path.getParent) match {
      case Some(parent) =>
        jIteratorToSeq {
          parent.resolve(base).iterator()
        }.mkString(".")
      case None => base
    }
  }
}
