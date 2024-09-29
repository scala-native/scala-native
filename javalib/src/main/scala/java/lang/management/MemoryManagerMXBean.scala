package java.lang.management

trait MemoryManagerMXBean {

  /** Returns the name of this memory manager.
   */
  def getName(): String

  /** Returns `true` when the memory manager is valid and `false` otherwise.
   */
  def isValid(): Boolean

  /** Returns the name of the managed memory pools.
   */
  def getMemoryPoolNames(): Array[String]

}
