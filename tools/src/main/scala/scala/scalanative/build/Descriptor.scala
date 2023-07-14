package scala.scalanative.build

import java.io.FileReader
import java.nio.file.Path
import java.util.Properties

import scala.util.Try
import java.io.Reader

final case class Descriptor(
    groupId: Option[String],
    artifactId: Option[String],
    link: List[String]
)

object Descriptor {

  def load(path: Path): Try[Descriptor] = Try {
    var reader: Reader = null
    try {
      reader = new FileReader(path.toFile())
      val props = new Properties()
      props.load(reader)
      Descriptor(
        Option(props.getProperty("project.groupId")),
        Option(props.getProperty("project.artifactId")),
        Option(props.getProperty("nir.link.names")) match {
          case Some(value) => value.split(',').map(_.trim()).toList
          case None        => List.empty
        }
      )
    } finally {
      if (reader != null) {
        try {
          reader.close()
        } catch {
          case t: Throwable =>
        }
      }
    }
  }
}
