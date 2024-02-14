package scala.scalanative.windows

import scala.language.implicitConversions
import scala.scalanative.unsafe.{Word => _, _}
import scala.scalanative.unsigned._
import scala.scalanative.windows.HandleApi.Handle
import MinWinBaseApi._
import WinBaseApi.SecurityAttributes

@extern
object FileApi {
  private[windows] type PathMax = Nat.Digit3[Nat._2, Nat._6, Nat._0]
  private[windows] type FileName[C] = CArray[C, PathMax]
  private[windows] type AlternateFileName[C] =
    CArray[C, Nat.Digit2[Nat._1, Nat._4]]
  private[windows] type Win32FindData[C] = CStruct13[
    DWord,
    FileTimeStruct,
    FileTimeStruct,
    FileTimeStruct,
    DWord,
    DWord,
    DWord,
    DWord,
    FileName[C],
    AlternateFileName[C],
    DWord,
    DWord,
    Word
  ]
  type Win32FindDataW = Win32FindData[WChar]
  type Win32FindDataA = Win32FindData[CChar]
  type ByHandleFileInformation = CStruct10[
    DWord,
    FileTimeStruct,
    FileTimeStruct,
    FileTimeStruct,
    DWord,
    DWord,
    DWord,
    DWord,
    DWord,
    DWord
  ]

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

  def CreateDirectoryA(
      filename: CString,
      securityAttributes: Ptr[SecurityAttributes]
  ): Boolean =
    extern
  def CreateDirectoryW(
      filename: CWString,
      securityAttributes: Ptr[SecurityAttributes]
  ): Boolean =
    extern
  def DeleteFileA(filename: CString): Boolean = extern
  def DeleteFileW(filename: CWString): Boolean = extern
  def FindFirstFileA(
      filename: CString,
      findFileData: Ptr[Win32FindDataA]
  ): Handle = extern
  def FindNextFileA(
      searchHandle: Handle,
      findFileData: Ptr[Win32FindDataA]
  ): Boolean = extern

  def FindFirstFileW(
      filename: CWString,
      findFileData: Ptr[Win32FindDataW]
  ): Handle = extern
  def FindNextFileW(
      searchHandle: Handle,
      findFileData: Ptr[Win32FindDataW]
  ): Boolean = extern
  def FindClose(searchHandle: Handle): Boolean = extern
  @blocking
  def FlushFileBuffers(handle: Handle): Boolean = extern
  def GetFileAttributesA(filename: CString): DWord = extern
  def GetFileAttributesW(filename: CWString): DWord = extern

  def GetFileInformationByHandle(
      file: Handle,
      fileInformation: Ptr[ByHandleFileInformation]
  ): Boolean =
    extern

  def GetFinalPathNameByHandleA(
      handle: Handle,
      buffer: CString,
      bufferSize: DWord,
      flags: DWord
  ): DWord = extern

  def GetFinalPathNameByHandleW(
      handle: Handle,
      buffer: CWString,
      bufferSize: DWord,
      flags: DWord
  ): DWord = extern

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
  def GetFileTime(
      file: Handle,
      creationTime: Ptr[FileTime],
      lastAccessTime: Ptr[FileTime],
      lastWriteTime: Ptr[FileTime]
  ): Boolean = extern

  def GetLogicalDriveStringsW(bufferLength: DWord, buffer: CWString): DWord =
    extern

  def GetTempFileNameW(
      pathName: CWString,
      prefixString: CWString,
      unique: UInt,
      tempFileName: CWString
  ): UInt = extern

  def GetTempPathA(bufferLength: DWord, buffer: CString): DWord = extern
  def GetTempPathW(bufferLength: DWord, buffer: CWString): DWord = extern

  def GetVolumePathNameW(
      filename: CWString,
      volumePathName: CWString,
      bufferLength: DWord
  ): Boolean = extern

  @blocking
  def ReadFile(
      fileHandle: Handle,
      buffer: Ptr[_],
      bytesToRead: DWord,
      bytesReadPtr: Ptr[DWord],
      overlapped: Ptr[_]
  ): Boolean = extern

  def RemoveDirectoryW(filename: CWString): Boolean = extern
  def SetEndOfFile(file: Handle): Boolean = extern
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

  def SetFileTime(
      file: Handle,
      creationTime: Ptr[FileTime],
      lastAccessTime: Ptr[FileTime],
      lastWriteTime: Ptr[FileTime]
  ): Boolean = extern

  @blocking def WriteFile(
      fileHandle: Handle,
      buffer: Ptr[_],
      bytesToRead: DWord,
      bytesWritten: Ptr[DWord],
      overlapped: Ptr[_]
  ): Boolean = extern

  def LockFile(
      hfile: Handle,
      dwFileOffsetLow: DWord,
      dwFileOffsetHigh: DWord,
      nNumberOfBytesToLockLow: DWord,
      nNumberOfBytesToLockHigh: DWord
  ): Boolean = extern

  @blocking def LockFileEx(
      hfile: Handle,
      dwFlags: DWord,
      dwReserved: DWord,
      nNumberOfBytesToLockLow: DWord,
      nNumberOfBytesToLockHigh: DWord,
      lpOverlapped: Ptr[OVERLAPPED]
  ): Boolean = extern

