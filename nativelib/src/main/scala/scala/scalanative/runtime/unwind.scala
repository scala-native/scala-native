package scala.scalanative
package runtime

import scalanative.unsafe._

@extern
private[runtime] object unwind {
  @name("scalanative_unwind_get_context")
  def get_context(context: CVoidPtr): CInt = extern
  @name("scalanative_unwind_init_local")
  def init_local(cursor: CVoidPtr, context: CVoidPtr): CInt = extern
  @name("scalanative_unwind_step")
  def step(cursor: CVoidPtr): CInt = extern
  @name("scalanative_unwind_get_proc_name")
  def get_proc_name(
      cursor: CVoidPtr,
      buffer: CString,
      length: CSize,
      offset: Ptr[Long]
  ): CInt = extern
  @name("scalanative_unwind_get_reg")
  def get_reg(
      cursor: CVoidPtr,
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
