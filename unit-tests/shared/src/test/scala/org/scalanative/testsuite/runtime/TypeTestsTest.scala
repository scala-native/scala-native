package org.scalanative.testsuite.runtime

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

/* Type tests, casts and dispatch through class RTTI: classes deeper than the
 * lineage stored inline in RTTI, classes implementing enough traits for
 * itable probes to collide, and reflective checks through java.lang.Class.
 */
object TypeTestsTest {
  trait Base0
  trait Base1 extends Base0
  trait Base2
  trait Base3 extends Base1 with Base2
  trait NeverImplemented

  class D0
  class D1 extends D0 with Base1
  class D2 extends D1
  class D3 extends D2 with Base2
  class D4 extends D3
  class D5 extends D4 with Base3
  class D6 extends D5
  class D7 extends D6
  class D8 extends D7
  class D9 extends D8
  class D10 extends D9
  class D11 extends D10
  class D12 extends D11
  final class Sibling extends D4

  def chain: Array[D0] = Array(
    new D0,
    new D1,
    new D2,
    new D3,
    new D4,
    new D5,
    new D6,
    new D7,
    new D8,
    new D9,
    new D10,
    new D11,
    new D12
  )
  def chainClasses: Array[Class[_]] = Array(
    classOf[D0],
    classOf[D1],
    classOf[D2],
    classOf[D3],
    classOf[D4],
    classOf[D5],
    classOf[D6],
    classOf[D7],
    classOf[D8],
    classOf[D9],
    classOf[D10],
    classOf[D11],
    classOf[D12]
  )

  trait M0 { def m0: Int = 0 }
  trait M1 { def m1: Int = 1 }
  trait M2 { def m2: Int = 2 }
  trait M3 { def m3: Int = 3 }
  trait M4 { def m4: Int = 4 }
  trait M5 { def m5: Int = 5 }
  trait M6 { def m6: Int = 6 }
  trait M7 { def m7: Int = 7 }
  trait M8 { def m8: Int = 8 }
  trait M9 { def m9: Int = 9 }
  trait M10 { def m10: Int = 10 }
  trait M11 { def m11: Int = 11 }
  trait M12 { def m12: Int = 12 }
  trait M13 { def m13: Int = 13 }
  trait M14 { def m14: Int = 14 }
  trait M15 { def m15: Int = 15 }
  trait M16 { def m16: Int = 16 }
  trait M17 { def m17: Int = 17 }
  trait M18 { def m18: Int = 18 }
  trait M19 { def m19: Int = 19 }
  trait M20 { def m20: Int = 20 }
  trait M21 { def m21: Int = 21 }
  trait M22 { def m22: Int = 22 }
  trait M23 { def m23: Int = 23 }
  trait M24 { def m24: Int = 24 }
  trait M25 { def m25: Int = 25 }
  trait M26 { def m26: Int = 26 }
  trait M27 { def m27: Int = 27 }
  trait M28 { def m28: Int = 28 }
  trait M29 { def m29: Int = 29 }
  trait M30 { def m30: Int = 30 }
  trait M31 { def m31: Int = 31 }
  trait M32 { def m32: Int = 32 }
  trait M33 { def m33: Int = 33 }
  trait M34 { def m34: Int = 34 }
  trait M35 { def m35: Int = 35 }
  trait M36 { def m36: Int = 36 }
  trait M37 { def m37: Int = 37 }
  trait M38 { def m38: Int = 38 }
  trait M39 { def m39: Int = 39 }
  class Many
      extends M0
      with M1
      with M2
      with M3
      with M4
      with M5
      with M6
      with M7
      with M8
      with M9
      with M10
      with M11
      with M12
      with M13
      with M14
      with M15
      with M16
      with M17
      with M18
      with M19
      with M20
      with M21
      with M22
      with M23
      with M24
      with M25
      with M26
      with M27
      with M28
      with M29
      with M30
      with M31
      with M32
      with M33
      with M34
      with M35
      with M36
      with M37
      with M38
      with M39
  class ManyHalf
      extends M0
      with M2
      with M4
      with M6
      with M8
      with M10
      with M12
      with M14
      with M16
      with M18
      with M20
      with M22
      with M24
      with M26
      with M28
      with M30
      with M32
      with M34
      with M36
      with M38
}

