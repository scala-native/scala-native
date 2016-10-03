package scala.scalanative.linker

import java.net.URI
import java.nio.ByteBuffer
import java.nio.file._

import scala.collection.JavaConverters._

/** A values which represents a member of a classpath. */
sealed abstract class Classpath {
  def contents(): Seq[VirtualFile]
  def close(): Unit
}

final class LocalClasspath(path: Path) extends Classpath {
  override def close(): Unit = ()

  override def contents(): Seq[VirtualFile] =
    Files
      .walk(path, Integer.MAX_VALUE, FileVisitOption.FOLLOW_LINKS)
      .iterator()
      .asScala
      .toSeq
      .map(filePath => new VirtualFile(filePath))
}

final class JarClasspath(path: Path) extends Classpath {
  private val fsUri: URI                   = URI.create(s"jar:${path.toUri}")
  private val options: Map[String, String] = Map("create" -> "false")
  private val fs: FileSystem               = FileSystems.newFileSystem(fsUri, options.asJava)
  private val roots: Seq[Path]             = fs.getRootDirectories.asScala.toSeq

  override def contents(): Seq[VirtualFile] = {
    roots
      .flatMap(
        path =>
          Files
            .walk(path, Integer.MAX_VALUE, FileVisitOption.FOLLOW_LINKS)
            .iterator()
            .asScala)
      .map(path => new VirtualFile(path))
  }

  override def close(): Unit = fs.close()
}

/** A memory cached file-like object contained in some member of a classpath. */
final class VirtualFile(val relativePath: Path) {
  def uri: URI = relativePath.toUri
  lazy val bytes: ByteBuffer =
    ByteBuffer.wrap(Files.readAllBytes(relativePath))
}
