// Ported from Scala.js commit: 5e9956b dated: 2024-11-18
// Significantly modified for Scala Native Issue 4209

package java.lang

import java.util.Optional
import java.util.concurrent.{ConcurrentHashMap, ConcurrentMap}

abstract class ClassValue[T <: AnyRef] protected () {
  private val cvMap: ConcurrentMap[Class[_], Optional[T]] =
    new ConcurrentHashMap[Class[_], Optional[T]](64) // a guess > default 16

  protected def computeValue(`type`: Class[_]): T

  def get(`type`: Class[_]): T =
    cvMap
      .computeIfAbsent(`type`, (tpe) => Optional.ofNullable(computeValue(tpe)))
      .orElse(null.asInstanceOf[T])

  def remove(`type`: Class[_]): Unit =
    cvMap.remove(`type`)
}

/* This companion object and, especially, its inner class are not documented
 * in the JDK API for ClassValue. Rather they are an implementation detail.
 *
 * The Scala compiler used by Scala Native seems to depend on that detail.
 * It tries and fails to resolve the inner class ClassValueMap.
 * See Issue 4209 for a sample error log and a fuller discussion.
 */

object ClassValue {
  class ClassValueMap {}
}
