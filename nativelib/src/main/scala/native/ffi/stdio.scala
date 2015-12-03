package native
package ffi

@extern
object stdio {
  def puts(str: Ptr[Char8]): Unit = extern
}
