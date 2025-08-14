package scala.scalanative.build

import java.io.FileReader
import java.nio.file.Path
import java.util.Properties

import scala.util.Try
import java.io.Reader
import scala.annotation.tailrec

private[build] final case class Descriptor(
    organization: Option[String],
    name: Option[String],
    gcProject: Boolean,
    links: Seq[String],
    defines: Seq[String],
    includes: Seq[String],
    cOptions: Seq[String],
    cppOptions: Seq[String]
)

private[build] object Descriptor {

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
        parseStrings("compile.include.paths", props),
        parseStrings("compile.c.options", props),
        parseStrings("compile.cpp.options", props)
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

  private def parseStrings(prop: String, props: Properties): Seq[String] =
    Option(props.getProperty(prop)) match {
      case Some(value) => value.split(',').map(_.trim()).toSeq
      case None        => Seq.empty
    }

}
