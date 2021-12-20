package java.lang.resource

import scala.scalanative.unsafe._

@extern
private[lang] object EmbeddedResourceReader {
  @name("scalanative_resource_get_content_byte")
  def getContentByte(embeddedResourceId: CInt, byteIndex: CInt): Byte = extern

  @name("scalanative_resource_get_path_byte")
  def getPathByte(embeddedResourceId: CInt, byteIndex: CInt): Byte = extern

  @name("scalanative_resource_get_embedded_size")
  def getEmbeddedSize(): CInt = extern

  @name("scalanative_resource_get_path_length")
  def getPathLength(embeddedResourceId: CInt): CInt = extern

  @name("scalanative_resource_get_content_length")
  def getContentLength(embeddedResourceId: CInt): CInt = extern
}
