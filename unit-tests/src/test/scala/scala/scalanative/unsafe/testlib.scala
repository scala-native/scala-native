package scala.scalanative.unsafe

@extern
object testlib {
  @name("exec")
  def exec0(f: CFuncPtr0[Int]): Int = extern
  def exec(f: CFuncPtr): Int        = extern
}
