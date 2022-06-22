// Ported from Scala.js commit: 9dc4d5b dated: 11 Oct 2018

package java.util.concurrent

import java.util._

trait ThreadFactory {
  def newThread(r: Runnable): Thread
}