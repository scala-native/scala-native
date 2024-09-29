package java.lang.management

object ManagementFactory {

  private lazy val MemoryBean = MemoryMXBean()
  private lazy val ThreadBean = ThreadMXBean()
  private lazy val OperatingSystemBean = OperatingSystemMXBean()
  private lazy val RuntimeBean = RuntimeMXBean()
  private lazy val GarbageCollectorBean = GarbageCollectorMXBean()

  /** Returns the memory-specific bean.
   *
   *  @example
   *    {{{
   *  val memoryBean = ManagementFactory.getMemoryMXBean()
   *  println(s"current heap: $${memoryBean.getHeapMemoryUsage().getUsed()}")
   *
   *  val list = List.fill(Short.MaxValue)(0) // allocate memory
   *  println(s"current heap: $${memoryBean.getHeapMemoryUsage().getUsed()}")
   *    }}}
   */
  def getMemoryMXBean(): MemoryMXBean = MemoryBean

  /** Returns the thread-specific bean.
   *
   *  @example
   *    {{{
   *  val threadBean = ManagementFactory.getThreadMXBean()
   *  println(s"total threads: $${threadBean.getThreadCount()}")
   *    }}}
   */
  def getThreadMXBean(): ThreadMXBean = ThreadBean

  /** Returns the OS-specific bean.
   *
   *  @example
   *    {{{
   *  val osBean = ManagementFactory.getOperatingSystemMXBean()
   *  println(s"OS: $${osBean.getName()}")
   *    }}}
   */
  def getOperatingSystemMXBean(): OperatingSystemMXBean = OperatingSystemBean

  /** Returns the runtime-specific bean.
   *
   *  @example
   *    {{{
   *  val runtimeBean = ManagementFactory.getRuntimeMXBean()
   *  println(s"pid: $${runtimeBean.getPid()}")
   *    }}}
   */
  def getRuntimeMXBean(): RuntimeMXBean = RuntimeBean

  /** Returns a list of [[MemoryManagerMXBean]].
   *
   *  @example
   *    {{{
   *  val mmBean = ManagementFactory.getMemoryManagerMXBeans().get(0)
   *  println(s"Memory manager: $${mmBean.getName()}")
   *    }}}
   */
  def getMemoryManagerMXBeans(): java.util.List[MemoryManagerMXBean] =
    java.util.Collections.singletonList(GarbageCollectorBean)

  /** Returns a list of [[GarbageCollectorMXBean]].
   *
   *  @example
   *    {{{
   *  val gcBean = ManagementFactory.getGarbageCollectorMXBeans().get(0)
   *  println(s"Total collection time (ms): $${gcBean.getCollectionTime()}")
   *    }}}
   */
  def getGarbageCollectorMXBeans(): java.util.List[GarbageCollectorMXBean] =
    java.util.Collections.singletonList(GarbageCollectorBean)

}
