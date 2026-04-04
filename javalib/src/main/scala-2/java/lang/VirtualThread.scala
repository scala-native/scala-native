package java.lang

import scala.scalanative.runtime.UnsupportedFeature
import scala.scalanative.runtime.VirtualThreadScheduler

private[java] final class VirtualThread(
    name: String,
    characteristics: Int,
    task: Runnable
) extends Thread(name, characteristics) {

  // TODO: continuations-based thread implementation
  override def run(): Unit = UnsupportedFeature.virtualThreads()

  private[java] val scheduler: VirtualThreadScheduler = ???
  override def getState(): Thread.State = Thread.State.NEW

  private[lang] def joinNanos(nanos: scala.Long): scala.Boolean = ???
  private[lang] def sleepNanos(nanos: scala.Long): Unit = ???
  private[lang] def tryYield(): Unit = ???
  private[java] def park(): Unit = ???
  private[java] def parkNanos(nanos: scala.Long): Unit = ???
  private[java] def unpark(): Unit = ???
}
