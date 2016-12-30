package scala.scalanative
package native

@extern
object string {
  def strcpy(dest: CString, src: CString): CString                     = extern
  def strncpy(dest: CString, src: CString, count: CSize): CString      = extern
  def strcat(dest: CString, src: CString): CString                     = extern
  def strncat(dest: CString, src: CString, count: CSize): CString      = extern
  def strxfrm(dest: CString, src: CString, count: CSize): CSize        = extern
  def strlen(str: CString): CSize                                      = extern
  def strcmp(lhs: CString, rhs: CString): CInt                         = extern
  def strncmp(lhs: CString, rhs: CString, count: CSize): CInt          = extern
  def strcoll(lhs: CString, rhs: CString): CInt                        = extern
  def strchr(str: CString, ch: CInt): CString                          = extern
  def strrchr(str: CString, ch: CInt): CString                         = extern
  def strspn(dest: CString, src: CString): CSize                       = extern
  def strcspn(dest: CString, src: CString): CSize                      = extern
  def strpbrk(dest: CString, breakset: CString): CString               = extern
  def strstr(str: CString, substr: CString): CString                   = extern
  def strtok(str: CString, delim: CString): CChar                      = extern
  def memchr(ptr: Ptr[Byte], ch: CInt, count: CSize): Ptr[Byte]        = extern
  def memcmp(lhs: Ptr[Byte], rhs: Ptr[Byte], count: CSize): Ptr[Byte]  = extern
  def memset(dest: Ptr[Byte], ch: CInt, count: CSize): Ptr[Byte]       = extern
  def memcpy(dest: Ptr[Byte], src: Ptr[Byte], count: CSize): Ptr[Byte] = extern
  def memmove(dest: Ptr[Byte], src: Ptr[Byte], count: CSize): Ptr[Byte] =
    extern
  def strerror(errnum: CInt): CString = extern
}
