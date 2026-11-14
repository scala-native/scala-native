/* Ported from Scala.js commit: 8f98b8f dated: 2024-11-16
 *
 * Contains Scala Native specific updates subsequent to original port.
 * See Scala Native git repository history.
 */

package java.util

trait SortedMap[K, V] extends Map[K, V] with SequencedMap[K, V] {
  def firstKey(): K
  def comparator(): Comparator[_ >: K]
  def lastKey(): K
  def subMap(fromKey: K, toKey: K): SortedMap[K, V]
  def headMap(toKey: K): SortedMap[K, V]
  def tailMap(fromKey: K): SortedMap[K, V]

  // JEP 431 SortedMap methods. Since: Java 21

  override def putFirst(key: K, value: V): V =
    throw new UnsupportedOperationException()

  override def putLast(key: K, value: V): V =
    throw new UnsupportedOperationException()

  def reversed(): SortedMap[K, V] =
    throw new UnsupportedOperationException(
      "SortedMap: reversed() is not-yet-implemented"
    )
}
