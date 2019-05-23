package scala.scalanative
package posix.sys

import scala.scalanative.unsigned._
import scala.scalanative.unsafe._

@extern
object types {

  type blkcnt_t = CLong

  type blksize_t = CLong

  type clock_t = CLong

  type clockid_t = CInt

  type dev_t = CUnsignedLongLong

  type fsblkcnt_t = CUnsignedLong

  type fsfilcnt_t = CUnsignedLong

  type gid_t = CUnsignedInt

  type id_t = CUnsignedInt

  type ino_t = CUnsignedLong

  type key_t = CInt

  type mode_t = CUnsignedInt

  type nlink_t = CUnsignedLong

  type off_t = CLong

  type pid_t = CInt

  type pthread_attr_t = ULong

  type pthread_barrier_t = ULong

  type pthread_barrierattr_t = ULong

  type pthread_cond_t = ULong

  type pthread_condattr_t = ULong

  type pthread_key_t = ULong

  type pthread_mutex_t = ULong

  type pthread_mutexattr_t = ULong

  type pthread_once_t = ULong

  type pthread_rwlock_t = ULong

  type pthread_rwlockattr_t = ULong

  type pthread_spinlock_t = ULong

  type pthread_t = ULong

  type size_t = CSize

  type ssize_t = CSSize

  type suseconds_t = CLong

  type time_t = CLong

  type timer_t = Ptr[CUnsignedLong] // Actually C void *. Used as Ptr[Any]

  type uid_t = CUnsignedInt

}
