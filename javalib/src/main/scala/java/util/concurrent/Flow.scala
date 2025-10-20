// Ported from Scala.js commit: fb20d6f dated: 2023-01-20

package java.util.concurrent

import scalanative.annotation.alwaysinline

object Flow {

  @alwaysinline def defaultBufferSize(): Int = 256

  trait Processor[T, R] extends Subscriber[T] with Publisher[R]

  @FunctionalInterface
  trait Publisher[T] {
    def subscribe(subscriber: Subscriber[? >: T]): Unit
  }

  trait Subscriber[T] {
    def onSubscribe(subscription: Subscription): Unit
    def onNext(item: T): Unit
    def onError(throwable: Throwable): Unit
    def onComplete(): Unit
  }

  trait Subscription {
    def request(n: Long): Unit
    def cancel(): Unit
  }

}
