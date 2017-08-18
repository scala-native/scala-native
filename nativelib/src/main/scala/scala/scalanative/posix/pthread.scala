package scala.scalanative
package posix

import scala.scalanative.native.{
  CFunctionPtr0,
  CFunctionPtr1,
  CInt,
  CSize,
  Ptr,
  extern,
  name
}
import scala.scalanative.posix.sched.sched_param
import scala.scalanative.posix.sys.time.timespec
import scala.scalanative.posix.sys.types._

// SUSv2 version is used for compatibility
// see http://pubs.opengroup.org/onlinepubs/007908799/xsh/threads.html

@extern
object pthread {

  def pthread_atfork(prepare: routine, parent: routine, child: routine): CInt =
    extern

  def pthread_attr_destroy(attr: Ptr[pthread_attr_t]): CInt = extern

  def pthread_attr_getdetachstate(attr: Ptr[pthread_attr_t],
                                  detachstate: Ptr[CInt]): CInt = extern

  def pthread_attr_getguardsize(attr: Ptr[pthread_attr_t],
                                guardsize: Ptr[CSize]): CInt = extern

  def pthread_attr_getinheritsched(attr: Ptr[pthread_attr_t],
                                   inheritsched: Ptr[CInt]): CInt = extern

  def pthread_attr_getschedparam(attr: Ptr[pthread_attr_t],
                                 param: Ptr[sched_param]): CInt = extern

  def pthread_attr_getschedpolicy(attr: Ptr[pthread_attr_t],
                                  policy: Ptr[CInt]): CInt = extern

  def pthread_attr_getscope(attr: Ptr[pthread_attr_t], scope: Ptr[CInt]): CInt =
    extern

  def pthread_attr_getstacksize(attr: Ptr[pthread_attr_t],
                                stacksize: Ptr[CSize]): CInt = extern

  def pthread_attr_init(attr: Ptr[pthread_attr_t]): CInt = extern

  def pthread_attr_setdetachstate(attr: Ptr[pthread_attr_t],
                                  detachstate: CInt): CInt = extern

  def pthread_attr_setguardsize(attr: Ptr[pthread_attr_t],
                                guardsize: CSize): CInt = extern

  def pthread_attr_setinheritsched(attr: Ptr[pthread_attr_t],
                                   inheritsched: CInt): CInt = extern

  def pthread_attr_setschedparam(attr: Ptr[pthread_attr_t],
                                 param: Ptr[sched_param]): CInt = extern

  def pthread_attr_setschedpolicy(attr: Ptr[pthread_attr_t],
                                  policy: CInt): CInt = extern

  def pthread_attr_setscope(attr: Ptr[pthread_attr_t], scope: CInt): CInt =
    extern

  def pthread_attr_setstackaddr(attr: Ptr[pthread_attr_t],
                                stackaddr: Ptr[Byte]): CInt = extern

  def pthread_attr_setstacksize(attr: Ptr[pthread_attr_t],
                                stacksize: CSize): CInt = extern

  def pthread_cancel(thread: pthread_t): CInt = extern

  def pthread_cond_broadcast(cond: Ptr[pthread_cond_t]): CInt = extern

  def pthread_cond_destroy(cond: Ptr[pthread_cond_t]): CInt = extern

  def pthread_cond_init(cond: Ptr[pthread_cond_t],
                        attr: Ptr[pthread_condattr_t]): CInt = extern

  def pthread_cond_signal(cond: Ptr[pthread_cond_t]): CInt = extern

  def pthread_cond_timedwait(cond: Ptr[pthread_cond_t],
                             mutex: Ptr[pthread_mutex_t],
                             timespec: Ptr[timespec]): CInt = extern

  def pthread_cond_wait(cond: Ptr[pthread_cond_t],
                        mutex: Ptr[pthread_mutex_t]): CInt = extern

  def pthread_condattr_destroy(attr: Ptr[pthread_condattr_t]): CInt = extern

  def pthread_condattr_getpshared(attr: Ptr[pthread_condattr_t],
                                  pshared: Ptr[CInt]): CInt = extern

  def pthread_condattr_init(attr: Ptr[pthread_condattr_t]): CInt = extern

  def pthread_condattr_setpshared(attr: Ptr[pthread_condattr_t],
                                  pshared: CInt): CInt = extern

