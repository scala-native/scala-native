package scala.scalanative
package native

@extern
object statvfs {

  type fsblkcnt_t = CUnsignedLong
  type fsfilcnt_t = CUnsignedLong
  type statvfs = CStruct11[CUnsignedLong, // f_bsize
                           CUnsignedLong, // f_frsize
                           fsblkcnt_t, // f_blocks
                           fsblkcnt_t, // f_bfree
                           fsblkcnt_t, // f_bavail
                           fsfilcnt_t, // f_files
                           fsfilcnt_t, // f_ffree
                           fsfilcnt_t, // f_favail
                           CUnsignedLong, // f_fsid
                           CUnsignedLong, // f_flag
                           CUnsignedLong] // f_namemax

  @name("scalanative_statvfs")
  def statvfs(path: CString, buf: Ptr[statvfs]): CInt = extern

  @name("scalanative_fstatvfs")
  def fstatvfs(fd: CInt, buf: Ptr[statvfs]): CInt = extern

  @name("scalanative_st_rdonly")
  def ST_RDONLY: CUnsignedLong = extern

  @name("scalanative_st_nosuid")
  def ST_NOSUID: CUnsignedLong = extern
}
