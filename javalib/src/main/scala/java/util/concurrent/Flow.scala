package java.util.concurrent

object Flow {

  trait Processor[T, R] extends Subscriber[T] with Publisher[R]

  @FunctionalInterface
  trait Publisher[T] {
    def subscribe(subscriber: Subscriber[_ >: T]): Unit
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
