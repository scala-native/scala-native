package scala.scalanative

import java.nio.file.Path

package object io {
  private[io] def jIteratorToSeq[T](it: java.util.Iterator[T]): Seq[T] = {
    val buf = Seq.newBuilder[T]
    while (it.hasNext) {
      buf += it.next()
    }
    buf.result()
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
