package scala.scalanative.posix

import scala.scalanative.native._
import scala.scalanative.posix.sys.types.{ssize_t, mode_t}

// http://man7.org/linux/man-pages/man7/mq_overview.7.html

@extern
object mqueue {

  def mq_open(name: CString,
              oflag: CInt,
              mode: mode_t,
              attr: Ptr[CStruct4]): mqd_t                 = extern
  def mq_close(mqdes: mqd_t): CInt                        = extern
  def mq_getattr(mqdes: mqd_t, attr: Ptr[CStruct4]): CInt = extern
  def mq_setattr(mqdes: mqd_t,
                 newattr: Ptr[CStruct4],
                 oldattr: Ptr[CStruct4]): CInt           = extern
  def mq_unlink(name: CString): CInt                     = extern
  def mq_notify(mqdes: mqd_t, sevp: Ptr[CStruct6]): CInt = extern
  def mq_receive(mqdes: mqd_t,
                 msg_ptr: CString,
                 msg_len: CSize,
                 msg_prio: Ptr[CUnsignedInt]): ssize_t = extern
  def mq_send(mqdes: mqd_t,
              msg_ptr: CString,
              msglen: CSize,
              msg_prio: CUnsignedInt): CInt = extern
  def timedreceive(mqdes: mqd_t,
                   msg_ptr: CString,
                   msglen: CSize,
                   msg_prio: Ptr[CUnsignedInt],
                   abs_timeout: Ptr[CStruct2]): ssize_t = extern
  def timedsend(mqdes: mqd_t,
                msg_ptr: CString,
                msglen: CSize,
                msg_prio: CUnsignedInt,
                abs_timeout: Ptr[CStruct2]): CInt = extern

  // Types
  type mqd_t = CUnsignedLong

}
