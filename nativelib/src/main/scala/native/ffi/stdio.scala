package native
package ffi

@extern
object stdio {
  def puts(str: ffi.Ptr[ffi.Char8]): Unit = extern
}
