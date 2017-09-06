package scala.concurrent.forkjoin

class ForkJoinWorkerThread(final var pool: ForkJoinPool)
    extends Thread("aForkJoinWorkerThread") {

  final var workQueue
    : ForkJoinPool.WorkQueue = pool.registerWorker(this) // work-stealing mechanics

  def getPool: ForkJoinPool = pool

  def getPoolIndex: Int = workQueue.poolIndex

  protected def onStart(): Unit = {}

  protected def onTermination(exception: Throwable) = {}

  override def run(): Unit = {
    var exception: Throwable = null
    try {
      onStart()
      pool.runWorker(workQueue)
    } catch {
      case ex: Throwable => exception = ex
      case _: Throwable  =>
    } finally {
      try {
        onTermination(exception)
      } catch {
        case ex: Throwable => if (exception == null) exception = ex
        case _: Throwable  =>
      } finally {
        pool.deregisterWorker(this, exception)
      }
    }
  }

}
