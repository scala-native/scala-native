package scala.scalanative
package posix

import scala.scalanative.native._
import sys.types.{pthread_attr_t}

@extern
object aio {

  // types as described in <sys/types.h> - imported above for use in this and ops
  // pthread_attr_t
  type off_t   = CLongLong
  type size_t  = CSize
  type ssize_t = CSSize

  // struct timespec structure as described in <time.h>
  import time.timespec

  // sigevent structure and sigval union as described in <signal.h>
  type sigevent = CStruct5[CInt,
                           CInt,
                           sigval,
                           CFunctionPtr1[sigval, Unit],
                           Ptr[pthread_attr_t]]
  type sigval = CArray[Byte, Nat._8]

  // constants
  @name("scalanative_aio_alldone")
  def AIO_ALLDONE: CInt = extern

  @name("scalanative_aio_canceled")
  def AIO_CANCELED: CInt = extern

  @name("scalanative_aio_notcanceled")
  def AIO_NOTCANCELED: CInt = extern

  @name("scalanative_lio_nop")
  def LIO_NOP: CInt = extern

  @name("scalanative_lio_nowait")
  def LIO_NOWAIT: CInt = extern

  @name("scalanative_lio_read")
  def LIO_READ: CInt = extern

  @name("scalanative_lio_wait")
  def LIO_WAIT: CInt = extern

  @name("scalanative_lio_write")
  def LIO_WRITE: CInt = extern

  type aiocb = CStruct7[CInt,
                        off_t,
                        Ptr[Byte],
                        size_t,
                        CInt,
                        CFunctionPtr1[sigevent, Unit],
                        CInt]

  def aio_cancel(fd: CInt, aiocbp: Ptr[aiocb]): CInt = extern
  def aio_error(aiocbp: Ptr[aiocb]): CInt            = extern
  def aio_fsync(op: CInt, aiocbp: Ptr[aiocb]): CInt  = extern
  def aio_read(aiocbp: Ptr[aiocb]): CInt             = extern
  def aio_return(aiocbp: Ptr[aiocb]): ssize_t        = extern
  def aio_suspend(aiocblist: Ptr[Ptr[aiocb]],
                  nent: CInt,
                  timeoutp: Ptr[timespec]): CInt = extern
  def aio_write(aiocbp: Ptr[aiocb]): CInt        = extern
  def lio_listio(mode: CInt,
                 aiocblist: Ptr[Ptr[aiocb]],
                 nent: CInt,
                 sigp: Ptr[sigevent]): CInt = extern
}

import aio._

object aioOps {

  implicit class sigevent_ops(val p: Ptr[sigevent]) extends AnyVal {
    def sigev_notify: CInt                                 = !p._1
    def sigev_notify_=(value: CInt): Unit                  = !p._1 = value
    def sigev_signo: CInt                                  = !p._2
    def sigev_signo_=(value: CInt): Unit                   = !p._2 = value
    def sigev_value: sigval                                = !p._3
    def sigev_value_=(value: sigval): Unit                 = !p._3 = value
    def sigev_notify_function: CFunctionPtr1[sigval, Unit] = !p._4
    def sigev_notify_function_=(value: CFunctionPtr1[sigval, Unit]): Unit =
      !p._4 = value
    def sigev_notify_attributes: Ptr[pthread_attr_t] = !p._5
    def sigev_notify_attributes_=(value: Ptr[pthread_attr_t]): Unit =
      !p._5 = value
  }

  //def sigevent()(implicit z: Zone): Ptr[sigevent] = native.alloc[sigevent]

  implicit class aiocb_ops(val p: Ptr[aiocb]) extends AnyVal {
    def aio_fildes: CInt                            = !p._1
    def aio_fildes_=(value: CInt): Unit             = !p._1 = value
    def aio_offset: off_t                           = !p._2
    def aio_offset_=(value: off_t): Unit            = !p._2 = value
    def aio_buf: Ptr[Byte]                          = !p._3
    def aio_buf_=(value: Ptr[Byte]): Unit           = !p._3 = value
    def aio_nbytes: size_t                          = !p._4
    def aio_nbytes_=(value: size_t): Unit           = !p._4 = value
    def aio_reqprio: CInt                           = !p._5
    def aio_reqprio_=(value: CInt): Unit            = !p._5 = value
    def aio_sigevent: CFunctionPtr1[sigevent, Unit] = !p._6
    def aio_sigevent_=(value: CFunctionPtr1[sigevent, Unit]): Unit =
      !p._6 = value
    def aio_lio_opcode: CInt                = !p._7
    def aio_lio_opcode_=(value: CInt): Unit = !p._7 = value
  }

  //def aiocb()(implicit z: Zone): Ptr[aiocb] = native.alloc[aiocb]

  implicit class sigval_ops(val p: Ptr[sigval]) extends AnyVal {
    def sival_int: Ptr[CInt]                = p.cast[Ptr[CInt]]
    def sival_int_=(value: CInt): Unit      = !p.cast[Ptr[CInt]] = value
    def sival_ptr: Ptr[Ptr[Byte]]           = p.cast[Ptr[Ptr[Byte]]]
    def sival_ptr_=(value: Ptr[Byte]): Unit = !p.cast[Ptr[Ptr[Byte]]] = value
  }

}
