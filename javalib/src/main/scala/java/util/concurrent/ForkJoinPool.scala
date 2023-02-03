/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package java.util.concurrent

object ForkJoinPool {

  trait ManagedBlocker {
    @throws[InterruptedException]
    def block(): Boolean
    def isReleasable(): Boolean
  }
  def managedBlock(blocker: ManagedBlocker): Unit = () // TODO: ForkJoinPool
}
