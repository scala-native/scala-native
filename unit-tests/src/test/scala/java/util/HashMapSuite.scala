package java.util

// Ported from Scala.js

import java.{util => ju}
import scala.reflect.ClassTag

object HashMapSuite extends MapSuite {
  def factory(): HashMapSuiteFactory = new HashMapSuiteFactory
}

object HashMapSuiteFactory {
  def allFactories: scala.Iterator[MapFactory] =
    scala.Iterator(new HashMapSuiteFactory) ++ LinkedHashMapSuiteFactory.allFactories
}

class HashMapSuiteFactory extends AbstractMapSuiteFactory {
  override def implementationName: String =
    "java.util.HashMap"

  override def empty[K: ClassTag, V: ClassTag]: ju.HashMap[K, V] =
    new ju.HashMap[K, V]

  def allowsNullKeys: Boolean   = true
  def allowsNullValues: Boolean = true
}
