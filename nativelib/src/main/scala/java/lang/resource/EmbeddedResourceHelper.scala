package java.lang.resource

import scala.scalanative.runtime.libc
import scala.scalanative.unsigned._
import scala.scalanative.runtime.ByteArray
import scala.scalanative.unsafe.Ptr

private[lang] object EmbeddedResourceHelper {

  lazy val resourceFileIdMap = getAllFilePaths().zipWithIndex.toMap

  // Decodes, constructs and returns all embedded resource file paths.
  private def getAllFilePaths(): Array[String] = {
    val filePathAmount = EmbeddedResourceReader.getEmbeddedSize()
    Array.tabulate(filePathAmount) { idx =>
      val pathSize = EmbeddedResourceReader.getPathLength(idx)
      val path = Array.ofDim[Byte](pathSize)
      libc.memcpy(
        path.asInstanceOf[ByteArray].atRaw(0),
        EmbeddedResourceReader.getPathPtr(idx),
        pathSize
      )
      new String(path)
    }
  }

  def getContentPtr(resourceId: Int): Ptr[Byte] =
    EmbeddedResourceReader.getContentPtr(resourceId)

}
