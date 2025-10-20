package scala.scalanative.runtime.resource

import scala.scalanative.runtime.ffi
import scala.scalanative.unsigned.*
import scala.scalanative.runtime.{ByteArray, Intrinsics}
import scala.scalanative.unsafe.Ptr

private[runtime] object EmbeddedResourceHelper {

  lazy val resourceFileIdMap = getAllFilePaths().zipWithIndex.toMap

  // Decodes, constructs and returns all embedded resource file paths.
  private def getAllFilePaths(): Array[String] = {
    val filePathAmount = EmbeddedResourceReader.getEmbeddedSize()
    Array.tabulate(filePathAmount) { idx =>
      val pathSize = EmbeddedResourceReader.getPathLength(idx)
      val path = Array.ofDim[Byte](pathSize)
      ffi.memcpy(
        path.asInstanceOf[ByteArray].atRaw(0),
        EmbeddedResourceReader.getPathPtr(idx),
        Intrinsics.castIntToRawSize(pathSize)
      )
      new String(path)
    }
  }

  def getContentPtr(resourceId: Int): Ptr[Byte] =
    EmbeddedResourceReader.getContentPtr(resourceId)

}
