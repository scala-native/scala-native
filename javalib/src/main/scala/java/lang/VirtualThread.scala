package java.lang

import scala.scalanative.runtime.UnsupportedFeature

final private[lang] class VirtualThread(
    name: String,
    characteristics: Int,
    task: Runnable
) extends Thread(name, characteristics) {

  // TODO: continuations-based thread implementation
  override def run(): Unit = UnsupportedFeature.virtualThreads()

  override def getState(): Thread.State = Thread.State.NEW
}
