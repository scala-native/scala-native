package scala.scalanative.windows

import scala.language.implicitConversions
import scala.scalanative.libc.wchar.wcscpy
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.windows.HandleApi.Handle

@extern
object FileApi {
  def CreateFileA(
      filename: CString,
      desiredAccess: DWord,
      shareMode: DWord,
      securityAttributes: SecurityAttributes,
      creationDisposition: DWord,
      flagsAndAttributes: UInt,
      templateFile: Handle
  ): Handle = extern

  def CreateFileW(
      filename: CWString,
      desiredAccess: DWord,
      shareMode: DWord,
      securityAttributes: SecurityAttributes,
      creationDisposition: DWord,
      flagsAndAttributes: UInt,
      templateFile: Handle
  ): Handle = extern
  def DeleteFileA(filename: CString): Boolean = extern
  def DeleteFileW(filename: CWString): Boolean = extern
  def FlushFileBuffers(handle: Handle): Boolean = extern
  def GetFileAttributesA(filename: CString): DWord = extern
  def GetFileAttributesW(filename: CWString): DWord = extern
  def GetFullPathNameA(
      filename: CString,
      bufferLength: DWord,
      buffer: CString,
      filePart: Ptr[CString]
  ): DWord = extern

  def GetFullPathNameW(
      filename: CWString,
      bufferLength: DWord,
      buffer: CWString,
      filePart: Ptr[CWString]
  ): DWord = extern
  def GetFileSizeEx(file: Handle, fileSize: Ptr[LargeInteger]): Boolean = extern
  def GetTempPathA(bufferLength: DWord, buffer: CString): DWord = extern
  def GetTempPathW(bufferLength: DWord, buffer: CWString): DWord = extern
  def ReadFile(
      fileHandle: Handle,
      buffer: Ptr[Byte],
      bytesToRead: DWord,
      bytesReadPtr: Ptr[DWord],
      overlapped: Ptr[Byte]
  ): Boolean = extern
  def SetFileAttributesA(filename: CString, fileAttributes: DWord): Boolean =
    extern
  def SetFileAttributesW(filename: CWString, fileAttributes: DWord): Boolean =
    extern
  def SetFilePointerEx(
      file: Handle,
      distanceToMove: LargeInteger,
      newFilePointer: Ptr[LargeInteger],
      moveMethod: DWord
  ): Boolean = extern

  def WriteFile(
      fileHandle: Handle,
      buffer: Ptr[Byte],
      bytesToRead: DWord,
      bytesWritten: Ptr[DWord],
      overlapped: Ptr[Byte]
  ): Boolean = extern
}

object FileApiExt {
  final val MAX_PATH = 260.toUInt

  // File Pointer Move Methods
  final val FILE_BEGIN = 0.toUInt
  final val FILE_CURRENT = 1.toUInt
  final val FILE_END = 2.toUInt

  // File sharing
  final val FILE_NOT_SHARED = 0.toUInt
  final val FILE_SHARE_READ = 0x01.toUInt
  final val FILE_SHARE_WRITE = 0x02.toUInt
  final val FILE_SHARE_DELETE = 0x04.toUInt
  final val FILE_SHARE_ALL =
    FILE_SHARE_READ | FILE_SHARE_WRITE | FILE_SHARE_DELETE

  // File disposition
  final val CREATE_NEW = 0x01.toUInt
  final val CREATE_ALWAYS = 0x02.toUInt
  final val OPEN_EXISTING = 0x03.toUInt
  final val OPEN_ALWAYS = 0x04.toUInt
  final val TRUNCATE_EXISTING = 0x05.toUShort

  // File attributes
  final val INVALID_FILE_ATTRIBUTES = 0xffffffff.toUInt
  final val FILE_ATTRIBUTE_READONLY = 0x00000001.toUInt
  final val FILE_ATTRIBUTE_HIDDEN = 0x00000002.toUInt
  final val FILE_ATTRIBUTE_SYSTEM = 0x00000004.toUInt
  final val FILE_ATTRIBUTE_DIRECTORY = 0x00000010.toUInt
  final val FILE_ATTRIBUTE_ARCHIVE = 0x00000020.toUInt
  final val FILE_ATTRIBUTE_DEVICE = 0x00000040.toUInt
  final val FILE_ATTRIBUTE_NORMAL = 0x00000080.toUInt
  final val FILE_ATTRIBUTE_TEMPORARY = 0x00000100.toUInt
  final val FILE_ATTRIBUTE_SPARSE_FILE = 0x00000200.toUInt
  final val FILE_ATTRIBUTE_REPARSE_POINT = 0x00000400.toUInt
  final val FILE_ATTRIBUTE_COMPRESSED = 0x00000800.toUInt
  final val FILE_ATTRIBUTE_OFFLINE = 0x00001000.toUInt
  final val FILE_ATTRIBUTE_NOT_CONTENT_INDEXED = 0x00002000.toUInt
  final val FILE_ATTRIBUTE_ENCRYPTED = 0x00004000.toUInt
  final val FILE_ATTRIBUTE_INTEGRITY_STREAM = 0x00008000.toUInt
  final val FILE_ATTRIBUTE_VIRTUAL = 0x00010000.toUInt
  final val FILE_ATTRIBUTE_NO_SCRUB_DATA = 0x00020000.toUInt
  final val FILE_ATTRIBUTE_EA = 0x00040000.toUInt
  final val FILE_ATTRIBUTE_PINNED = 0x00080000.toUInt
  final val FILE_ATTRIBUTE_UNPINNED = 0x00100000.toUInt
  final val FILE_ATTRIBUTE_RECALL_ON_OPEN = 0x00040000.toUInt
  final val FILE_ATTRIBUTE_RECALL_ON_DATA_ACCCESS = 0x00400000.toUInt

  // File flags
  final val FILE_FLAG_BACKUP_SEMANTICS = 0x02000000.toUInt
  final val FILE_FLAG_DELETE_ON_CLOSE = 0x04000000.toUInt
  final val FILE_FLAG_NO_BUFFERING = 0x20000000.toUInt
  final val FILE_FLAG_OPEN_NO_RECALL = 0x00100000.toUInt
  final val FILE_FLAG_OPEN_REPARSE_POINT = 0x00200000.toUInt
  final val FILE_FLAG_OVERLAPPED = 0x40000000.toUInt
  final val FILE_FLAG_POSIX_SEMANTICS = 0x01000000.toUInt
  final val FILE_FLAG_RANDOM_ACCESS = 0x10000000.toUInt
  final val FILE_FLAG_SESSION_AWARE = 0x00800000.toUInt
  final val FILE_FLAG_SEQUENTIAL_SCAN = 0x08000000.toUInt
  final val FILE_FLAG_WRITE_THROUGH = 0x80000000.toUInt
}
