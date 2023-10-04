package scala.scalanative.build

import java.io.FileReader
import java.nio.file.Path
import java.util.Properties

import scala.util.Try
import java.io.Reader
import scala.annotation.tailrec

final case class Descriptor(
    groupId: Option[String],
    artifactId: Option[String],
    gcProject: Boolean,
    links: List[String],
    defines: List[String],
    includes: List[List[String]]
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
        props.getProperty("project.gcProject", "false").toBoolean,
        parseStrings("nir.link.names", props),
        parseStrings("preprocessor.defines", props),
        parseIncludePaths(props)
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

  private def parseIncludePaths(props: Properties): List[List[String]] = {
    @tailrec
    def createLists(acc: List[List[String]], index: Int): List[List[String]] = {
      val res =
        if (index == 0)
          parseStrings("compile.include.path", props)
        else
          parseStrings(s"compile.include.path$index", props)

      res match {
        case Nil             => acc
        case _: List[String] => createLists(res :: acc, index + 1)
      }
    }

    createLists(List[List[String]](), 0)
  }

}
