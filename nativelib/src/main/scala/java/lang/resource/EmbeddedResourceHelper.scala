package java.lang.resource

import java.util.Base64

private[lang] object EmbeddedResourceHelper {

  lazy val resourceFileIdMap =
    EmbeddedResourceHelper.getAllFilePaths().zipWithIndex.toMap

  // Decodes, constructs and returns all embedded resource file paths.
  private def getAllFilePaths(): Array[String] = {
    val filePathAmount = EmbeddedResourceReader.getEmbeddedSize()
    val res = Array.ofDim[Array[Byte]](filePathAmount)

    for (id <- 0 until filePathAmount) {
      val pathSize = EmbeddedResourceReader.getPathLength(id)
      val path = Array.ofDim[Byte](pathSize)
      for (pos <- 0 until pathSize) {
        path(pos) =
          EmbeddedResourceReader.getPathByte(id, pos).asInstanceOf[Byte]
      }
      res(id) = Base64.getDecoder().decode(new String(path))
    }

    res.map(new String(_))
  }

  def getContentByte(resourceId: Int, pos: Long): Byte =
    EmbeddedResourceReader.getContentByte(resourceId, pos.toInt)
}
