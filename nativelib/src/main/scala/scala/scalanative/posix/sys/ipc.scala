package scala.scalanative.posix.sys

import scala.scalanative.native.{name, extern}

/**
 * Created by remi on 01/03/17.
 */
@extern
object ipc {

  // Macros
  @name("scalanative_ipc_stat")
  def IPC_STAT = extern
  @name("scalanative_ipc_set")
  def IPC_SET = extern
  @name("scalanative_ipc_rmid")
  def IPC_RMID = extern
  @name("scalanative_ipc_info")
  def IPC_INFO = extern
  @name("scalanative_ipc_private")
  def IPC_PRIVATE = extern
  @name("scalanative_ipc_creat")
  def IPC_CREATE = extern
  @name("scalanative_ipc_execl")
  def IPC_EXECL = extern
  @name("scalanative_ipc_nowait")
  def IPC_NOWAIT = extern
}
