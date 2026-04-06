/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

@SerialVersionUID(-9202173006928992231L)
class CancellationException(message: String) extends IllegalStateException(message) {
  def this() = this(null)
}
