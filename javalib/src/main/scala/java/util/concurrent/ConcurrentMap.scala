// Ported from Scala.js commit: bbf0314 dated: Mon, 13 Jun 2022

package java.util.concurrent

import java.util._

trait ConcurrentMap[K, V] extends Map[K, V] {
  def putIfAbsent(key: K, value: V): V
  def remove(key: Any, value: Any): Boolean
  def replace(key: K, oldValue: V, newValue: V): Boolean
  def replace(key: K, value: V): V
}
