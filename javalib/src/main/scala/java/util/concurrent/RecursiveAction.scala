/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;

abstract class RecursiveAction() extends ForkJoinTask[Void] {

  protected def compute(): Unit

  final def getRawResult(): Void = null

  protected final def setRawResult(mustBeNull: Void): Unit = ()

  protected final def exec(): Boolean = {
    compute()
    true
  }

}
