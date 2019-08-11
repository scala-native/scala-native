package scala.scalanative.native

@extern
object sysinfo {
  def get_nprocs: CInt = extern
}
