package scala.scalanative.codegen

import java.nio.file.Files._
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.EnumSet
import java.nio.file.Files
import java.nio.file.Path
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitResult._
import scala.collection.mutable.ArrayBuffer
import scala.scalanative.nir._
import java.util.Base64
import java.nio.ByteBuffer
import scala.scalanative.build.Config
import scala.scalanative.io.VirtualDirectory
import scala.scalanative.util.Scope
import java.nio.file.Paths
import scala.collection.mutable
import scala.annotation.tailrec

private[scalanative] object ResourceEmbedder {

  private case class ClasspathFile(
      accessPath: Path,
      pathName: String,
      classpathDirectory: VirtualDirectory
  )

  def apply(config: Config): Seq[Defn.Var] = Scope { implicit scope =>
    val classpath = config.classPath

    implicit val position: Position = Position.NoPosition

    val foundFiles =
      if (config.compilerConfig.embedResources) {
        classpath.flatMap { classpath =>
          val virtualDir = VirtualDirectory.real(classpath)

          virtualDir.files
            .flatMap { path =>
              val pathName =
                if (!path.toString().startsWith("/")) { // local file
                  "/" + path.toString()
                } else { // other file (f.e in jar)
                  path.toString()
                }

              if (isInIgnoredDirectory(path)) {
                config.logger.debug(
                  s"Did not embed: $pathName. Source file extension detected."
                )
                None
              } else if (isSourceFile(path)) {
                config.logger.debug(
                  s"Did not embed: $pathName. File in the ignored scala-native folder."
                )
                None
              } else if (Files.isDirectory(path)) {
                None
              } else {
                Some(ClasspathFile(path, pathName, virtualDir))
              }
            }
        }
      } else {
        Seq()
      }

    def filterEqualPathNames(
        path: List[ClasspathFile]
    ): List[ClasspathFile] = {
      path
        .foldLeft((Set.empty[String], List.empty[ClasspathFile])) {
          case ((visited, filtered), (file @ ClasspathFile(_, pathName, _)))
              if !visited.contains(pathName) =>
            (visited + pathName, file :: filtered)
          case (state, _) => state
        }
        ._2
    }

    val embeddedFiles = filterEqualPathNames(foundFiles.toList)

    val pathValues = embeddedFiles.map {
      case ClasspathFile(accessPath, pathName, virtDir) =>
        val encodedPath = Base64.getEncoder
          .encode(pathName.toString.getBytes())
          .map(Val.Byte(_))
        Val.ArrayValue(Type.Byte, encodedPath.toSeq)
    }

    val contentValues = embeddedFiles.map {
      case ClasspathFile(accessPath, pathName, virtDir) =>
        val fileBuffer = virtDir.read(accessPath)
        val encodedContent = Base64.getEncoder
          .encode(fileBuffer.array())
          .map(Val.Byte(_))
        Val.ArrayValue(Type.Byte, encodedContent.toSeq)
    }

    def generateArrayVar(name: String, arrayValue: Val.ArrayValue) = {
      Defn.Var(
        Attrs.None,
        extern(name),
        Type.Ptr,
        Val.Const(
          arrayValue
        )
      )
    }

    def generateExtern2DArray(name: String, content: Seq[Val.Const]) = {
      generateArrayVar(
        name,
        Val.ArrayValue(
          Type.Ptr,
          content
        )
      )
    }

    def generateExternLongArray(name: String, content: Seq[Val.Int]) = {
      generateArrayVar(
        name,
        Val.ArrayValue(
          Type.Int,
          content
        )
      )
    }

    val generated =
      Seq(
        generateExtern2DArray(
          "__scala_native_resources_all_path",
          pathValues.toIndexedSeq.map(Val.Const(_))
        ),
        generateExtern2DArray(
          "__scala_native_resources_all_content",
          contentValues.toIndexedSeq.map(Val.Const(_))
        ),
        generateExternLongArray(
          "__scala_native_resources_all_path_lengths",
          pathValues.toIndexedSeq.map(path => Val.Int(path.values.length))
        ),
        generateExternLongArray(
          "__scala_native_resources_all_content_lengths",
          contentValues.toIndexedSeq.map(content =>
            Val.Int(content.values.length)
          )
        ),
        Defn.Var(
          Attrs.None,
          extern("__scala_native_resources_amount"),
          Type.Ptr,
          Val.Int(contentValues.length)
        )
      )

    embeddedFiles.foreach { classpathFile =>
      config.logger.info("Embedded resource: " + classpathFile.pathName)
    }

    generated
  }

  private def extern(id: String): Global =
    Global.Member(Global.Top("__"), Sig.Extern(id))

  private val sourceExtensions =
    Seq(
      ".class",
      ".tasty",
      ".nir",
      ".jar",
      ".scala",
      ".java"
    )

  private def isSourceFile(path: Path): Boolean = {
    if (path.getFileName == null) false
    else sourceExtensions.exists(path.getFileName.toString.endsWith(_))
  }

  private def isInIgnoredDirectory(path: Path): Boolean = {
    path.startsWith("/scala-native/")
  }

}
