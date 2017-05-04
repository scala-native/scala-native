package scala.scalanative
package posix.sys

import native._
import scala.scalanative.posix.time.time_t
import types._

// http://pubs.opengroup.org/onlinepubs/9699919799/

@extern
object msg {

  def msgctl(msqid: CInt, cmd: CInt, buf: Ptr[msqid_ds]): CInt = extern

  def msgget(key: key_t, msgflg: CInt): CInt                   = extern

  def msgsnd(msqid: CInt, msgp: Ptr[Byte], msgz: CSize, msgflg: CInt): CInt =
    extern

  def msgrcv(msqid: CInt,
             msgp: Ptr[Byte],
             msgz: CSize,
             msgtyp: CLong,
             msgflg: CInt): ssize_t = extern

  // Types
  type msgqnum_t = CUnsignedInt
  type msglen_t  = CUnsignedInt
  type ipc_perm =
    CStruct7[key_t, uid_t, gid_t, uid_t, gid_t, CUnsignedShort, CUnsignedShort]
  type msqid_ds = CStruct8[ipc_perm,
                           msgqnum_t,
                           msglen_t,
                           pid_t,
                           pid_t,
                           time_t,
                           time_t,
                           time_t]
  // Macros

  @name("scalanative_msg_noerror")
  def MSG_NOERROR: CInt = extern
}
