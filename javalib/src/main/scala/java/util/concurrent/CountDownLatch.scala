package java.util.concurrent

// No-op stub defined to allow usage of lazy vals with -Ylightweight-lazy-vals
class CountDownLatch(count: Int) {

  def await(): Unit = ()

  def await(timeout: Long, unit: TimeUnit): Boolean = true

  def countDown(): Unit = ()

  def getCount(): Long = 0L
}
