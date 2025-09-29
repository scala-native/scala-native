package scala.scalanative.runtime.resource

import scala.scalanative.runtime.RawPtr
import scala.scalanative.unsafe._

@extern
private[resource] object EmbeddedResourceReader {
  @name("scalanative_resource_get_content_ptr")
  def getContentPtr(embeddedResourceId: CInt): Ptr[Byte] = extern

  @name("scalanative_resource_get_path_ptr")
  def getPathPtr(embeddedResourceId: CInt): RawPtr = extern

  @name("scalanative_resource_get_embedded_size")
  def getEmbeddedSize(): CInt = extern

  @name("scalanative_resource_get_path_length")
  def getPathLength(embeddedResourceId: CInt): CInt = extern

  @name("scalanative_resource_get_content_length")
  def getContentLength(embeddedResourceId: CInt): CInt = extern
}
