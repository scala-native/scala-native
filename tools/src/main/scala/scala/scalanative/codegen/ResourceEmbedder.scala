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

object ResourceEmbedder {

  final case class ClasspathFile(
      accessPath: Path,
      pathName: String,
      classpathDirectory: VirtualDirectory
  )

  def apply(config: Config)(implicit scope: Scope): Seq[Defn.Var] = {
    val classpath = config.classPath

    val pathValues = new ArrayBuffer[Val.ArrayValue]()
    val contentValues = new ArrayBuffer[Val.ArrayValue]()

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

              if (notSourceFile(path) && Files.isRegularFile(path)) {
                Some(ClasspathFile(path, pathName, virtualDir))
              } else None
            }
        }
      } else {
        Seq()
      }

    val alreadyVisited = mutable.HashSet[String]()

    val embeddedFiles = foundFiles.filter { classpathFile =>
      if (!alreadyVisited.contains(classpathFile.pathName)) {
        alreadyVisited.add(classpathFile.pathName)
        true
      } else false
    }

    foundFiles.foreach {
      case ClasspathFile(accessPath, pathName, virtDir) =>
        val fileBuffer = virtDir.read(accessPath)
        val encodedContent = Base64.getEncoder
          .encode(fileBuffer.array())
          .map(a => Val.Int(a.asInstanceOf[Int]))

        val encodedPath = Base64.getEncoder
          .encode(pathName.toString.getBytes())
          .map(a => Val.Int(a.asInstanceOf[Int]))

        contentValues += Val.ArrayValue(Type.Int, encodedContent.toSeq)
        pathValues += Val.ArrayValue(Type.Int, encodedPath.toSeq)
    }

    def generateExtern2DArray(name: String, content: IndexedSeq[Val.Const]) = {
      Defn.Var(
        Attrs.None,
        extern(name),
        Type.Ptr,
        Val.Const(
          Val.ArrayValue(
            Type.Ptr,
            content
          )
        )
      )
    }

    def generateExternLongArray(name: String, content: IndexedSeq[Val.Long]) = {
      Defn.Var(
        Attrs.None,
        extern(name),
        Type.Ptr,
        Val.Const(
          Val.ArrayValue(
            Type.Long,
            content
          )
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
          pathValues.toIndexedSeq.map(path =>
            Val.Long(path.values.length.asInstanceOf[Long])
          )
        ),
        generateExternLongArray(
          "__resources_all_content_lengths",
          contentValues.toIndexedSeq.map(content =>
            Val.Long(content.values.length.asInstanceOf[Long])
          )
        ),
        Defn.Var(
          Attrs.None,
          extern("__resources_size"),
          Type.Ptr,
          Val.Long(
            contentValues.length.asInstanceOf[Long]
          )
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
    Seq(".class", ".c", ".cpp", ".h", ".nir", ".jar", ".scala", ".java", ".hpp")

  private def notSourceFile(path: Path): Boolean = {
    if (path.getFileName == null) false
    else sourceExtensions.filter(path.getFileName.toString.endsWith(_)).isEmpty
  }

}
