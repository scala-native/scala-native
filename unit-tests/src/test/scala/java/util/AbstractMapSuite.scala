package java.util

// Ported from Scala.js

import java.{util => ju}
import scala.reflect.ClassTag

abstract class AbstractMapSuite extends MapSuite {
  def factory(): AbstractMapSuiteFactory
}

abstract class AbstractMapSuiteFactory extends MapFactory {
  def implementationName: String

  def empty[K: ClassTag, V: ClassTag]: ju.AbstractMap[K, V]
}
