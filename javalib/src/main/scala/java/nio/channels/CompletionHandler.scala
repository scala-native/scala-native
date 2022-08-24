package java.nio.channels

trait CompletionHandler[V, A] {

  def completed(result: V, attachment: A): Unit

  def failed(exc: Throwable, attachment: A): Unit

}
