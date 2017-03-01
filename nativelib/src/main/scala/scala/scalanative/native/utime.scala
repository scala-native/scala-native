package scala.scalanative
package native

@extern
object utime {
  // TODO: Where should we put those recurrent types?
  import stat.time_t
  type utimbuf = CStruct2[time_t, // actime
                          time_t] // modtime

  @name("scalanative_utime")
  def utime(path: CString, times: Ptr[utimbuf]): CInt = extern
}
