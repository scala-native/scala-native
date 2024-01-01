package org.scalanative.testsuite.javalib.util

import java.{lang => jl}

import java.util._

import org.junit._
import org.junit.Assert._
import org.scalanative.testsuite.utils.Platform.executingInJVM

trait MyService {
  def id: Int
}

// Loaded
class MyServiceImpl1 extends MyService {
  val id = 1
}
// Loaded
class MyServiceImpl2 extends MyServiceImpl1 {
  override val id = 2
}
// Not configured in config
class MyServiceImpl3 extends MyService {
  val id = 3
}
// Not defined in META-INF
class MyServiceImpl4 extends MyService {
  val id = 4
}

class ServiceLoaderTest {
  @Test def loadService(): Unit = {
    val loader = ServiceLoader.load(classOf[MyService])
    val idsFound = scala.collection.mutable.Set.empty[Int]
    loader.forEach { provider =>
      idsFound += provider.id
    }
    assertTrue("1", idsFound.contains(1))
    assertTrue("2", idsFound.contains(2))
    // Disabled in native config (to test opt-in behaviour)
    assertEquals(executingInJVM, idsFound.contains(3))
    assertFalse("4", idsFound.contains(4))
  }
}
