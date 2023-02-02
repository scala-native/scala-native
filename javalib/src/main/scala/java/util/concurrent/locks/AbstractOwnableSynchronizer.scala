/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent
package locks

import java.util.concurrent.atomic.AtomicReference

/** A synchronizer that may be exclusively owned by a thread. This class
 *  provides a basis for creating locks and related synchronizers that may
 *  entail a notion of ownership. The {@code AbstractOwnableSynchronizer} class
 *  itself does not manage or use this information. However, subclasses and
 *  tools may use appropriately maintained values to help control and monitor
 *  access and provide diagnostics.
 *
 *  @since 1.6
 *  @author
 *    Doug Lea
 */
abstract class AbstractOwnableSynchronizer protected ()
    extends java.io.Serializable {

  /** The current owner of exclusive mode synchronization.
   */
  private var exclusiveOwnerThread: Thread = _

  /** Sets the thread that currently owns exclusive access. A {@code null}
   *  argument indicates that no thread owns access. This method does not
   *  otherwise impose any synchronization or {@code volatile} field accesses.
   *  @param thread
   *    the owner thread
   */
  protected final def setExclusiveOwnerThread(t: Thread): Unit =
    exclusiveOwnerThread = t

  /** Returns the thread last set by {@code setExclusiveOwnerThread}, or {@code
   *  null} if never set. This method does not otherwise impose any
   *  synchronization or {@code volatile} field accesses.
   *  @return
   *    the owner thread
   */
  protected final def getExclusiveOwnerThread(): Thread =
    exclusiveOwnerThread
}
