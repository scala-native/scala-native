package scala.scalanative
package native

@extern
object Atomic {
  @name("scalanative_compare_and_swap_int")
  def compareAndSwapInt(ptr: native.Ptr[native.CInt],
                        oldValue: native.CInt,
                        newValue: native.CInt): Boolean = extern
}
