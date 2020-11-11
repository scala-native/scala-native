package scala.scalanative.junit.utils

import java.util.{LinkedHashMap, LinkedHashSet, LinkedList}
import scala.collection.mutable
import scala.reflect.ClassTag

/** Set of helper method replacing problematic Scala collection.JavaConverters as they cause problems
 * in cross compile between 2.13+ and older Scala versions */
object CollectionConverters {
  implicit class ScalaToJavaCollections[T: ClassTag](
      private val self: Iterable[T]) {
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
      self.iterator.asInstanceOf[Iterator[(K, V)]].foreach {
        case (k, v) => m.put(k, v)
      }
      m
    }
  }

  implicit class JavaToScalaCollections[T: ClassTag](
      private val self: java.util.Collection[T]) {
    private def buf        = self.iterator().toScalaSeq
    def toScalaSeq: Seq[T] = buf.toSeq
    def toScalaMap[K: ClassTag, V: ClassTag](
        implicit ev: T =:= java.util.Map.Entry[K, V]): mutable.Map[K, V] = {
      val map = mutable.Map.empty[K, V]
      self
        .iterator()
        .asInstanceOf[Iterator[java.util.Map.Entry[K, V]]]
        .foreach { v => map.put(v.getKey(), v.getValue()) }
      map
    }
    def toScalaSet: Set[T] = self.iterator().toScalaSet
  }

  implicit class JavaIteratorToScala[T: ClassTag](
      private val self: java.util.Iterator[T]) {
    val toScalaSeq: mutable.UnrolledBuffer[T] = {
      val b = new mutable.UnrolledBuffer[T]()
      while (self.hasNext) {
        b += self.next()
      }
      b
    }
    def toScalaSet: Set[T] = toScalaSeq.toSet
  }

}