class TypeTestsTest {
  import TypeTestsTest._

  @Test def classTestsAcrossDeepHierarchy(): Unit = {
    for ((obj, depth) <- chain.zipWithIndex) {
      assertTrue(obj.isInstanceOf[D0])
      assertEquals(s"D1 at $depth", depth >= 1, obj.isInstanceOf[D1])
      assertEquals(s"D4 at $depth", depth >= 4, obj.isInstanceOf[D4])
      assertEquals(s"D6 at $depth", depth >= 6, obj.isInstanceOf[D6])
      assertEquals(s"D7 at $depth", depth >= 7, obj.isInstanceOf[D7])
      assertEquals(s"D8 at $depth", depth >= 8, obj.isInstanceOf[D8])
      assertEquals(s"D9 at $depth", depth >= 9, obj.isInstanceOf[D9])
      assertEquals(s"D11 at $depth", depth >= 11, obj.isInstanceOf[D11])
      assertEquals(s"D12 at $depth", depth >= 12, obj.isInstanceOf[D12])
      assertFalse(obj.isInstanceOf[Sibling])
    }
    val sibling: D0 = new Sibling
    assertTrue(sibling.isInstanceOf[D4])
    assertFalse(sibling.isInstanceOf[D5])
    assertFalse(sibling.isInstanceOf[D9])
  }

  @Test def traitTestsAcrossDeepHierarchy(): Unit = {
    for ((obj, depth) <- chain.zipWithIndex) {
      assertEquals(s"Base0 at $depth", depth >= 1, obj.isInstanceOf[Base0])
      assertEquals(s"Base2 at $depth", depth >= 3, obj.isInstanceOf[Base2])
      assertEquals(s"Base3 at $depth", depth >= 5, obj.isInstanceOf[Base3])
      assertFalse(obj.isInstanceOf[NeverImplemented])
    }
  }

  @Test def castsAcrossDeepHierarchy(): Unit = {
    val objs = chain
    assertSame(objs(12), objs(12).asInstanceOf[D9])
    assertSame(objs(12), objs(12).asInstanceOf[Base3])
    assertThrows(classOf[ClassCastException], objs(8).asInstanceOf[D9])
    assertThrows(classOf[ClassCastException], objs(2).asInstanceOf[Base2])
  }

  @Test def reflectiveClassChecks(): Unit = {
    val classes = chainClasses
    for ((obj, depth) <- chain.zipWithIndex;
        (cls, clsDepth) <- classes.zipWithIndex) {
      assertEquals(
        s"${cls.getName}.isInstance(D$depth)",
        depth >= clsDepth,
        cls.isInstance(obj)
      )
      assertEquals(
        s"${cls.getName}.isAssignableFrom(D$depth)",
        depth >= clsDepth,
        cls.isAssignableFrom(obj.getClass)
      )
    }
    assertTrue(classOf[Base0].isAssignableFrom(classOf[Base3]))
    assertFalse(classOf[Base3].isAssignableFrom(classOf[Base0]))
    assertTrue(classOf[Base3].isAssignableFrom(classOf[D12]))
    assertFalse(classOf[NeverImplemented].isAssignableFrom(classOf[D12]))
    assertTrue(classOf[Base0].isInterface())
    assertFalse(classOf[D12].isInterface())
  }

  @Test def forNameFindsClassesAndTraits(): Unit = {
    assertSame(classOf[D12], Class.forName(classOf[D12].getName))
    assertSame(classOf[Base3], Class.forName(classOf[Base3].getName))
    // java.lang.Class appears in its own lineage, which must not be
    // mistaken for the start of another class
    assertSame(classOf[Class[_]], Class.forName("java.lang.Class"))
  }

