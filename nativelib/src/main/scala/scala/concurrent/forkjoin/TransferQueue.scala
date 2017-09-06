package scala.concurrent.forkjoin

import java.util.concurrent.{BlockingQueue, TimeUnit}

trait TransferQueue[E] extends BlockingQueue[E] {

  def tryTransfer(e: E): Boolean

  def transfer(e: E): Unit

  def tryTransfer(e: E, timeout: Long, unit: TimeUnit): Boolean

  def hasWaitingConsumer: Boolean

  def getWaitingConsumerCount: Int

}
