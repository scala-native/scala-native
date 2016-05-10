package scala.scalanative
package native

@extern object stdlib {
  var __stderrp: Ptr[_] = extern
  var __stdoutp: Ptr[_] = extern
  def fopen(filename: CString, mode: CString): Ptr[_] = extern
  def fprintf(stream: Ptr[_], format: CString, args: Any*): CInt = extern
  def malloc(size: Word): Ptr[_] = extern
}
