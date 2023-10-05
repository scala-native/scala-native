package scala.scalanative.build

import java.io.FileReader
import java.nio.file.Path
import java.util.Properties

import scala.util.Try
import java.io.Reader
import scala.annotation.tailrec

final case class Descriptor(
    organization: Option[String],
    name: Option[String],
    gcProject: Boolean,
    links: List[String],
    defines: List[String],
    includes: List[String]
)

object Descriptor {

  def load(path: Path): Try[Descriptor] = Try {
    var reader: Reader = null
    try {
      reader = new FileReader(path.toFile())
      val props = new Properties()
      props.load(reader)
      Descriptor(
        Option(props.getProperty("project.organization")),
        Option(props.getProperty("project.name")),
        props.getProperty("project.gcProject", "false").toBoolean,
        parseStrings("nir.link.names", props),
        parseStrings("preprocessor.defines", props),
        parseStrings("compile.include.paths", props)
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

  private def parseStrings(prop: String, props: Properties): List[String] =
    Option(props.getProperty(prop)) match {
      case Some(value) => value.split(',').map(_.trim()).toList
      case None        => List.empty
    }

}
