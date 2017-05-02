package scala.scalanative
package posix.sys

import native.{CInt, extern, name}

@extern
object ipc {

  // Macros
  @name("scalanative_ipc_stat")
  def IPC_STAT: CInt = extern
  @name("scalanative_ipc_set")
  def IPC_SET: CInt = extern
  @name("scalanative_ipc_rmid")
  def IPC_RMID: CInt = extern
  @name("scalanative_ipc_info")
  def IPC_INFO: CInt = extern
  @name("scalanative_ipc_private")
  def IPC_PRIVATE: CInt = extern
  @name("scalanative_ipc_creat")
  def IPC_CREATE: CInt = extern
  @name("scalanative_ipc_execl")
  def IPC_EXECL: CInt = extern
  @name("scalanative_ipc_nowait")
  def IPC_NOWAIT: CInt = extern
}
