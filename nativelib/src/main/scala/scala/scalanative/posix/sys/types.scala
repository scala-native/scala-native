package scala.scalanative
package posix.sys

import scala.scalanative.native.{CInt, CStruct0, extern}

@extern
object types {

  type pthread_attr_t = CStruct0

  type pthread_barrier_t = CStruct0

  type pthread_barrierattr_t = CStruct0

  type pthread_cond_t = CStruct0

  type pthread_condattr_t = CStruct0

  type pthread_key_t = CStruct0

  type pthread_mutex_t = CStruct0

  type pthread_mutexattr_t = CStruct0

  type pthread_once_t = CStruct0

  type pthread_rwlock_t = CStruct0

  type pthread_rwlockattr_t = CStruct0

  type pthread_spinlock_t = CStruct0

  type pthread_t = CStruct0

  type clockid_t = CInt
}
