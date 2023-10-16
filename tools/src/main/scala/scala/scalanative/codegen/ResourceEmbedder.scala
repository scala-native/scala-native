package scala.scalanative.codegen

import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitResult._
import java.nio.file.Files
import java.nio.file.Files._
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.FileSystems
import java.util.EnumSet
import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.scalanative.build.Config
import scala.scalanative.io.VirtualDirectory
import scala.scalanative.nir._
import scala.scalanative.util.Scope

private[scalanative] object ResourceEmbedder {

  private case class ClasspathFile(
      accessPath: Path,
      pathName: String,
      classpathDirectory: VirtualDirectory
  )

  private final val ScalaNativeExcludePattern: String = "/scala-native/**"

  def apply(config: Config): Seq[Defn.Var] = Scope { implicit scope =>
    val classpath = config.classPath
    val fs = FileSystems.getDefault()
    val includePatterns =
      config.compilerConfig.resourceIncludePatterns.map(p =>
        fs.getPathMatcher(s"glob:$p")
      )
    val excludePatterns =
      (config.compilerConfig.resourceExcludePatterns :+ ScalaNativeExcludePattern)
        .map(p => fs.getPathMatcher(s"glob:$p"))

    implicit val position: Position = Position.NoPosition

    val notInIncludePatterns =
      s"Not matched by any include path ${includePatterns.mkString}"
    type IgnoreReason = String

    /** If the return value is defined, the given path should be ignored. If
     *  it's None, the path should be included.
     */
    def shouldIgnore(path: Path): Option[IgnoreReason] =
      includePatterns.find(_.matches(path)).fold(Option(notInIncludePatterns)) {
        includePattern =>
          excludePatterns
            .find(_.matches(path))
            .map(excludePattern =>
              s"Matched by '$includePattern', but excluded by '$excludePattern'"
            )
      }

    val foundFiles =
      if (config.compilerConfig.embedResources) {
        classpath.flatMap { classpath =>
          val virtualDir = VirtualDirectory.real(classpath)

          virtualDir.files
            .flatMap { path =>
              // Use the same path separator on all OSs
              val pathString = path.toString().replace(File.separator, "/")
              val (pathName, correctedPath) =
                if (!pathString.startsWith("/")) { // local file
                  ("/" + pathString, classpath.resolve(path))
                } else { // other file (f.e in jar)
                  (pathString, path)
                }

              shouldIgnore(path) match {
                case Some(reason) =>
                  config.logger.debug(s"Did not embed: $pathName - $reason")
                  None
                case None =>
                  if (isSourceFile((path))) None
                  else if (Files.isDirectory(correctedPath)) None
                  else Some(ClasspathFile(path, pathName, virtualDir))
              }
            }
        }
      } else Seq.empty

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
        val encodedPath = pathName.toString.getBytes().map(Val.Byte(_))
        Val.ArrayValue(Type.Byte, encodedPath.toSeq)
    }

    val contentValues = embeddedFiles.map {
      case ClasspathFile(accessPath, pathName, virtDir) =>
        val fileBuffer = virtDir.read(accessPath)
        val encodedContent = fileBuffer.array().map(Val.Byte(_))
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

  private def extern(id: String): Global.Member =
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
}
