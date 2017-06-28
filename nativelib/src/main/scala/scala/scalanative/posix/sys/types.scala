package scala.scalanative
package posix.sys

import scala.scalanative.native.{CInt, ULong, extern}

@extern
object types {

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

  type clockid_t = CInt
}
