/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package java.util.concurrent

object ForkJoinPool {

  /** Interface for extending managed parallelism for tasks running in {@link s.
   *
   *  <p>A {@code ManagedBlocker} provides two methods. Method {@link
   *  #isReleasable} must return {@code true} if blocking is not necessary.
   *  Method {@link #block} blocks the current thread if necessary (perhaps
   *  internally invoking {@code isReleasable} before actually blocking). These
   *  actions are performed by any thread invoking {@link
   *  managedBlock(ManagedBlocker)}. The unusual methods in this API accommodate
   *  synchronizers that may, but don't usually, block for long periods.
   *  Similarly, they allow more efficient internal handling of cases in which
   *  additional workers may be, but usually are not, needed to ensure
   *  sufficient parallelism. Toward this end, implementations of method {@code
   *  isReleasable} must be amenable to repeated invocation. Neither method is
   *  invoked after a prior invocation of {@code isReleasable} or {@code block}
   *  returns {@code true}.
   *
   *  <p>For example, here is a ManagedBlocker based on a ReentrantLock: <pre>
   *  {@code class ManagedLocker implements ManagedBlocker { final ReentrantLock
   *  lock; boolean hasLock = false; ManagedLocker(ReentrantLock lock) {
   *  this.lock = lock; } public boolean block() { if (!hasLock) lock.lock();
   *  return true; } public boolean isReleasable() { return hasLock || (hasLock
   *  \= lock.tryLock()); } }}</pre>
   *
   *  <p>Here is a class that possibly blocks waiting for an item on a given
   *  queue: <pre> {@code class QueueTaker<E> implements ManagedBlocker { final
   *  BlockingQueue<E> queue; volatile E item = null;
   *  QueueTaker(BlockingQueue<E> q) { this.queue = q; } public boolean block()
   *  throws InterruptedException { if (item == null) item = queue.take();
   *  return true; } public boolean isReleasable() { return item != null ||
   *  (item = queue.poll()) != null; } public E getItem() { // call after
   *  pool.managedBlock completes return item; } }}</pre>
   */
  trait ManagedBlocker {

    /** Possibly blocks the current thread, for example waiting for a lock or
     *  condition.
     *
     *  @return
     *    {@code true} if no additional blocking is necessary (i.e., if
     *    isReleasable would return true)
     *  @throws InterruptedException
     *    if interrupted while waiting (the method is not required to do so, but
     *    is allowed to)
     */
    @throws[InterruptedException]
    def block(): Boolean

    /** Returns {@code true} if blocking is unnecessary.
     *  @return
     *    {@code true} if blocking is unnecessary
     */
    def isReleasable(): Boolean
  }

  /** Runs the given possibly blocking task. When {@linkplain ForkJoinTask#in)
   *  running in a , this method possibly arranges for a spare thread to be
   *  activated if necessary to ensure sufficient parallelism while the current
   *  thread is blocked in {@link ManagedBlocker#block blocker.block()}.
   *
   *  <p>This method repeatedly calls {@code blocker.isReleasable()} and {@code
   *  blocker.block()} until either method returns {@code true}. Every call to
   *  {@code blocker.block()} is preceded by a call to {@code
   *  blocker.isReleasable()} that returned {@code false}.
   *
   *  <p>If not running in a this method is behaviorally equivalent to <pre>
   *  {@code while (!blocker.isReleasable()) if (blocker.block()) break;}</pre>
   *
   *  If running in a the pool may first be expanded to ensure sufficient
   *  parallelism available during the call to {@code blocker.block()}.
   *
   *  @param blocker
   *    the blocker task
   *  @throws InterruptedException
   *    if {@code blocker.block()} did so
   */
  @throws[InterruptedException]
  def managedBlock(blocker: ManagedBlocker): Unit = () // TODO: ForkJoinPool

}
