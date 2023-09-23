package java.util.concurrent

abstract class CompletableFuture[T] extends Future[T] with CompletionStage[T] {}

object CompletableFuture {
  trait AsynchronousCompletionTask
}
