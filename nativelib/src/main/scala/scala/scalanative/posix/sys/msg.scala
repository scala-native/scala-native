package scala.scalanative.posix.sys

import scala.scalanative.native._
import scala.scalanative.posix.sys.types._
import scala.scalanative.runtime.time.time_t

/**
 * Created by remi on 01/03/17.
 */
@extern
object msg {

  // msgctl
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
  type msqid_ds = CStruct9[ipc_perm,
                           time_t,
                           time_t,
                           time_t,
                           CUnsignedLong,
                           msgqnum_t,
                           msglen_t,
                           pid_t,
                           pid_t]

  // Macros
  @name("scalanative_msg_info")
  def MSG_INFO = extern
  @name("scalanative_msg_stat")
  def MSG_STAT = extern
  @name("scalanative_msg_noerror")
  def MSG_NOERROR = extern
  @name("scalanative_msg_copy")
  def MSG_COPY = extern
  @name("scalanative_msg_except")
  def MSG_EXCEPT = extern

}
