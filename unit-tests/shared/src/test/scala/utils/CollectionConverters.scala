package scala.scalanative.junit.utils

import java.util.{LinkedHashMap, LinkedHashSet, LinkedList}

import scala.collection.mutable

/** Set of helper method replacing problematic Scala collection.JavaConverters
 *  as they cause problems in cross compile between 2.13+ and older Scala
 *  versions
 */
object CollectionConverters {
  implicit class ScalaToJavaCollections[T](private val self: Iterable[T]) {
    def toJavaList: LinkedList[T] = {
      val list = new LinkedList[T]()
      self.foreach(list.add)
      list
    }

    def toJavaSet: java.util.Set[T] = {
      val s = new LinkedHashSet[T]()
      self.foreach(s.add)
      s
    }

    def toJavaMap[K, V](implicit ev: T =:= (K, V)): java.util.Map[K, V] = {
      val m = new LinkedHashMap[K, V]()
      self.iterator.foreach { elem =>
        val (key, value): (K, V) = elem: @unchecked
        m.put(key, value)
      }
      m
    }
  }

  implicit class JavaToScalaCollections[T](
      private val self: java.util.Collection[T]
  ) {
    def toScalaSeq: Seq[T] = self.iterator().toScalaSeq
    def toScalaSet: Set[T] = self.iterator().toScalaSet
    def toScalaMap[K, V](implicit
        ev: T =:= java.util.Map.Entry[K, V]
    ): Map[K, V] =
      self.iterator().toScalaMap[K, V]
  }

  implicit class JavaIteratorToScala[T](
      private val self: java.util.Iterator[T]
  ) {
    private def toBuilderResult[R](builder: mutable.Builder[T, R]): R = {
      while (self.hasNext())
        builder += self.next()
      builder.result()
    }
    private def toMapBuilderResult[R, K, V](
        builder: mutable.Builder[(K, V), R]
    )(implicit ev: T =:= java.util.Map.Entry[K, V]): R = {
      while (self.hasNext()) {
        val next: java.util.Map.Entry[K, V] = self.next()
        val pair: (K, V) = (next.getKey(), next.getValue())
        builder += pair
      }
      builder.result()
    }
    def toScalaSeq: Seq[T] = toBuilderResult(Seq.newBuilder)
    def toScalaSet: Set[T] = toBuilderResult(Set.newBuilder)
    def toScalaMap[K, V](implicit
        ev: T =:= java.util.Map.Entry[K, V]
    ): Map[K, V] =
      toMapBuilderResult(Map.newBuilder[K, V])
  }

}
