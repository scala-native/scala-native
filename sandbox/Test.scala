/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2002-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala
package runtime


import java.lang.reflect.{ Method => JMethod }
import java.lang.{ Class => JClass }

import scala.annotation.tailrec

private[scala] final class PolyMethodCache(
  private[this] val next: PolyMethodCache,
  private[this] val receiver: JClass[_],
  private[this] val method: JMethod,
  private[this] val complexity: Int
) {

  @tailrec private def findInternal(forReceiver: JClass[_]): JMethod =
    if (forReceiver eq receiver) method
    else next match {
      case x: PolyMethodCache => x findInternal forReceiver
      case _                  => next find forReceiver
    }

  def find(forReceiver: JClass[_]): JMethod = findInternal(forReceiver)

  def add(forReceiver: JClass[_], forMethod: JMethod): PolyMethodCache = ???
}
