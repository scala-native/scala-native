package scala.scalanative
package runtime

import native._

@extern
object unwind {
  @name("scalanative_unwind_get_context")
  def get_context(context: Ptr[Byte]): CInt = extern
  @name("scalanative_unwind_init_local")
  def init_local(cursor: Ptr[Byte], context: Ptr[Byte]): CInt = extern
  @name("scalanative_unwind_step")
  def step(cursor: Ptr[Byte]): CInt = extern
  @name("scalanative_unwind_get_proc_name")
  def get_proc_name(cursor: Ptr[Byte],
                    buffer: CString,
                    length: CSize,
                    offset: Ptr[Byte]): CInt = extern
  @name("scalanative_unwind_get_proc_start_ip")
  def get_proc_start_ip(cursor: Ptr[Byte], buffer: Ptr[CUnsignedLong]): CInt =
    extern
}