  def pthread_create(thread: Ptr[pthread_t],
                     attr: Ptr[pthread_attr_t],
                     startroutine: CFunctionPtr1[Ptr[Byte], Ptr[Byte]],
                     args: Ptr[Byte]): CInt = extern

  def pthread_detach(thread: pthread_t): CInt = extern

  def pthread_equal(thread1: pthread_t, thread2: pthread_t): CInt = extern

  def pthread_exit(retval: Ptr[Byte]): Unit = extern

  def pthread_getconcurrency(): CInt = extern

  def pthread_getschedparam(thread: pthread_t,
                            policy: Ptr[CInt],
                            param: Ptr[sched_param]): CInt = extern

  def pthread_getspecific(key: pthread_key_t): Ptr[Byte] = extern

  def pthread_join(thread: pthread_t, value_ptr: Ptr[Ptr[Byte]]): CInt = extern

  def pthread_key_create(key: Ptr[pthread_key_t],
                         destructor: CFunctionPtr1[Ptr[Byte], Unit]): CInt =
    extern

  def pthread_key_delete(key: pthread_key_t): CInt = extern

  def pthread_mutex_destroy(mutex: Ptr[pthread_mutex_t]): CInt = extern

  def pthread_mutex_getprioceiling(mutex: Ptr[pthread_mutex_t],
                                   prioceiling: Ptr[CInt]): CInt = extern

  def pthread_mutex_init(mutex: Ptr[pthread_mutex_t],
                         attr: Ptr[pthread_mutexattr_t]): CInt = extern

  def pthread_mutex_lock(mutex: Ptr[pthread_mutex_t]): CInt = extern

  def pthread_mutex_setprioceiling(mutex: Ptr[pthread_mutex_t],
                                   prioceiling: CInt,
                                   old_prioceiling: Ptr[CInt]): CInt = extern

  def pthread_mutex_trylock(mutex: Ptr[pthread_mutex_t]): CInt = extern

  def pthread_mutex_unlock(mutex: Ptr[pthread_mutex_t]): CInt = extern

  def pthread_mutexattr_destroy(attr: Ptr[pthread_mutexattr_t]): CInt = extern

  def pthread_mutexattr_getprioceiling(attr: Ptr[pthread_mutexattr_t],
                                       prioceiling: Ptr[CInt]): CInt = extern

  def pthread_mutexattr_getprotocol(attr: Ptr[pthread_mutexattr_t],
                                    protocol: Ptr[CInt]): CInt = extern

  def pthread_mutexattr_getpshared(attr: Ptr[pthread_mutexattr_t],
                                   pshared: Ptr[CInt]): CInt = extern

  def pthread_mutexattr_gettype(attr: Ptr[pthread_mutexattr_t],
                                tp: Ptr[CInt]): CInt = extern

  def pthread_mutexattr_init(attr: Ptr[pthread_mutexattr_t]): CInt = extern

  def pthread_mutexattr_setprioceiling(attr: Ptr[pthread_mutexattr_t],
                                       prioceiling: CInt): CInt = extern

  def pthread_mutexattr_setprotocol(attr: Ptr[pthread_mutexattr_t],
                                    protocol: CInt): CInt = extern

  def pthread_mutexattr_setpshared(attr: Ptr[pthread_mutexattr_t],
                                   pshared: CInt): CInt = extern

  def pthread_mutexattr_settype(attr: Ptr[pthread_mutexattr_t],
                                tp: CInt): CInt = extern

  def pthread_once(once_control: Ptr[pthread_once_t],
                   init_routine: routine): CInt = extern

  def pthread_rwlock_destroy(rwlock: Ptr[pthread_rwlock_t]): CInt = extern

  def pthread_rwlock_init(rwlock: Ptr[pthread_rwlock_t],
                          attr: Ptr[pthread_rwlockattr_t]): CInt = extern

  def pthread_rwlock_rdlock(rwlock: Ptr[pthread_rwlock_t]): CInt = extern

  def pthread_rwlock_tryrdlock(rwlock: Ptr[pthread_rwlock_t]): CInt = extern

  def pthread_rwlock_trywrlock(rwlock: Ptr[pthread_rwlock_t]): CInt = extern

  def pthread_rwlock_unlock(rwlock: Ptr[pthread_rwlock_t]): CInt = extern

