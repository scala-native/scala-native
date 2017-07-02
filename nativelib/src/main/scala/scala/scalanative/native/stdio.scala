package scala.scalanative
package native

@extern
object stdio {

  // File access

  def fopen(filename: CString, mode: CString): Ptr[FILE]  = extern
  def fclose(stream: Ptr[FILE]): CInt                     = extern
  def fflush(stream: Ptr[FILE]): CInt                     = extern
  def setbuf(stream: Ptr[FILE], buffer: Ptr[CChar]): Unit = extern
  def setvbuf(stream: Ptr[FILE],
              buffer: Ptr[CChar],
              mode: CInt,
              size: CSize): CInt =
    extern
  def fwide(stream: Ptr[FILE], mode: CInt): CInt = extern

  // Direct input/output

  def fread(buffer: Ptr[Byte],
            size: CSize,
            count: CSize,
            stream: Ptr[FILE]): CSize =
    extern
  def fwrite(buffer: Ptr[Byte],
             size: CSize,
             count: CSize,
             stream: Ptr[FILE]): CSize =
    extern

  // Unformatted input/output

  def fgetc(stream: Ptr[FILE]): CInt                               = extern
  def getc(stream: Ptr[FILE]): CInt                                = extern
  def fgets(str: CString, count: CInt, stream: Ptr[FILE]): CString = extern
  def fputc(ch: CInt, stream: Ptr[FILE]): CInt                     = extern
  def putc(ch: CInt, stream: Ptr[FILE]): CInt                      = extern
  def fputs(str: CString, stream: Ptr[FILE]): CInt                 = extern
  def getchar(): CInt                                              = extern
  def gets(str: CString): CString                                  = extern
  def putchar(ch: CInt): CInt                                      = extern
  def puts(str: CString): CInt                                     = extern
  def ungetc(ch: CInt, stream: Ptr[FILE]): CInt                    = extern

  // Formatted input/output

  def scanf(format: CString, args: CVararg*): CInt                     = extern
  def fscanf(stream: Ptr[FILE], format: CString, args: CVararg*): CInt = extern
  def sscanf(buffer: CString, format: CString, args: CVararg*): CInt   = extern
  def printf(format: CString, args: CVararg*): CInt                    = extern
  def fprintf(stream: Ptr[FILE], format: CString, args: CVararg*): CInt =
    extern
  def sprintf(buffer: CString, format: CString, args: CVararg*): CInt = extern
  def snprintf(buffer: CString,
               bufsz: CInt,
               format: CString,
               args: CVararg*): CInt =
    extern

  // File positioning

  def ftell(stream: Ptr[FILE]): CLong                             = extern
  def fgetpos(stream: Ptr[FILE], pos: Ptr[fpos_t]): CInt          = extern
  def fseek(stream: Ptr[FILE], offset: CLong, origin: CInt): CInt = extern
  def fsetpos(stream: Ptr[FILE], pos: Ptr[fpos_t]): CInt          = extern
  def rewind(stream: Ptr[FILE]): Unit                             = extern

  // Error handling

  def clearerr(stream: Ptr[FILE]): Unit = extern
  def feof(stream: Ptr[FILE]): CInt     = extern
  def ferror(stream: Ptr[FILE]): CInt   = extern
  def perror(str: CString): Unit        = extern

  // Operations on files

  @name("scalanative_libc_remove")
  def remove(fname: CString): CInt                             = extern
  def rename(oldFileName: CString, newFileName: CString): CInt = extern
  def tmpfile(): Ptr[FILE]                                     = extern
  def tmpnam(fileName: CString): CString                       = extern

  // Types

  type FILE   = CStruct0
  type fpos_t = CStruct0

  // Macros

  @name("scalanative_libc_stdin")
  def stdin: Ptr[FILE] = extern
  @name("scalanative_libc_stdout")
  def stdout: Ptr[FILE] = extern
  @name("scalanative_libc_stderr")
  def stderr: Ptr[FILE] = extern
  @name("scalanative_libc_eof")
  def EOF: CInt = extern
  @name("scalanative_libc_fopen_max")
  def FOPEN_MAX: CUnsignedInt = extern
  @name("scalanative_libc_filename_max")
  def FILENAME_MAX: CUnsignedInt = extern
  @name("scalanative_libc_bufsiz")
  def BUFSIZ: CUnsignedInt = extern
  @name("scalanative_libc_iofbf")
  def _IOFBF: CInt = extern
  @name("scalanative_libc_iolbf")
  def _IOLBF: CInt = extern
  @name("scalanative_libc_ionbf")
  def _IONBF: CInt = extern
  @name("scalanative_libc_seek_set")
  def SEEK_SET: CInt = extern
  @name("scalanative_libc_seek_cur")
  def SEEK_CUR: CInt = extern
  @name("scalanative_libc_seek_end")
  def SEEK_END: CInt = extern
  @name("scalanative_libc_tmp_max")
  def TMP_MAX: CUnsignedInt = extern
  @name("scalanative_libc_l_tmpnam")
  def L_tmpnam: CUnsignedInt = extern
}
