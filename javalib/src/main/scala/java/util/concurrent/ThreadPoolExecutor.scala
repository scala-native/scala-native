package java.util
package concurrent

import ThreadPoolExecutor._

object ThreadPoolExecutor {
  class CallerRunsPolicy extends RejectedExecutionHandler {
    def rejectedExecution(r: Runnable, e: ThreadPoolExecutor): Unit = ???
  }
  class AbortPolicy extends RejectedExecutionHandler {
    def rejectedExecution(r: Runnable, e: ThreadPoolExecutor): Unit = {
      throw new RejectedExecutionException()
    }
  }

  class DiscardPolicy extends RejectedExecutionHandler {
    def rejectedExecution(r: Runnable, e: ThreadPoolExecutor): Unit = ???
  }

  class DiscardOldestPolicy extends RejectedExecutionHandler {
    def rejectedExecution(r: Runnable, e: ThreadPoolExecutor): Unit = ???
  }
}

class ThreadPoolExecutor(corePoolSize: Int,
                         maximumPoolSize: Int,
                         keepAliveTime: Long,
                         unit: TimeUnit,
                         workQueue: BlockingQueue[Runnable],
                         threadFactory: ThreadFactory,
                         handler: RejectedExecutionHandler)
    extends AbstractExecutorService {

  def this(corePoolSize: Int,
           maximumPoolSize: Int,
           keepAliveTime: Long,
           unit: TimeUnit,
           workQueue: BlockingQueue[Runnable]) =
    this(corePoolSize,
         maximumPoolSize,
         keepAliveTime,
         unit,
         workQueue,
         ???,
         ???)

  def this(corePoolSize: Int,
           maximumPoolSize: Int,
           keepAliveTime: Long,
           unit: TimeUnit,
           workQueue: BlockingQueue[Runnable],
           threadFactory: ThreadFactory) =
    this(corePoolSize,
         maximumPoolSize,
         keepAliveTime,
         unit,
         workQueue,
         threadFactory,
         ???)

  def this(corePoolSize: Int,
           maximumPoolSize: Int,
           keepAliveTime: Long,
           unit: TimeUnit,
           workQueue: BlockingQueue[Runnable],
           handler: RejectedExecutionHandler) =
    this(corePoolSize,
         maximumPoolSize,
         keepAliveTime,
         unit,
         workQueue,
         ???,
         handler)


  def allowCoreThreadTimeOut(value: Boolean): Unit = ???
  def allowsCoreThreadTimeOut(): Boolean = ???
  def awaitTermination(timeout: Long, unit: TimeUnit): Boolean = ???
  def execute(command: Runnable): Unit = ???
  def getActiveCount(): Int = ???
  def getCompletedTaskCount(): Long = ???
  def getCorePoolSize(): Int = ???
  def getKeepAliveTime(unit: TimeUnit): Long = ???
  def getLargestPoolSize(): Int = ???
  def getMaximumPoolSize(): Int = ???
  def getPoolSize(): Int = ???
  def getRejectedExecutionHandler(): RejectedExecutionHandler = ???
  def getTaskCount(): Long = ???
  def getThreadFactory(): ThreadFactory = ???
  def invokeAll[T](tasks: Collection[Callable[T]]): List[Future[T]] = ???
  def invokeAll[T](tasks: Collection[Callable[T]], timeout: Long, unit: TimeUnit): List[Future[T]] = ???
  def invokeAny[T](tasks: Collection[Callable[T]]): T = ???
  def invokeAny[T](tasks: Collection[Callable[T]], timeout: Long, unit: TimeUnit): T = ???
  def isShutdown(): Boolean = ???
  def isTerminated(): Boolean = ???
  def isTerminating(): Boolean = ???
  def prestartAllCoreThreads(): Int = ???
  def prestartCoreThread(): Boolean = ???
  def purge(): Unit = ???
  def remove(task: Runnable): Boolean = ???
  def setCorePoolSize(corePoolSize: Int): Unit = ???
  def setKeepAliveTime(time: Long, unit: TimeUnit): Unit = ???
  def setMaximumPoolSize(maximumPoolSize: Int): Unit = ???
  def setRejectedExecutionHandler(handler: RejectedExecutionHandler): Unit = ???
  def setThreadFactory(threadFactory: ThreadFactory): Unit = ???
  def shutdown(): Unit = ???
  def shutdownNow(): List[Runnable] = ???  
  def submit[T](task: Callable[T]): Future[T] = ???
  def submit[T](task: Runnable): Future[T] = ???
  def submit[T](task: Runnable,result: T): Future[T] = ???
  protected def afterExecute(r: Runnable, t: Throwable): Unit = ???
  protected def beforeExecute(t: Thread, r: Runnable): Unit = ???
  protected def newTaskFor[T](callable: Callable[T]): RunnableFuture[T] = ???
  protected def newTaskFor[T](runnable: Runnable, value: T): RunnableFuture[T] = ???
  protected def terminated(): Unit = ???
}