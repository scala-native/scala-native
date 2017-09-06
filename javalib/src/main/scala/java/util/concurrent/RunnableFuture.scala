package java.util.concurrent

// Ported from Harmony

trait RunnableFuture[V] extends Runnable with Future[V] {

  override def run(): Unit

}
