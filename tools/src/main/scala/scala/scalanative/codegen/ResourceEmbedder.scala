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

object ResourceEmbedder {

  final case class ClasspathFile(file: Path, classpath: Path)

  def apply(config: Config): Seq[Defn.Var] = {
    val classpath = config.classPath

    def getAllClassPathFiles(classpath: Seq[Path]): Seq[ClasspathFile] = {
      var paths: ArrayBuffer[ClasspathFile] = ArrayBuffer.empty

      var root: Path = null
      val visitor = new SimpleFileVisitor[Path] {

        override def visitFile(
            path: Path,
            attr: BasicFileAttributes
        ): FileVisitResult = {
          if (attr.isRegularFile() && notSourceFile(path)) {
            paths += ClasspathFile(path, root)
          }
          CONTINUE
        }

        override def postVisitDirectory(
            dir: Path,
            exc: IOException
        ): FileVisitResult =
          CONTINUE

        override def visitFileFailed(
            file: Path,
            exc: IOException
        ): FileVisitResult = {
          throw exc
          TERMINATE
        }
      }

      classpath.map { path =>
        root = path
        Files.walkFileTree(path, visitor);
      }

      paths.toSeq
    }

    val pathValues = new ArrayBuffer[Val.ArrayValue]()
    val contentValues = new ArrayBuffer[Val.ArrayValue]()

    implicit val position: Position = Position.NoPosition

    val embeddedFiles = 
      if(config.compilerConfig.embedResources) {
        getAllClassPathFiles(classpath)
      } else {
        Seq()
      }

    embeddedFiles.foreach {
      case ClasspathFile(path, root) =>
        val relativePath: String = root.relativize(path).toString
        val encodedContent = Base64.getEncoder
          .encode(Files.readAllBytes(path))
          .map(a => Val.Int(a.asInstanceOf[Int]))
        val encodedPath = Base64.getEncoder
          .encode(relativePath.getBytes())
          .map(a => Val.Int(a.asInstanceOf[Int]))

        contentValues += Val.ArrayValue(Type.Int, encodedContent.toSeq)
        pathValues += Val.ArrayValue(Type.Int, encodedPath.toSeq)
    }

    def generateExtern2DArray(name: String, content: Array[Val.Const]) = {
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

    def generateExternLongArray(name: String, content: Array[Val.Long]) = {
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
          pathValues.toArray.map(Val.Const(_))
        ),
        generateExtern2DArray(
          "__resources_all_content",
          contentValues.toArray.map(Val.Const(_))
        ),
        generateExternLongArray(
          "__resources_all_path_lengths",
          pathValues.toArray.map(path =>
            Val.Long(path.values.length.asInstanceOf[Long])
          )
        ),
        generateExternLongArray(
          "__resources_all_content_lengths",
          contentValues.toArray.map(content =>
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
      config.logger.info("Embedded resource: " + classpathFile.file)
    }

    generated
  }

  private def extern(id: String): Global =
    Global.Member(Global.Top("__"), Sig.Extern(id))

  private val sourceExtensions =
    Set(".class", ".c", ".cpp", ".h", ".nir", ".jar")

  private def notSourceFile(path: Path): Boolean =
    sourceExtensions.filter(path.getFileName.toString.endsWith(_)).isEmpty

}
