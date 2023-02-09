package scala.scalanative
package posix
package sys

import scalanative.unsafe._

@extern object sysinfo {
  /* Return number of available processors.  */
  def get_nprocs(): Int = extern

  /* Return number of configured processors.  */
  def get_nprocs_conf(): Int = extern

  /* Return number of physical pages of memory in the system.  */
  def get_phys_pages(): CLongInt = extern

  /* Return number of available physical pages of memory in the system.  */
  def get_avphys_pages(): CLongInt = extern
}
