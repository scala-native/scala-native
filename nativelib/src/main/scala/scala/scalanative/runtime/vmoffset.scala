package scala.scalanative.runtime

import scalanative.unsafe.*

@extern
@define("__SCALANATIVE_VMOFFSET")
private[runtime] object vmoffset {

  /** Get the image offset of this executable.
   */
  @name("scalanative_get_vmoffset")
  def get_vmoffset(): CInt = extern
}
