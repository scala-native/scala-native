package scala.scalanative
package posix.sys

import scala.scalanative.native.{CInt, CStruct0, Ptr, extern}

@extern
object types {

  type pthread_attr_t = Ptr[CStruct0]

  type pthread_barrier_t = Ptr[CStruct0]

  type pthread_barrierattr_t = Ptr[CStruct0]

  type pthread_cond_t = Ptr[CStruct0]

  type pthread_condattr_t = Ptr[CStruct0]

  type pthread_key_t = Ptr[CStruct0]

  type pthread_mutex_t = Ptr[CStruct0]

  type pthread_mutexattr_t = Ptr[CStruct0]

  type pthread_once_t = Ptr[CStruct0]

  type pthread_rwlock_t = Ptr[CStruct0]

  type pthread_rwlockattr_t = Ptr[CStruct0]

  type pthread_spinlock_t = Ptr[CStruct0]

  type pthread_t = Ptr[CStruct0]

  type clockid_t = CInt
}
