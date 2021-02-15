package scala.scalanative
package posix

import scala.scalanative.unsafe._

/**
 * aio.h - asynchronous input and output
 */
@extern
object aio {
  import sys.types

  // header shall define the aiocb structure, which shall include at least the following members
  type aiocb = CStruct7[
    CInt, // aio_fildes     File descriptor
    off_t, // aio_offset     File offset
    Ptr[Byte], // aio_buf        Location of buffer
    size_t, // aio_nbytes     Length of transfer
    CInt, // aio_reqprio    Request priority offset
    Ptr[sigevent], // aio_sigevent   Signal number and value
    CInt // aio_lio_opcode Operation to be performed
  ]

  // types as described in <sys/types.h>
  type pthread_attr_t = types.pthread_attr_t
  type off_t          = types.off_t
  type size_t         = types.size_t
  type ssize_t        = types.ssize_t

  // timespec structure as described in <time.h>
  type timespec = time.timespec

  // sigevent structure and sigval union as described in <signal.h>
  type sigevent = signal.sigevent
  type sigval   = signal.sigval

  // header shall define the following symbolic constants
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

  // The following shall be declared as functions and may also be defined as macros.
  // Function prototypes shall be provided
  @name("scalanative_aio_cancel")
  def aio_cancel(fd: CInt, aiocbp: Ptr[aiocb]): CInt = extern
  @name("scalanative_aio_error")
  def aio_error(aiocbp: Ptr[aiocb]): CInt = extern
  // File Synchronization and Synchronized Input and Output are optional and extensions to the ISO C standard.
  @name("scalanative_aio_fsync")
  def aio_fsync(op: CInt, aiocbp: Ptr[aiocb]): CInt = extern
  @name("scalanative_aio_read")
  def aio_read(aiocbp: Ptr[aiocb]): CInt = extern
  @name("scalanative_aio_return")
  def aio_return(aiocbp: Ptr[aiocb]): ssize_t = extern
  @name("scalanative_aio_suspend")
  def aio_suspend(aiocblist: Ptr[Ptr[aiocb]],
                  nent: CInt,
                  timeoutp: Ptr[timespec]): CInt = extern
  @name("scalanative_aio_write")
  def aio_write(aiocbp: Ptr[aiocb]): CInt = extern
  @name("scalanative_lio_listio")
  def lio_listio(mode: CInt,
                 aiocblist: Ptr[Ptr[aiocb]],
                 nent: CInt,
                 sigp: Ptr[sigevent]): CInt = extern
}

object aioOps {
  import aio._

  // TODO: move to signal
  implicit class sigevent_ops(val p: Ptr[sigevent]) extends AnyVal {
    def sigev_notify: CInt                                      = !p._1
    def sigev_notify_=(value: CInt): Unit                       = !p._1 = value
    def sigev_signo: CInt                                       = !p._2
    def sigev_signo_=(value: CInt): Unit                        = !p._2 = value
    def sigev_value: Ptr[sigval]                                = !p._3
    def sigev_value_=(value: Ptr[sigval]): Unit                 = !p._3 = value
    def sigev_notify_function: CFunctionPtr1[Ptr[sigval], Unit] = !p._4
    def sigev_notify_function_=(value: CFunctionPtr1[Ptr[sigval], Unit]): Unit =
      !p._4 = value
    def sigev_notify_attributes: Ptr[pthread_attr_t] = !p._5
    def sigev_notify_attributes_=(value: Ptr[pthread_attr_t]): Unit =
      !p._5 = value
  }

  implicit class aiocb_ops(val p: Ptr[aiocb]) extends AnyVal {
    def aio_fildes: CInt                  = !p._1
    def aio_fildes_=(value: CInt): Unit   = !p._1 = value
    def aio_offset: off_t                 = !p._2
    def aio_offset_=(value: off_t): Unit  = !p._2 = value
    def aio_buf: Ptr[Byte]                = !p._3
    def aio_buf_=(value: Ptr[Byte]): Unit = !p._3 = value
    def aio_nbytes: size_t                = !p._4
    def aio_nbytes_=(value: size_t): Unit = !p._4 = value
    def aio_reqprio: CInt                 = !p._5
    def aio_reqprio_=(value: CInt): Unit  = !p._5 = value
    def aio_sigevent: Ptr[sigevent]       = !p._6
    def aio_sigevent_=(value: Ptr[sigevent]): Unit =
      !p._6 = value
    def aio_lio_opcode: CInt                = !p._7
    def aio_lio_opcode_=(value: CInt): Unit = !p._7 = value
  }

  // TODO: move to signal
  implicit class sigval_ops(val p: Ptr[sigval]) extends AnyVal {
    def sival_int: Ptr[CInt]                = p.cast[Ptr[CInt]]
    def sival_int_=(value: CInt): Unit      = !p.cast[Ptr[CInt]] = value
    def sival_ptr: Ptr[Ptr[Byte]]           = p.cast[Ptr[Ptr[Byte]]]
    def sival_ptr_=(value: Ptr[Byte]): Unit = !p.cast[Ptr[Ptr[Byte]]] = value
  }

}
