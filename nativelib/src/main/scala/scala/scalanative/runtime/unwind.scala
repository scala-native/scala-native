package scala.scalanative
package runtime

import scalanative.unsafe._

@extern
@compile("platform/posix/libunwind")
@compile("platform/posix/unwind.c")
@compile("platform/windows/unwind.c")
object unwind {
  @name("scalanative_unwind_get_context")
  def get_context(context: Ptr[Byte]): CInt = extern
  @name("scalanative_unwind_init_local")
  def init_local(cursor: Ptr[Byte], context: Ptr[Byte]): CInt = extern
  @name("scalanative_unwind_step")
  def step(cursor: Ptr[Byte]): CInt = extern
  @name("scalanative_unwind_get_proc_name")
  def get_proc_name(
      cursor: Ptr[Byte],
      buffer: CString,
      length: CSize,
      offset: Ptr[Long]
  ): CInt = extern
  @name("scalanative_unwind_get_reg")
  def get_reg(
      cursor: Ptr[Byte],
      reg: CInt,
      valp: Ptr[CSize]
  ): CInt = extern

  @name("scalanative_unw_reg_ip")
  def UNW_REG_IP: CInt = extern

  @name("scalanative_unwind_sizeof_context")
  def sizeOfContext: CSize = extern

  @name("scalanative_unwind_sizeof_cursor")
  def sizeOfCursor: CSize = extern
}
