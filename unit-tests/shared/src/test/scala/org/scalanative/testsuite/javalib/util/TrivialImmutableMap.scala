// Ported from Scala.js commit: 9683b0c dated: 2021-10-22

package org.scalanative.testsuite.javalib.util

import java.util as ju
import java.util.Map.Entry

final class TrivialImmutableMap[K, V] private (contents: List[Entry[K, V]])
    extends ju.AbstractMap[K, V] {

  def entrySet(): ju.Set[Entry[K, V]] = {
    new ju.AbstractSet[Entry[K, V]] {
      def size(): Int = contents.size

      def iterator(): ju.Iterator[Entry[K, V]] = {
        new ju.Iterator[Entry[K, V]] {
          private var remaining: List[Entry[K, V]] = contents

          def hasNext(): Boolean = remaining.nonEmpty

          def next(): Entry[K, V] = {
            val head = remaining.head
            remaining = remaining.tail
            head
          }
        }
      }
    }
  }
}

object TrivialImmutableMap {
  def apply[K, V](contents: List[Entry[K, V]]): TrivialImmutableMap[K, V] =
    new TrivialImmutableMap(contents)

  def apply[K, V](contents: (K, V)*): TrivialImmutableMap[K, V] =
    apply(
      contents.toList.map(kv =>
        new ju.AbstractMap.SimpleImmutableEntry(kv._1, kv._2)
      )
    )
}
