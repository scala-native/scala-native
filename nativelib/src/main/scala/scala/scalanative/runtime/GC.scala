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
  @name("scalanative_GC_alloc")
  def alloc(cls: Class[_], size: Int): RawPtr = extern
  @name("scalanative_GC_alloc_atomic")
  def alloc_atomic(cls: Class[_], size: Int): RawPtr = extern
  @name("scalanative_GC_alloc_small")
  def alloc_small(cls: Class[_], size: Int): RawPtr = extern
  @name("scalanative_GC_alloc_large")
  def alloc_large(cls: Class[_], size: Int): RawPtr = extern
  @name("scalanative_GC_collect")
  def collect(): Unit = extern
  @name("scalanative_GC_init")
  def init(): Unit = extern
  @name("scalanative_GC_register_weak_reference_handler")
  def registerWeakReferenceHandler(handler: CFuncPtr0[Unit]): Unit = extern
  @name("scalanative_GC_get_init_heapsize")
  def getInitHeapSize(): CSize = extern
  @name("scalanative_GC_get_max_heapsize")
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
  private type Handle = CVoidPtr
  private type DWord = CUnsignedInt
  private type SecurityAttributes = CStruct3[DWord, CVoidPtr, Boolean]
  private type PtrAny = CVoidPtr
  type ThreadRoutineArg = PtrAny
  type ThreadStartRoutine = CFuncPtr1[ThreadRoutineArg, PtrAny]

  /** Proxy to pthread_create which registers created thread in the GC */
  @name("scalanative_GC_pthread_create")
  def pthread_create(
      thread: Ptr[pthread_t],
      attr: Ptr[pthread_attr_t],
      startroutine: ThreadStartRoutine,
      args: ThreadRoutineArg
  ): CInt = extern

  /** Proxy to CreateThread which registers created thread in the GC */
  @name("scalanative_GC_CreateThread")
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
  @name("scalanative_GC_set_mutator_thread_state")
  private[scalanative] def setMutatorThreadState(
      newState: MutatorThreadState
  ): Unit = extern

  /** A call to GC yield mechanism used for polling the GC StopTheWorld event.
   *  If the GarbageCollector wants to perform collection it would stop the
   *  calling thread until GC is done and it's safe to continue execution.
   *  Lowering phase would introduce calls of this function to check if it
   *  should stop execution of the thread.
   */
  @name("scalanative_GC_yield")
  private[scalanative] def `yield`(): Unit = extern

  /** Address of yield point trap - conditionally protected memory address used
   *  for polling StopTheWorld event. Lowering phase would introduce write/read
   *  instruction to this address to check if it should stop execution of the
   *  thread. Upon write/read to protected memory special signal handler (UNIX)
   *  or exceptions filter (Windows) would be triggered leading to stopping
   *  execution of the thread. Used only in release mode for low-overhead
   *  yieldpoints
   */
  @name("scalanative_GC_yieldpoint_trap")
  private[scalanative] var yieldPointTrap: RawPtr = extern

  /** Notify the Garbage Collector about the range of memory which should be
   *  scanned when marking the objects. The range should contain only memory NOT
   *  allocated using the GC, eg. using malloc. Otherwise it might lead to the
   *  undefined behaviour at runtime.
   *
   *  @param addressLow
   *    Start of the range including the first address that should be scanned
   *    when marking
   *  @param addressHigh
   *    End of the range including the last address that should be scanned when
   *    marking
   */
  @name("scalanative_GC_add_roots")
  def addRoots(addressLow: CVoidPtr, addressHigh: CVoidPtr): Unit = extern

  /** Notify the Garbage Collector about the range of memory which should no
   *  longer should be scanned when marking the objects. Every previously
   *  registered range of addressed using [[addRoots]] which is fully contained
   *  withen the range of addressLow and addressHigh would be exluded from the
   *  subsequent scanning during the GC. It is safe to pass a range of addressed
   *  which doen't match any of the previously registered memory regions.
   *
   *  @param addressLow
   *    Start of the range including the first address that should be scanned
   *    when marking
   *  @param addressHigh
   *    End of the range including the last address that should be scanned when
   *    marking
   */
  @name("scalanative_GC_remove_roots")
  def removeRoots(addressLow: CVoidPtr, addressHigh: CVoidPtr): Unit = extern
}