  def pthread_rwlock_wrlock(rwlock: Ptr[pthread_rwlock_t]): CInt = extern

  def pthread_rwlockattr_destroy(attr: Ptr[pthread_rwlockattr_t]): CInt =
    extern

  def pthread_rwlockattr_getpshared(attr: Ptr[pthread_rwlockattr_t],
                                    pshared: Ptr[CInt]): CInt = extern

  def pthread_rwlockattr_init(attr: Ptr[pthread_rwlockattr_t]): CInt = extern

  def pthread_rwlockattr_setpshared(attr: Ptr[pthread_rwlockattr_t],
                                    pshared: CInt): CInt = extern

  def pthread_self(): pthread_t = extern

  def pthread_setcancelstate(state: CInt, oldstate: Ptr[CInt]): CInt = extern

  def pthread_setcanceltype(tp: CInt, oldtype: Ptr[CInt]): CInt = extern

  def pthread_setconcurrency(concurrency: CInt): CInt = extern

  def pthread_setschedparam(thread: pthread_t,
                            policy: CInt,
                            param: Ptr[sched_param]): CInt = extern

  def pthread_setspecific(key: pthread_key_t, value: Ptr[Byte]): CInt = extern

  def pthread_testcancel(): Unit = extern

  // Types
  type routine = CFunctionPtr0[Unit]

  // Macros
  @name("scalanative_pthread_cancel_asynchronous")
  def PTHREAD_CANCEL_ASYNCHRONOUS: CInt = extern

  @name("scalanative_pthread_cancel_enable")
  def PTHREAD_CANCEL_ENABLE: CInt = extern

  @name("scalanative_pthread_cancel_defered")
  def PTHREAD_CANCEL_DEFERRED: CInt = extern

  @name("scalanative_pthread_cancel_disable")
  def PTHREAD_CANCEL_DISABLE: CInt = extern

  @name("scalanative_pthread_canceled")
  def PTHREAD_CANCELED: Ptr[Byte] = extern

  @name("scalanative_pthread_create_deteached")
  def PTHREAD_CREATE_DETACHED: CInt = extern

  @name("scalanative_pthread_create_joinale")
  def PTHREAD_CREATE_JOINABLE: CInt = extern

  @name("scalanative_pthread_explicit_sched")
  def PTHREAD_EXPLICIT_SCHED: CInt = extern

  @name("scalanative_pthread_inherit_sched")
  def PTHREAD_INHERIT_SCHED: CInt = extern

  @name("scalanative_pthread_mutex_default")
  def PTHREAD_MUTEX_DEFAULT: CInt = extern

  @name("scalanative_pthread_mutex_errorcheck")
  def PTHREAD_MUTEX_ERRORCHECK: CInt = extern

  @name("scalanative_pthread_mutex_normal")
  def PTHREAD_MUTEX_NORMAL: CInt = extern

  @name("scalanative_pthread_mutex_recursive")
  def PTHREAD_MUTEX_RECURSIVE: CInt = extern

  @name("scalanative_pthread_once_init")
  def PTHREAD_ONCE_INIT: pthread_once_t = extern

  @name("scalanative_pthread_prio_inherit")
  def PTHREAD_PRIO_INHERIT: CInt = extern

  @name("scalanative_pthread_prio_none")
  def PTHREAD_PRIO_NONE: CInt = extern

  @name("scalanative_pthread_prio_protect")
  def PTHREAD_PRIO_PROTECT: CInt = extern

  @name("scalanative_pthread_process_shared")
  def PTHREAD_PROCESS_SHARED: CInt = extern

  @name("scalanative_pthread_process_private")
  def PTHREAD_PROCESS_PRIVATE: CInt = extern

  @name("scalanative_pthread_scope_process")
  def PTHREAD_SCOPE_PROCESS: CInt = extern

  @name("scalanative_pthread_scope_system")
  def PTHREAD_SCOPE_SYSTEM: CInt = extern

  @name("scalanative_pthread_cond_initializer")
  def PTHREAD_COND_INITIALIZER: pthread_cond_t = extern

  @name("scalanative_pthread_mutex_initializer")
  def PTHREAD_MUTEX_INITIALIZER: pthread_mutex_t = extern

  @name("scalanative_pthread_rwlock_initializer")
  def PTHREAD_RWLOCK_INITIALIZER: pthread_rwlock_t = extern

}
