package scala.scalanative
package posix.sys

import native.{CInt, CString, extern, name}
import scala.scalanative.posix.sys.types.key_t

@extern
object ipc {

  def ftok(pathname: CString, proj_id: CInt): key_t = extern

  // Macros

  @name("scalanative_ipc_creat")
  def IPC_CREAT: CInt = extern

  @name("scalanative_ipc_excl")
  def IPC_EXCL: CInt = extern

  @name("scalanative_ipc_nowait")
  def IPC_NOWAIT: CInt = extern

  @name("scalanative_ipc_private")
  def IPC_PRIVATE: CInt = extern

  @name("scalanative_ipc_stat")
  def IPC_STAT: CInt = extern

  @name("scalanative_ipc_set")
  def IPC_SET: CInt = extern

  @name("scalanative_ipc_rmid")
  def IPC_RMID: CInt = extern
}
