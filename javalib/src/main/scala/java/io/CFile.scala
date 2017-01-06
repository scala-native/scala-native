package java.io
import scalanative.native._

object CFileFlags {
  /* Values for flag manipulation in nativeFileOpen */
  val openRead: Int      = 1
  val openWrite: Int     = 2
  val openCreate: Int    = 4
  val openTruncate: Int  = 8
  val openAppend: Int    = 16
  val openText: Int      = 32
  val openCreateNew: Int = 64 /* Use this flag with HyOpenCreate, if this flag is specified then trying to create an existing file will fail */
  val openSync: Int      = 128
  val IsDir: Int         = 0 /* Return values for HyFileAttr */
  val IsFile: Int        = 1
  val seekSet            = 0
  val seekCur            = 1
  val seekEnd            = 2
}

@extern
object CFile {

  @name("scalanative_tty_available")
  def fileTtyAvalaible(): CInt = extern

  @name("scalanative_file_seek")
  def fileSeek(inFD: CInt, offset: CLong, whence: CInt): CLong = extern

  @name("scalanative_file_sync")
  def fileSync(inFileDesc: Long): Int = extern

  @name("scalanative_file_findfirst")
  def fileFindFirst(path: CString, charbuf: CString): Ptr[_] /*UDATA*/ = extern

  @name("scalanative_file_findnext")
  def fileFindNext(findhandle: Ptr[_] /*UDATA*/, charbuf: CString): Int =
    extern

  @name("file_findclose")
  def fileFindClose(findhandle: Ptr[_] /*UDATA*/ ): Unit = extern

  @name("scalanative_set_last_mod")
  def setLastModNative(path: CString, time: Long): Int = extern

  @name("scalanative_set_read_only_native")
  def setReadOnlyNative(path: CString): Int = extern

  @name("scalanative_file_mkdir")
  def fileMkDir(path: CString): Int = extern

  @name("scalanative_file_length")
  def fileLength(path: CString): Long = extern

  @name("scalanative_last_mod")
  def lastModNative(path: CString): CSize = extern

  @name("scalanative_file_open")
  def nativeFileOpen(path: CString, flags: Int, mode: Int): CInt = extern

  @name("scalanative_file_descriptor_close")
  def fileDescriptorClose(fd: CInt): CInt = extern

  @name("scalanative_separator_char")
  def separatorChar(): Char = extern

  @name("scalanative_path_separator_char")
  def pathSeparatorChar(): Char = extern

  @name("scalanative_is_case_sensitive")
  def isCaseSensitiveImpl(): Int = extern

  @name("scalanative_get_platform_roots")
  def getPlatformRoots(rootStrings: Ptr[CChar]) = extern

  @name("scalanative_file_attr")
  def fileAttribute(path: CString): Int = extern

  @name("scalanative_get_os_encoding")
  def getOsEncoding(): CString = extern

  @name("scalanative_get_temp_dir")
  def getTempDir(): CString = extern

  @name("scalanative_eexist")
  def EEXIST: CInt = extern

}