  def UnlockFile(
      hfile: Handle,
      dwFileOffsetLow: DWord,
      dwFileOffsetHigh: DWord,
      nNumberOfBytesToUnlockLow: DWord,
      nNumberOfBytesToUnlockHigh: DWord
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

  // Final path flags
  final val FILE_NAME_NORMALIZED = 0.toUInt
  final val FILE_NAME_OPENED = 0x8.toUInt

  final val VOLUME_NAME_DOS = 0.toUInt
  final val VOLUME_NAME_GUID = 0x01.toUInt
  final val VOLUME_NAME_NT = 0x02.toUInt
  final val VOLUME_NAME_NONE = 0x04.toUInt

  // File lock flags
  final val LOCKFILE_EXCLUSIVE_LOCK = 0x00000002.toUInt
  final val LOCKFILE_FAIL_IMMEDIATELY = 0x00000001.toUInt
}

object FileApiOps {
  import FileApi._
  import MinWinBaseApiOps._
  import util.Conversion._
  import scalanative.libc.string.strcpy
  import scala.scalanative.libc.wchar.wcscpy

  abstract class Win32FileDataOps[C: Tag](ref: Ptr[Win32FindData[C]]) {
    def fileAttributes: DWord = ref._1
    def creationTime: FileTime = ref.at2.fileTime
    def lastAccessTime: FileTime = ref.at3.fileTime
    def lastWriteTime: FileTime = ref.at4.fileTime
    private def fileSizeHigh: DWord = ref._5
    private def fileSizeLow: DWord = ref._6
    def fileSize: ULargeInteger =
      dwordPairToULargeInteger(fileSizeHigh, fileSizeLow)
    def reserved0: DWord = ref._7
    def reserved1: DWord = ref._8
    def fileName: Ptr[C] = ref._9.at(0)
    def alternateFileName: Ptr[C] = ref._10.at(0)
    // following fields are not used on some devices, though should not be written
    def fileType: DWord = ref._11
    def creatorType: DWord = ref._12
    def finderFlags: Word = ref._13

    def fileAttributes_=(v: DWord): Unit = ref._1 = v
    def creationTime_=(v: FileTime): Unit = ref.at2.fileTime = v
    def lastAccessTime_=(v: FileTime): Unit = ref.at3.fileTime = v
    def lastWriteTime_=(v: FileTime): Unit = ref.at4.fileTime = v
    def fileSize_=(v: ULargeInteger): Unit =
      uLargeIntegerToDWordPair(v, ref.at5, ref.at6)
    def reserved0_=(v: DWord): Unit = ref._7 = v
    def reserved1_=(v: DWord): Unit = ref._8 = v

    def fileName_=(v: Ptr[C]): Unit
    def alternateFileName_=(v: Ptr[C]): Unit
  }

  implicit final class Win32FileDataAOps(ref: Ptr[Win32FindDataA])
      extends Win32FileDataOps[CChar](ref) {
    override def fileName_=(v: CString): Unit = strcpy(ref._9.at(0), v)
    override def alternateFileName_=(v: CString): Unit =
      strcpy(ref._10.at(0), v)
  }

  implicit final class Win32FileDataWOps(ref: Ptr[Win32FindDataW])
      extends Win32FileDataOps[CChar16](ref) {
    override def fileName_=(v: CWString): Unit =
      wcscpy(
        ref._10.at(0).asInstanceOf[CWideString],
        v.asInstanceOf[CWideString]
      )

    override def alternateFileName_=(v: CWString): Unit =
      wcscpy(
        ref._10.at(0).asInstanceOf[CWideString],
        v.asInstanceOf[CWideString]
      )
  }

  implicit class ByHandleFileInformationOps(
      val ref: Ptr[ByHandleFileInformation]
  ) extends AnyVal {
    def fileAttributes: DWord = ref._1
    def creationTime: FileTime = ref.at2.fileTime
    def lastAccessTime: FileTime = ref.at3.fileTime
    def lastWriteTime: FileTime = ref.at4.fileTime
    def volumeSerialNumber: DWord = ref._5
    private def fileSizeHigh: DWord = ref._6
    private def fileSizeLow: DWord = ref._7
    def fileSize: ULargeInteger =
      dwordPairToULargeInteger(fileSizeHigh, fileSizeLow)
    def numberOfLinks: DWord = ref._8
    private def fileIndexHigh: DWord = ref._9
    private def fileIndexLow: DWord = ref._10
    def fileIndex: ULargeInteger =
      dwordPairToULargeInteger(fileIndexHigh, fileIndexLow)

    def fileAttributes_=(v: DWord): Unit = ref._1 = v
    def creationTime_=(v: FileTime): Unit = ref.at2.fileTime = v
    def lastAccessTime_=(v: FileTime): Unit = ref.at3.fileTime = v
    def lastWriteTime_=(v: FileTime): Unit = ref.at4.fileTime = v
    def volumeSerialNumber_=(v: DWord): Unit = ref._5
    def fileSize_=(v: ULargeInteger): Unit =
      uLargeIntegerToDWordPair(v, ref.at6, ref.at7)
    def numberOfLinks_=(v: DWord): Unit = ref._8
    def fileIndex_=(v: ULargeInteger): Unit =
      uLargeIntegerToDWordPair(v, ref.at9, ref.at10)
  }
}
