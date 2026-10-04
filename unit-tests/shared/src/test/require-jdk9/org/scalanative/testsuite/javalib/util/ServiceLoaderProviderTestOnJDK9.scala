package org.scalanative.testsuite.javalib.util

import java.util.ServiceLoader
import java.util.function.Supplier

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.Platform.executingInJVM

class ServiceLoaderProviderTestOnJDK9 {
  @Test def loadedProvidersAsSuppliers(): Unit = {
    val stream = ServiceLoader.load(classOf[MyService]).stream()
    try {
      val providers = stream.iterator()
      val ids = scala.collection.mutable.Set.empty[Int]
      while (providers.hasNext()) {
        val provider = providers.next()
        val supplier = (provider: AnyRef).asInstanceOf[Supplier[MyService]]
        val service = supplier.get()
        assertEquals(provider.`type`(), service.getClass())
        ids += service.id
      }
      assertTrue(ids.contains(1))
      assertTrue(ids.contains(2))
      assertEquals(executingInJVM, ids.contains(3))
      assertFalse(ids.contains(4))
    } finally stream.close()
  }

  @Test def customProviderAsNullableSupplier(): Unit = {
    val provider = new ServiceLoader.Provider[String] {
      def get(): String = null
      def `type`(): Class[String] = classOf[String]
    }
    val supplier = (provider: AnyRef).asInstanceOf[Supplier[String]]
    assertNull(supplier.get())
  }
}
