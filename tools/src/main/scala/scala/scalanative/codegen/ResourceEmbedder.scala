package scala.scalanative.embedder

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

object ResourceEmbedder {

  final case class ClasspathFile(
      accessPath: Path,
      pathName: String,
      classpathDirectory: VirtualDirectory
  )

  def apply(config: Config)(implicit scope: Scope): Seq[Defn.Var] = {
    val classpath = config.classPath

    implicit val position: Position = Position.NoPosition

    val foundFiles =
      if (config.compilerConfig.embedResources) {
        classpath.flatMap { classpath =>
          val virtualDir = VirtualDirectory.real(classpath)
          val root = virtualDir.uri.getPath()

          virtualDir.files
            .flatMap { relativePath =>
              val (path, pathName) =
                if (root != null) { // local file
                  val name = s"${root}${relativePath}"
                  (Paths.get(name), s"/${relativePath.toString()}")
                } else { // other file (f.e in jar)
                  (relativePath, relativePath.toString)
                }

              if (isSourceFile(path)) {
                config.logger.debug(
                  s"Did not embed: $pathName. Source file extension detected."
                )
                None
              } else {
                if (Files.isRegularFile(path)) {
                  Some(ClasspathFile(path, pathName, virtualDir))
                } else {
                  None
                }
              }
            }
        }
      } else {
        Seq()
      }

    @tailrec
    def filterEqualPathNames(
        path: List[ClasspathFile],
        visitedPathNames: Set[String],
        filteredFiles: List[ClasspathFile]
    ): List[ClasspathFile] = {
      path match {
        case (file @ ClasspathFile(_, pathName, _)) :: tail =>
          if (visitedPathNames.contains(pathName)) {
            filterEqualPathNames(tail, visitedPathNames, filteredFiles)
          } else {
            filterEqualPathNames(
              tail,
              visitedPathNames + pathName,
              filteredFiles.::(file)
            )
          }
        case Nil =>
          filteredFiles
      }
    }

    val embeddedFiles = filterEqualPathNames(foundFiles.toList, Set.empty, Nil)

    val pathValues = embeddedFiles.map {
      case ClasspathFile(accessPath, pathName, virtDir) =>
        val encodedPath = Base64.getEncoder
          .encode(pathName.toString.getBytes())
          .map(a => Val.Int(a))
        Val.ArrayValue(Type.Int, encodedPath.toIndexedSeq)
    }

    val contentValues = embeddedFiles.map {
      case ClasspathFile(accessPath, pathName, virtDir) =>
        val fileBuffer = virtDir.read(accessPath)
        val encodedContent = Base64.getEncoder
          .encode(fileBuffer.array())
          .map(a => Val.Int(a))
        Val.ArrayValue(Type.Int, encodedContent.toIndexedSeq)
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

    def generateExtern2DArray(name: String, content: IndexedSeq[Val.Const]) = {
      generateArrayVar(
        name,
        Val.ArrayValue(
          Type.Ptr,
          content
        )
      )
    }

    def generateExternLongArray(name: String, content: IndexedSeq[Val.Int]) = {
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
          "__resources_all_path",
          pathValues.toIndexedSeq.map(Val.Const(_))
        ),
        generateExtern2DArray(
          "__resources_all_content",
          contentValues.toIndexedSeq.map(Val.Const(_))
        ),
        generateExternLongArray(
          "__resources_all_path_lengths",
          pathValues.toIndexedSeq.map(path => Val.Int(path.values.length))
        ),
        generateExternLongArray(
          "__resources_all_content_lengths",
          contentValues.toIndexedSeq.map(content =>
            Val.Int(content.values.length)
          )
        ),
        Defn.Var(
          Attrs.None,
          extern("__resources_amount"),
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
      ".nir",
      ".jar",
      ".scala",
      ".java",
      ".c",
      ".h",
      ".cpp",
      ".hpp",
      ".S"
    )

  private def isSourceFile(path: Path): Boolean = {
    if (path.getFileName == null) false
    else !sourceExtensions.filter(path.getFileName.toString.endsWith(_)).isEmpty
  }

}
