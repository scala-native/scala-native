package scala.scalanative
package runtime

import scalanative.unsafe._
import scala.scalanative.annotation.alwaysinline

/** The Boehm GC conservative garbage collector
 *
 *  @see
 *    [[http://hboehm.info/gc/gcinterface.html C Interface]]
 */
@extern
object GC {
  @name("scalanative_alloc")
  def alloc(cls: Class[_], size: CSize): RawPtr = extern
  @name("scalanative_alloc_atomic")
  def alloc_atomic(cls: Class[_], size: CSize): RawPtr = extern
  @name("scalanative_alloc_small")
  def alloc_small(cls: Class[_], size: CSize): RawPtr = extern
  @name("scalanative_alloc_large")
  def alloc_large(cls: Class[_], size: CSize): RawPtr = extern
  @name("scalanative_collect")
  def collect(): Unit = extern
  @name("scalanative_init")
  def init(): Unit = extern
  @name("scalanative_register_weak_reference_handler")
  def registerWeakReferenceHandler(handler: Ptr[Byte]): Unit = extern
  @name("scalanative_get_init_heapsize")
  def getInitHeapSize(): CSize = extern
  @name("scalanative_get_max_heapsize")
  def getMaxHeapSize(): CSize = extern

  /*  Multithreading awareness for GC Every implementation of GC supported in
   *  ScalaNative needs to register a given thread The main thread is
   *  automatically registered. Every additional thread needs to explicitly
   *  notify GC about it's creation and termination. For that purpose we follow
   *  the Boehm GC convention for overloading the pthread_create/CreateThread
   *  functions respectively for POSIX and Windows.
   */
  private type pthread_t = CUnsignedLongInt
  private type pthread_attr_t = CUnsignedLongLong
  private type Handle = Ptr[Byte]
  private type DWord = CUnsignedInt
  private type SecurityAttributes = CStruct3[DWord, Ptr[Byte], Boolean]
  private type PtrAny = Ptr[Byte]
  type ThreadRoutineArg = PtrAny
  type ThreadStartRoutine = CFuncPtr1[ThreadRoutineArg, PtrAny]

  /** Proxy to pthread_create which registers created thread in the GC */
  @name("scalanative_pthread_create")
  def pthread_create(
      thread: Ptr[pthread_t],
      attr: Ptr[pthread_attr_t],
      startroutine: ThreadStartRoutine,
      args: ThreadRoutineArg
  ): CInt = extern

  /** Proxy to CreateThread which registers created thread in the GC */
  @name("scalanative_CreateThread")
  def CreateThread(
      threadAttributes: Ptr[SecurityAttributes],
      stackSize: CSize,
      startRoutine: ThreadStartRoutine,
      routineArg: ThreadRoutineArg,
      creationFlags: DWord,
      threadId: Ptr[DWord]
  ): Handle = extern

  private[scalanative] type MutatorThreadState = CInt
  private[scalanative] object MutatorThreadState {

    /** Thread executes Scala Native code using GC following cooperative mode -
     *  it periodically polls for synchronization events.
     */
    @alwaysinline final def Managed = 0

    /** Thread executes foreign code (syscalls, C functions) and is not able to
     *  modify the state of the GC. Upon synchronization event garbage collector
     *  would ignore this thread. Upon returning from foreign execution thread
     *  would stop until synchronization event would finish.
     */
    @alwaysinline final def Unmanaged = 1
  }

  /** Notifies change of internal state of thread to the GC. Used by Scala
   *  Native runtime on calls/returns from potentially blocking extern functions
   */
  @name("scalanative_gc_set_mutator_thread_state")
  private[scalanative] def setMutatorThreadState(
      newState: MutatorThreadState
  ): Unit = extern

  /** Address of safepoint - conditionally protected memory address used for
   *  polling StopTheWorld event. Lowering phase would introduce write/read
   *  instruction to this address to check if it should stop execution of the
   *  thread. Upon write/read to protected memory special signal handler (UNIX)
   *  or exceptions filter (Windows) would be triggered leading to stopping
   *  execution of the thread.
   */
  @name("scalanative_gc_safepoint")
  private[scalanative] var safepoint: RawPtr = extern

}
