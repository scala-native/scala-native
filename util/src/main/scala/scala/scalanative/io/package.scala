package scala.scalanative

import java.nio.file.Path
import scala.collection.JavaConverters._

package object io {
  def packageNameFromPath(path: Path): String = {
    val fileName = path.getFileName.toString
    val base     = fileName.split('.').init.mkString(".")

    Option(path.getParent) match {
      case Some(parent) => parent.resolve(base).asScala.mkString(".")
      case None         => base
    }
  }
}
