/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */ /*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

abstract class RecursiveTask[V]() extends ForkJoinTask[V] {
  private[concurrent] var result: V = _

  protected def compute(): V
  override final def getRawResult(): V = result
  override final protected def setRawResult(value: V): Unit = result = value

  override final protected def exec(): Boolean = {
    result = compute()
    true
  }
}
