package java.lang.ref

import java.lang.ref._

import org.junit.{Ignore, Test}
import org.junit.Assert._

import scala.scalanative.runtime.GC

class WeakReferenceTest {

  case class A(a: Int)
  var weakRef: WeakReference[A] = null
   
  @noinline def alloc(): Unit = {
    var a = A(0)
    weakRef = new WeakReference(a)
    assertEquals("get() should return object reference", weakRef.get(), A(0))
    a = null
  }

  @Test def referencesNullAfterGC(): Unit = {
    alloc()

    var i = 0
    while (i<3) {
      if(i == 0){
          GC.collect()
      }
      
      // We do not want to put the reference on stack
      // during GC, so we hide it behind an if block
      if (i == 3) {
        assertEquals(weakRef.get(), null)
      }
      i += 1
    }

  }

}