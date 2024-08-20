package java.lang.management

object ManagementFactory {

  private lazy val MemoryBean = MemoryMXBean()
  private lazy val ThreadBean = ThreadMXBean()
  private lazy val OperatingSystemBean = OperatingSystemMXBean()
  private lazy val RuntimeBean = RuntimeMXBean()

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

}
