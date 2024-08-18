package java.lang.management

/** Represents the runtime memory usage.
 *
 *  @param init
 *    the initial amount of memory (bytes)
 *
 *  @param used
 *    the currently used amount of memory (bytes)
 *
 *  @param committed
 *    the currently committed amount of memory (bytes)
 *
 *  @param max
 *    the maximum amount of memory that can be used (bytes)
 */
class MemoryUsage(init: Long, used: Long, committed: Long, max: Long) {
  require(init >= -1, s"init ($init) must be >= -1.")
  require(used >= -1, s"used ($used) must be >= -1")
  require(committed >= -1, s"committed ($max) must be >= -1.")
  require(max >= -1, s"max ($max) must be >= -1.")

  /** The initial amount of memory (bytes).
   */
  def getInit(): Long = init

  /** The currently used amount of memory (bytes).
   */
  def getUsed(): Long = used

  /** The currently committed amount of memory (bytes)
   */
  def getCommitted(): Long = committed

  /** The maximum amount of memory that can be used (bytes).
   */
  def getMax(): Long = max

  override def toString(): String =
    s"init = $init(${init >> 10}K) used = $used(${used >> 10}K) " +
      s"committed = $committed(${committed >> 10}K) max = $max(${max >> 10}K)"
}