  @Test def manyTraits(): Unit = {
    val many: AnyRef = new Many
    val half: AnyRef = new ManyHalf

    assertTrue(many.isInstanceOf[M0])
    assertEquals(true, half.isInstanceOf[M0])
    assertEquals(true, classOf[M0].isInstance(half))
    assertTrue(many.isInstanceOf[M1])
    assertEquals(false, half.isInstanceOf[M1])
    assertEquals(false, classOf[M1].isInstance(half))
    assertTrue(many.isInstanceOf[M2])
    assertEquals(true, half.isInstanceOf[M2])
    assertEquals(true, classOf[M2].isInstance(half))
    assertTrue(many.isInstanceOf[M3])
    assertEquals(false, half.isInstanceOf[M3])
    assertEquals(false, classOf[M3].isInstance(half))
    assertTrue(many.isInstanceOf[M4])
    assertEquals(true, half.isInstanceOf[M4])
    assertEquals(true, classOf[M4].isInstance(half))
    assertTrue(many.isInstanceOf[M5])
    assertEquals(false, half.isInstanceOf[M5])
    assertEquals(false, classOf[M5].isInstance(half))
    assertTrue(many.isInstanceOf[M6])
    assertEquals(true, half.isInstanceOf[M6])
    assertEquals(true, classOf[M6].isInstance(half))
    assertTrue(many.isInstanceOf[M7])
    assertEquals(false, half.isInstanceOf[M7])
    assertEquals(false, classOf[M7].isInstance(half))
    assertTrue(many.isInstanceOf[M8])
    assertEquals(true, half.isInstanceOf[M8])
    assertEquals(true, classOf[M8].isInstance(half))
    assertTrue(many.isInstanceOf[M9])
    assertEquals(false, half.isInstanceOf[M9])
    assertEquals(false, classOf[M9].isInstance(half))
    assertTrue(many.isInstanceOf[M10])
    assertEquals(true, half.isInstanceOf[M10])
    assertEquals(true, classOf[M10].isInstance(half))
    assertTrue(many.isInstanceOf[M11])
    assertEquals(false, half.isInstanceOf[M11])
    assertEquals(false, classOf[M11].isInstance(half))
    assertTrue(many.isInstanceOf[M12])
    assertEquals(true, half.isInstanceOf[M12])
    assertEquals(true, classOf[M12].isInstance(half))
    assertTrue(many.isInstanceOf[M13])
    assertEquals(false, half.isInstanceOf[M13])
    assertEquals(false, classOf[M13].isInstance(half))
    assertTrue(many.isInstanceOf[M14])
    assertEquals(true, half.isInstanceOf[M14])
    assertEquals(true, classOf[M14].isInstance(half))
    assertTrue(many.isInstanceOf[M15])
    assertEquals(false, half.isInstanceOf[M15])
    assertEquals(false, classOf[M15].isInstance(half))
    assertTrue(many.isInstanceOf[M16])
    assertEquals(true, half.isInstanceOf[M16])
    assertEquals(true, classOf[M16].isInstance(half))
    assertTrue(many.isInstanceOf[M17])
    assertEquals(false, half.isInstanceOf[M17])
    assertEquals(false, classOf[M17].isInstance(half))
    assertTrue(many.isInstanceOf[M18])
    assertEquals(true, half.isInstanceOf[M18])
    assertEquals(true, classOf[M18].isInstance(half))
    assertTrue(many.isInstanceOf[M19])
    assertEquals(false, half.isInstanceOf[M19])
    assertEquals(false, classOf[M19].isInstance(half))
    assertTrue(many.isInstanceOf[M20])
    assertEquals(true, half.isInstanceOf[M20])
    assertEquals(true, classOf[M20].isInstance(half))
    assertTrue(many.isInstanceOf[M21])
    assertEquals(false, half.isInstanceOf[M21])
    assertEquals(false, classOf[M21].isInstance(half))
    assertTrue(many.isInstanceOf[M22])
    assertEquals(true, half.isInstanceOf[M22])
    assertEquals(true, classOf[M22].isInstance(half))
    assertTrue(many.isInstanceOf[M23])
    assertEquals(false, half.isInstanceOf[M23])
    assertEquals(false, classOf[M23].isInstance(half))
    assertTrue(many.isInstanceOf[M24])
    assertEquals(true, half.isInstanceOf[M24])
    assertEquals(true, classOf[M24].isInstance(half))
    assertTrue(many.isInstanceOf[M25])
    assertEquals(false, half.isInstanceOf[M25])
    assertEquals(false, classOf[M25].isInstance(half))
    assertTrue(many.isInstanceOf[M26])
    assertEquals(true, half.isInstanceOf[M26])
    assertEquals(true, classOf[M26].isInstance(half))
    assertTrue(many.isInstanceOf[M27])
    assertEquals(false, half.isInstanceOf[M27])
    assertEquals(false, classOf[M27].isInstance(half))
    assertTrue(many.isInstanceOf[M28])
    assertEquals(true, half.isInstanceOf[M28])
    assertEquals(true, classOf[M28].isInstance(half))
    assertTrue(many.isInstanceOf[M29])
    assertEquals(false, half.isInstanceOf[M29])
    assertEquals(false, classOf[M29].isInstance(half))
    assertTrue(many.isInstanceOf[M30])
    assertEquals(true, half.isInstanceOf[M30])
    assertEquals(true, classOf[M30].isInstance(half))
    assertTrue(many.isInstanceOf[M31])
    assertEquals(false, half.isInstanceOf[M31])
    assertEquals(false, classOf[M31].isInstance(half))
    assertTrue(many.isInstanceOf[M32])
    assertEquals(true, half.isInstanceOf[M32])
    assertEquals(true, classOf[M32].isInstance(half))
    assertTrue(many.isInstanceOf[M33])
    assertEquals(false, half.isInstanceOf[M33])
    assertEquals(false, classOf[M33].isInstance(half))
    assertTrue(many.isInstanceOf[M34])
    assertEquals(true, half.isInstanceOf[M34])
    assertEquals(true, classOf[M34].isInstance(half))
    assertTrue(many.isInstanceOf[M35])
    assertEquals(false, half.isInstanceOf[M35])
    assertEquals(false, classOf[M35].isInstance(half))
    assertTrue(many.isInstanceOf[M36])
    assertEquals(true, half.isInstanceOf[M36])
    assertEquals(true, classOf[M36].isInstance(half))
    assertTrue(many.isInstanceOf[M37])
    assertEquals(false, half.isInstanceOf[M37])
    assertEquals(false, classOf[M37].isInstance(half))
    assertTrue(many.isInstanceOf[M38])
    assertEquals(true, half.isInstanceOf[M38])
    assertEquals(true, classOf[M38].isInstance(half))
    assertTrue(many.isInstanceOf[M39])
    assertEquals(false, half.isInstanceOf[M39])
    assertEquals(false, classOf[M39].isInstance(half))
    assertFalse(many.isInstanceOf[NeverImplemented])
    val m = new Many
    assertEquals(
      780,
      (m: M0).m0 + (m: M1).m1 + (m: M2).m2 + (m: M3).m3 + (m: M4).m4 + (m: M5).m5 + (m: M6).m6 + (m: M7).m7 + (m: M8).m8 + (m: M9).m9 + (m: M10).m10 + (m: M11).m11 + (m: M12).m12 + (m: M13).m13 + (m: M14).m14 + (m: M15).m15 + (m: M16).m16 + (m: M17).m17 + (m: M18).m18 + (m: M19).m19 + (m: M20).m20 + (m: M21).m21 + (m: M22).m22 + (m: M23).m23 + (m: M24).m24 + (m: M25).m25 + (m: M26).m26 + (m: M27).m27 + (m: M28).m28 + (m: M29).m29 + (m: M30).m30 + (m: M31).m31 + (m: M32).m32 + (m: M33).m33 + (m: M34).m34 + (m: M35).m35 + (m: M36).m36 + (m: M37).m37 + (m: M38).m38 + (m: M39).m39
    )
    val h = new ManyHalf
    assertEquals(
      380,
      (h: M0).m0 + (h: M2).m2 + (h: M4).m4 + (h: M6).m6 + (h: M8).m8 + (h: M10).m10 + (h: M12).m12 + (h: M14).m14 + (h: M16).m16 + (h: M18).m18 + (h: M20).m20 + (h: M22).m22 + (h: M24).m24 + (h: M26).m26 + (h: M28).m28 + (h: M30).m30 + (h: M32).m32 + (h: M34).m34 + (h: M36).m36 + (h: M38).m38
    )
  }
}
