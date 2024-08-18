package java.lang.management

object ManagementFactory {

  private lazy val MemoryBean = MemoryMXBean()

  /** Returns the memory-specific bean.
   *
   *  @example
   *    {{{
   *  val memoryBean = getMemoryMXBean()
   *  println(s"current heap: $${memoryBean.getHeapMemoryUsage().getUsed()}")
   *
   *  val list = List.fill(Short.MaxValue)(0) // allocate memory
   *  println(s"current heap: $${memoryBean.getHeapMemoryUsage().getUsed()}")
   *    }}}
   */
  def getMemoryMXBean(): MemoryMXBean = MemoryBean

}
