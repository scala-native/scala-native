// Ported from JSR 166 revision 1.20

/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

import java.util.NavigableMap
import java.util.NavigableSet

trait ConcurrentNavigableMap[K, V]
    extends ConcurrentMap[K, V]
    with NavigableMap[K, V] {

  override def subMap(
      fromKey: K,
      fromInclusive: Boolean,
      toKey: K,
      toInclusive: Boolean
  ): ConcurrentNavigableMap[K, V]

  override def headMap(
      toKey: K,
      inclusive: Boolean
  ): ConcurrentNavigableMap[K, V]

  override def tailMap(
      fromKey: K,
      inclusive: Boolean
  ): ConcurrentNavigableMap[K, V]

  override def subMap(fromKey: K, toKey: K): ConcurrentNavigableMap[K, V]

  override def headMap(toKey: K): ConcurrentNavigableMap[K, V]

  override def tailMap(fromKey: K): ConcurrentNavigableMap[K, V]

  override def descendingMap(): ConcurrentNavigableMap[K, V]

  override def navigableKeySet(): NavigableSet[K]

  override def keySet(): NavigableSet[K]

  override def descendingKeySet(): NavigableSet[K]
}
