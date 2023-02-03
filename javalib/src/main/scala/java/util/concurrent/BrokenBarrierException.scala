/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

@SerialVersionUID(7117394618823254244L)
class BrokenBarrierException(message: String) extends Exception(message) {
  def this() = this(null)
}
