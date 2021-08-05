package scala.scalanative.unsafe

// used by ExternTest
@extern
object testlib {
  // C code in testlib.c
  @name("exec")
  def exec0(f: CFuncPtr0[Int]): Int = extern
  def exec(f: CFuncPtr): Int = extern
}
