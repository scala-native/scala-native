package scala.scalanative.windows

import scala.scalanative.windows.HandleApi.Handle
import scala.scalanative.unsafe._

@extern
object MemoryApi {

  def MapViewOfFile(
      hFileMappingObject: Handle,
      dwDesiredAccess: DWord,
      dwFileOffsetHigh: DWord,
      dwFileOffsetLow: DWord,
      dwNumberOfBytesToMap: CSize
  ): Ptr[Byte] = extern

  def UnmapViewOfFile(lpBaseAddress: Ptr[Byte]): Boolean = extern

  @blocking
  def FlushViewOfFile(
      lpBaseAddress: Ptr[Byte],
      dwNumberOfBytesToFlush: DWord
  ): Boolean = extern

  @name("scalanative_file_map_all_access")
  def FILE_MAP_ALL_ACCESS: DWord = extern

  @name("scalanative_file_map_read")
  def FILE_MAP_READ: DWord = extern

  @name("scalanative_file_map_write")
  def FILE_MAP_WRITE: DWord = extern

  @name("scalanative_file_map_copy")
  def FILE_MAP_COPY: DWord = extern

  @name("scalanative_file_map_execute")
  def FILE_MAP_EXECUTE: DWord = extern

  @name("scalanative_file_map_large_pages")
  def FILE_MAP_LARGE_PAGES: DWord = extern

  @name("scalanative_file_map_targets_invalid")
  def FILE_MAP_TARGETS_INVALID: DWord = extern

}
