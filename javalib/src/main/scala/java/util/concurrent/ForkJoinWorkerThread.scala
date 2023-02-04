/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;

import java.security.AccessController;
import java.security.PrivilegedAction;

class ForkJoinWorkerThread private[concurrent] (
    group: ThreadGroup,
    private[concurrent] val pool: ForkJoinPool,
    useSystemClassLoader: Boolean,
    isInnocuous: Boolean
) extends Thread(group, null, pool.nextWorkerThreadName(), 0L) {
  super.setDaemon(true)
  if (pool.ueh != null) {
    super.setUncaughtExceptionHandler(pool.ueh)
  }
  private[concurrent] val workQueue =
    new ForkJoinPool.WorkQueue(this, isInnocuous);

  private[concurrent] def this(group: ThreadGroup, pool: ForkJoinPool) = {
    this(group, pool, false, false);
  }

  protected def this(pool: ForkJoinPool) = {
    this(null, pool, false, false);
  }

  def getPool(): ForkJoinPool = {
    pool;
  }

  def getPoolIndex(): Int = {
    workQueue.getPoolIndex();
  }

  protected def onStart(): Unit = ()

  protected def onTermination(exception: Throwable): Unit = ()

  override def run(): Unit = {
    var exception: Throwable = null;
    val p = pool;
    val w = workQueue;
    if (p != null && w != null) { // skip on failed initialization
      try {
        p.registerWorker(w)
        onStart()
        p.runWorker(w)
      } catch {
        case ex: Throwable => exception = ex
      } finally {
        try onTermination(exception)
        catch {
          case ex: Throwable =>
            if (exception == null)
              exception = ex;
        } finally {
          p.deregisterWorker(this, exception);
        }
      }
    }
  }

}
