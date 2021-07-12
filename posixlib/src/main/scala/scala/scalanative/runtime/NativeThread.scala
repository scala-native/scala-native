package scala.scalanative.runtime

import scala.scalanative.posix.sys.types.{pthread_attr_t, pthread_t}
import scala.scalanative.unsafe.{CInt, CSize, Ptr, extern, name}

@extern
object NativeThread {

  @name("get_max_priority")
  def THREAD_MAX_PRIORITY: Int = extern

  @name("get_min_priority")
  def THREAD_MIN_PRIORITY: Int = extern

  @name("get_norm_priority")
  def THREAD_NORM_PRIORITY: Int = extern

  @name("get_stack_size")
  def THREAD_DEFAULT_STACK_SIZE: CSize = extern

  @name("set_priority")
  def setPriority(thread: pthread_t, priority: CInt): Unit = extern

  @name("attr_set_priority")
  def attrSetPriority(attr: Ptr[pthread_attr_t], priority: CInt): Unit = extern
}
