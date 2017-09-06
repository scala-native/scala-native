package java.util
package concurrent.locks

private class LockSupport {}

object LockSupport {

  // To be implemented, with thread suspend/resume

  def unpark(thread: Thread): Unit = {}

  def park(): Unit = {}

  def parkNanos(nanos: Long): Unit = {}

  def parkUntil(deadline: Long): Unit = {}

}
