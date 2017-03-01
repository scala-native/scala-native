package scala.scalanative.posix.sys

import scala.scalanative.native._
import scala.scalanative.posix.sys.types.{ssize_t, key_t}

/**
  * Created by remi on 01/03/17.
  */
@extern
object msg {

  def msgctl(msqid: CInt, cmd: CInt, buf: Ptr[CStruct9]): CInt = extern
  def msgget(key: key_t, msgflg: CInt): CInt = extern
  def msgsnd(msqid: CInt, msgp: Ptr[Byte], msgz: CSize, msgflg: CInt): CInt = extern
  def msgrcv(msqid: CInt, msgp: Ptr[Byte], msgz: CSize, msgtyp: CLong, msgflg: CInt): ssize_t = extern

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
