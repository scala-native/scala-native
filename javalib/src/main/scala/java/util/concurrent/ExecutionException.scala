/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

@SerialVersionUID(783632799108934948L)
class ExecutionException(message: String, cause: Throwable) extends Exception(message, cause) {
  def this(cause: Throwable) = this(if (cause == null) null else cause.toString, cause)
}
