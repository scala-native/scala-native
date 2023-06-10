package java.util.concurrent

import java.util._

/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

/** A {@link ConcurrentMap} supporting {@link NavigableMap} operations, and
 *  recursively so for its navigable sub-maps.
 *
 *  <p>This interface is a member of the <a href="{@docRoot
 *  }/java.base/java/util/package-summary.html#CollectionsFramework"> Java
 *  Collections Framework</a>.
 *
 *  @author
 *    Doug Lea
 *  @param <K>
 *    the type of keys maintained by this map
 *  @param <V>
 *    the type of mapped values
 *  @since 1.6
 */
trait ConcurrentNavigableMap[K, V]
    extends ConcurrentMap[K, V]
    with NavigableMap[K, V] {

  /** @throws ClassCastException
   *    {@inheritDoc }
   *  @throws NullPointerException
   *    {@inheritDoc }
   *  @throws IllegalArgumentException
   *    {@inheritDoc }
   */
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

  /** Returns a reverse order view of the mappings contained in this map. The
   *  descending map is backed by this map, so changes to the map are reflected
   *  in the descending map, and vice-versa.
   *
   *  <p>The returned map has an ordering equivalent to {@link
   *  java.util.Collections# reverseOrder ( Comparator )
   *  Collections.reverseOrder} {@code (comparator())}. The expression {@code
   *  m.descendingMap().descendingMap()} returns a view of {@code m} essentially
   *  equivalent to {@code m}.
   *
   *  @return
   *    a reverse order view of this map
   */
  override def descendingMap(): ConcurrentNavigableMap[K, V]

  /** Returns a {@link NavigableSet} view of the keys contained in this map. The
   *  set's iterator returns the keys in ascending order. The set is backed by
   *  the map, so changes to the map are reflected in the set, and vice-versa.
   *  The set supports element removal, which removes the corresponding mapping
   *  from the map, via the {@code Iterator.remove}, {@code Set.remove}, {@code
   *  removeAll}, {@code retainAll}, and {@code clear} operations. It does not
   *  support the {@code add} or {@code addAll} operations.
   *
   *  <p>The view's iterators and spliterators are <a
   *  href="package-summary.html#Weakly"><i>weakly consistent</i></a>.
   *
   *  @return
   *    a navigable set view of the keys in this map
   */
  override def navigableKeySet(): NavigableSet[K]

  /** Returns a {@link NavigableSet} view of the keys contained in this map. The
   *  set's iterator returns the keys in ascending order. The set is backed by
   *  the map, so changes to the map are reflected in the set, and vice-versa.
   *  The set supports element removal, which removes the corresponding mapping
   *  from the map, via the {@code Iterator.remove}, {@code Set.remove}, {@code
   *  removeAll}, {@code retainAll}, and {@code clear} operations. It does not
   *  support the {@code add} or {@code addAll} operations.
   *
   *  <p>The view's iterators and spliterators are <a
   *  href="package-summary.html#Weakly"><i>weakly consistent</i></a>.
   *
   *  <p>This method is equivalent to method {@code navigableKeySet}.
   *
   *  @return
   *    a navigable set view of the keys in this map
   */
  override def keySet(): NavigableSet[K]

  /** Returns a reverse order {@link NavigableSet} view of the keys contained in
   *  this map. The set's iterator returns the keys in descending order. The set
   *  is backed by the map, so changes to the map are reflected in the set, and
   *  vice-versa. The set supports element removal, which removes the
   *  corresponding mapping from the map, via the {@code Iterator.remove},
   *  {@code Set.remove}, {@code removeAll}, {@code retainAll}, and {@code
   *  clear} operations. It does not support the {@code add} or {@code addAll}
   *  operations.
   *
   *  <p>The view's iterators and spliterators are <a
   *  href="package-summary.html#Weakly"><i>weakly consistent</i></a>.
   *
   *  @return
   *    a reverse order navigable set view of the keys in this map
   */
  override def descendingKeySet(): NavigableSet[K]
}
