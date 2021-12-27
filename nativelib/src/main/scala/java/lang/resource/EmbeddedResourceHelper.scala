package java.lang.resource

import java.util.Base64

private[lang] object EmbeddedResourceHelper {

  lazy val resourceFileIdMap =
    EmbeddedResourceHelper.getAllFilePaths().zipWithIndex.toMap

  // Decodes, constructs and returns all embedded resource file paths.
  private def getAllFilePaths(): Array[String] = {
    val filePathAmount = EmbeddedResourceReader.getEmbeddedSize()
    val res = Array.ofDim[Array[Byte]](filePathAmount)

    var id = 0
    while (id < filePathAmount) {
      val pathSize = EmbeddedResourceReader.getPathLength(id)
      val path = Array.ofDim[Byte](pathSize)
      var pos = 0
      while (pos < pathSize) {
        path(pos) = EmbeddedResourceReader.getPathByte(id, pos)
        pos += 1
      }
      res(id) = Base64.getDecoder().decode(new String(path))
      id += 1
    }

    res.map(new String(_))
  }

  def getContentByte(resourceId: Int, pos: Int): Byte =
    EmbeddedResourceReader.getContentByte(resourceId, pos)
}
