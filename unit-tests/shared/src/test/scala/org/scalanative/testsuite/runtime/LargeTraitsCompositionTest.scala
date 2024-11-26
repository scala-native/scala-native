// format: off
package org.scalanative.testsuite.runtime

import org.junit.Test
import org.junit.Assert._

class LargeTraitsCompositionTest {
  @Test def testDispatch(): Unit = {
    Impl0.test()
    Impl1.test()
  }
}

// This test is designed to test correctness of Trait dispatch and identity checks when given class is too large to implement quick itables lookup
// We define 64 traits (fast itable limit is 32) with at least 2 implementations to prevent static method invocation
trait Trait0 { def invoke0(): Int }
trait Trait1 { def invoke1(): Int }
trait Trait2 { def invoke2(): Int }
trait Trait3 { def invoke3(): Int }
trait Trait4 { def invoke4(): Int }
trait Trait5 { def invoke5(): Int }
trait Trait6 { def invoke6(): Int }
trait Trait7 { def invoke7(): Int }
trait Trait8 { def invoke8(): Int }
trait Trait9 { def invoke9(): Int }
trait Trait10 { def invoke10(): Int }
trait Trait11 { def invoke11(): Int }
trait Trait12 { def invoke12(): Int }
trait Trait13 { def invoke13(): Int }
trait Trait14 { def invoke14(): Int }
trait Trait15 { def invoke15(): Int }
trait Trait16 { def invoke16(): Int }
trait Trait17 { def invoke17(): Int }
trait Trait18 { def invoke18(): Int }
trait Trait19 { def invoke19(): Int }
trait Trait20 { def invoke20(): Int }
trait Trait21 { def invoke21(): Int }
trait Trait22 { def invoke22(): Int }
trait Trait23 { def invoke23(): Int }
trait Trait24 { def invoke24(): Int }
trait Trait25 { def invoke25(): Int }
trait Trait26 { def invoke26(): Int }
trait Trait27 { def invoke27(): Int }
trait Trait28 { def invoke28(): Int }
trait Trait29 { def invoke29(): Int }
trait Trait30 { def invoke30(): Int }
trait Trait31 { def invoke31(): Int }
trait Trait32 { def invoke32(): Int }
trait Trait33 { def invoke33(): Int }
trait Trait34 { def invoke34(): Int }
trait Trait35 { def invoke35(): Int }
trait Trait36 { def invoke36(): Int }
trait Trait37 { def invoke37(): Int }
trait Trait38 { def invoke38(): Int }
trait Trait39 { def invoke39(): Int }
trait Trait40 { def invoke40(): Int }
trait Trait41 { def invoke41(): Int }
trait Trait42 { def invoke42(): Int }
trait Trait43 { def invoke43(): Int }
trait Trait44 { def invoke44(): Int }
trait Trait45 { def invoke45(): Int }
trait Trait46 { def invoke46(): Int }
trait Trait47 { def invoke47(): Int }
trait Trait48 { def invoke48(): Int }
trait Trait49 { def invoke49(): Int }
trait Trait50 { def invoke50(): Int }
trait Trait51 { def invoke51(): Int }
trait Trait52 { def invoke52(): Int }
trait Trait53 { def invoke53(): Int }
trait Trait54 { def invoke54(): Int }
trait Trait55 { def invoke55(): Int }
trait Trait56 { def invoke56(): Int }
trait Trait57 { def invoke57(): Int }
trait Trait58 { def invoke58(): Int }
trait Trait59 { def invoke59(): Int }
trait Trait60 { def invoke60(): Int }
trait Trait61 { def invoke61(): Int }
trait Trait62 { def invoke62(): Int }
trait Trait63 { def invoke63(): Int }
class Impl0 extends Trait0 with Trait1 with Trait2 with Trait3 with Trait4 with Trait5 with Trait6 with Trait7 with Trait8 with Trait9 with Trait10 with Trait11 with Trait12 with Trait13 with Trait14 with Trait15 with Trait16 with Trait17 with Trait18 with Trait19 with Trait20 with Trait21 with Trait22 with Trait23 with Trait24 with Trait25 with Trait26 with Trait27 with Trait28 with Trait29 with Trait30 with Trait31 with Trait32 with Trait33 with Trait34 with Trait35 with Trait36 with Trait37 with Trait38 with Trait39 with Trait40 with Trait41 with Trait42 with Trait43 with Trait44 with Trait45 with Trait46 with Trait47 with Trait48 with Trait49 with Trait50 with Trait51 with Trait52 with Trait53 with Trait54 with Trait55 with Trait56 with Trait57 with Trait58 with Trait59 with Trait60 with Trait61 with Trait62 with Trait63 {
  override def invoke0(): Int = 0
  override def invoke1(): Int = 1
  override def invoke2(): Int = 2
  override def invoke3(): Int = 3
  override def invoke4(): Int = 4
  override def invoke5(): Int = 5
  override def invoke6(): Int = 6
  override def invoke7(): Int = 7
  override def invoke8(): Int = 8
  override def invoke9(): Int = 9
  override def invoke10(): Int = 10
  override def invoke11(): Int = 11
  override def invoke12(): Int = 12
  override def invoke13(): Int = 13
  override def invoke14(): Int = 14
  override def invoke15(): Int = 15
  override def invoke16(): Int = 16
  override def invoke17(): Int = 17
  override def invoke18(): Int = 18
  override def invoke19(): Int = 19
  override def invoke20(): Int = 20
  override def invoke21(): Int = 21
  override def invoke22(): Int = 22
  override def invoke23(): Int = 23
  override def invoke24(): Int = 24
  override def invoke25(): Int = 25
  override def invoke26(): Int = 26
  override def invoke27(): Int = 27
  override def invoke28(): Int = 28
  override def invoke29(): Int = 29
  override def invoke30(): Int = 30
  override def invoke31(): Int = 31
  override def invoke32(): Int = 32
  override def invoke33(): Int = 33
  override def invoke34(): Int = 34
  override def invoke35(): Int = 35
  override def invoke36(): Int = 36
  override def invoke37(): Int = 37
  override def invoke38(): Int = 38
  override def invoke39(): Int = 39
  override def invoke40(): Int = 40
  override def invoke41(): Int = 41
  override def invoke42(): Int = 42
  override def invoke43(): Int = 43
  override def invoke44(): Int = 44
  override def invoke45(): Int = 45
  override def invoke46(): Int = 46
  override def invoke47(): Int = 47
  override def invoke48(): Int = 48
  override def invoke49(): Int = 49
  override def invoke50(): Int = 50
  override def invoke51(): Int = 51
  override def invoke52(): Int = 52
  override def invoke53(): Int = 53
  override def invoke54(): Int = 54
  override def invoke55(): Int = 55
  override def invoke56(): Int = 56
  override def invoke57(): Int = 57
  override def invoke58(): Int = 58
  override def invoke59(): Int = 59
  override def invoke60(): Int = 60
  override def invoke61(): Int = 61
  override def invoke62(): Int = 62
  override def invoke63(): Int = 63
}
object Impl0 {
  def test(): Unit = {
    val instance = new Impl0()
    val anyInstance: Any = instance
    val cls = anyInstance.getClass()
    
    assertTrue("isInstanceOf[Trait0]", anyInstance.isInstanceOf[Trait0])
    assertEquals("invoke[Trait0]", 0, anyInstance.asInstanceOf[Trait0].invoke0())
    assertTrue("isAssignableFrom[Trait0]", classOf[Trait0].isAssignableFrom(cls))
    assertTrue("Trait0 isInstance", classOf[Trait0].isInstance(anyInstance))
    assertTrue("contains interface of Trait0", cls.getInterfaces().contains(classOf[Trait0]))

    assertTrue("isInstanceOf[Trait1]", anyInstance.isInstanceOf[Trait1])
    assertEquals("invoke[Trait1]", 1, anyInstance.asInstanceOf[Trait1].invoke1())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait1].isAssignableFrom(cls))
    assertTrue("Trait1 isInstance", classOf[Trait1].isInstance(anyInstance))
    assertTrue("contains interface of Trait1", cls.getInterfaces().contains(classOf[Trait1]))

    assertTrue("isInstanceOf[Trait2]", anyInstance.isInstanceOf[Trait2])
    assertEquals("invoke[Trait2]", 2, anyInstance.asInstanceOf[Trait2].invoke2())
    assertTrue("isAssignableFrom[Trait2]", classOf[Trait2].isAssignableFrom(cls))
    assertTrue("Trait2 isInstance", classOf[Trait2].isInstance(anyInstance))
    assertTrue("contains interface of Trait2", cls.getInterfaces().contains(classOf[Trait2]))

    assertTrue("isInstanceOf[Trait3]", anyInstance.isInstanceOf[Trait3])
    assertEquals("invoke[Trait3]", 3, anyInstance.asInstanceOf[Trait3].invoke3())
    assertTrue("isAssignableFrom[Trait3]", classOf[Trait3].isAssignableFrom(cls))
    assertTrue("Trait3 isInstance", classOf[Trait3].isInstance(anyInstance))
    assertTrue("contains interface of Trait3", cls.getInterfaces().contains(classOf[Trait3]))

    assertTrue("isInstanceOf[Trait4]", anyInstance.isInstanceOf[Trait4])
    assertEquals("invoke[Trait4]", 4, anyInstance.asInstanceOf[Trait4].invoke4())
    assertTrue("isAssignableFrom[Trait4]", classOf[Trait4].isAssignableFrom(cls))
    assertTrue("Trait4 isInstance", classOf[Trait4].isInstance(anyInstance))
    assertTrue("contains interface of Trait4", cls.getInterfaces().contains(classOf[Trait4]))

    assertTrue("isInstanceOf[Trait5]", anyInstance.isInstanceOf[Trait5])
    assertEquals("invoke[Trait5]", 5, anyInstance.asInstanceOf[Trait5].invoke5())
    assertTrue("isAssignableFrom[Trait5]", classOf[Trait5].isAssignableFrom(cls))
    assertTrue("Trait5 isInstance", classOf[Trait5].isInstance(anyInstance))
    assertTrue("contains interface of Trait5", cls.getInterfaces().contains(classOf[Trait5]))

    assertTrue("isInstanceOf[Trait6]", anyInstance.isInstanceOf[Trait6])
    assertEquals("invoke[Trait6]", 6, anyInstance.asInstanceOf[Trait6].invoke6())
    assertTrue("isAssignableFrom[Trait6]", classOf[Trait6].isAssignableFrom(cls))
    assertTrue("Trait6 isInstance", classOf[Trait6].isInstance(anyInstance))
    assertTrue("contains interface of Trait6", cls.getInterfaces().contains(classOf[Trait6]))

    assertTrue("isInstanceOf[Trait7]", anyInstance.isInstanceOf[Trait7])
    assertEquals("invoke[Trait7]", 7, anyInstance.asInstanceOf[Trait7].invoke7())
    assertTrue("isAssignableFrom[Trait7]", classOf[Trait7].isAssignableFrom(cls))
    assertTrue("Trait7 isInstance", classOf[Trait7].isInstance(anyInstance))
    assertTrue("contains interface of Trait7", cls.getInterfaces().contains(classOf[Trait7]))

    assertTrue("isInstanceOf[Trait8]", anyInstance.isInstanceOf[Trait8])
    assertEquals("invoke[Trait8]", 8, anyInstance.asInstanceOf[Trait8].invoke8())
    assertTrue("isAssignableFrom[Trait8]", classOf[Trait8].isAssignableFrom(cls))
    assertTrue("Trait8 isInstance", classOf[Trait8].isInstance(anyInstance))
    assertTrue("contains interface of Trait8", cls.getInterfaces().contains(classOf[Trait8]))

    assertTrue("isInstanceOf[Trait9]", anyInstance.isInstanceOf[Trait9])
    assertEquals("invoke[Trait9]", 9, anyInstance.asInstanceOf[Trait9].invoke9())
    assertTrue("isAssignableFrom[Trait9]", classOf[Trait9].isAssignableFrom(cls))
    assertTrue("Trait9 isInstance", classOf[Trait9].isInstance(anyInstance))
    assertTrue("contains interface of Trait9", cls.getInterfaces().contains(classOf[Trait9]))

    assertTrue("isInstanceOf[Trait10]", anyInstance.isInstanceOf[Trait10])
    assertEquals("invoke[Trait10]", 10, anyInstance.asInstanceOf[Trait10].invoke10())
    assertTrue("isAssignableFrom[Trait10]", classOf[Trait10].isAssignableFrom(cls))
    assertTrue("Trait10 isInstance", classOf[Trait10].isInstance(anyInstance))
    assertTrue("contains interface of Trait10", cls.getInterfaces().contains(classOf[Trait10]))

    assertTrue("isInstanceOf[Trait11]", anyInstance.isInstanceOf[Trait11])
    assertEquals("invoke[Trait11]", 11, anyInstance.asInstanceOf[Trait11].invoke11())
    assertTrue("isAssignableFrom[Trait11]", classOf[Trait11].isAssignableFrom(cls))
    assertTrue("Trait11 isInstance", classOf[Trait11].isInstance(anyInstance))
    assertTrue("contains interface of Trait11", cls.getInterfaces().contains(classOf[Trait11]))

    assertTrue("isInstanceOf[Trait12]", anyInstance.isInstanceOf[Trait12])
    assertEquals("invoke[Trait12]", 12, anyInstance.asInstanceOf[Trait12].invoke12())
    assertTrue("isAssignableFrom[Trait12]", classOf[Trait12].isAssignableFrom(cls))
    assertTrue("Trait12 isInstance", classOf[Trait12].isInstance(anyInstance))
    assertTrue("contains interface of Trait12", cls.getInterfaces().contains(classOf[Trait12]))

    assertTrue("isInstanceOf[Trait13]", anyInstance.isInstanceOf[Trait13])
    assertEquals("invoke[Trait13]", 13, anyInstance.asInstanceOf[Trait13].invoke13())
    assertTrue("isAssignableFrom[Trait13]", classOf[Trait13].isAssignableFrom(cls))
    assertTrue("Trait13 isInstance", classOf[Trait13].isInstance(anyInstance))
    assertTrue("contains interface of Trait13", cls.getInterfaces().contains(classOf[Trait13]))

    assertTrue("isInstanceOf[Trait14]", anyInstance.isInstanceOf[Trait14])
    assertEquals("invoke[Trait14]", 14, anyInstance.asInstanceOf[Trait14].invoke14())
    assertTrue("isAssignableFrom[Trait14]", classOf[Trait14].isAssignableFrom(cls))
    assertTrue("Trait14 isInstance", classOf[Trait14].isInstance(anyInstance))
    assertTrue("contains interface of Trait14", cls.getInterfaces().contains(classOf[Trait14]))

    assertTrue("isInstanceOf[Trait15]", anyInstance.isInstanceOf[Trait15])
    assertEquals("invoke[Trait15]", 15, anyInstance.asInstanceOf[Trait15].invoke15())
    assertTrue("isAssignableFrom[Trait15]", classOf[Trait15].isAssignableFrom(cls))
    assertTrue("Trait15 isInstance", classOf[Trait15].isInstance(anyInstance))
    assertTrue("contains interface of Trait15", cls.getInterfaces().contains(classOf[Trait15]))

    assertTrue("isInstanceOf[Trait16]", anyInstance.isInstanceOf[Trait16])
    assertEquals("invoke[Trait16]", 16, anyInstance.asInstanceOf[Trait16].invoke16())
    assertTrue("isAssignableFrom[Trait16]", classOf[Trait16].isAssignableFrom(cls))
    assertTrue("Trait16 isInstance", classOf[Trait16].isInstance(anyInstance))
    assertTrue("contains interface of Trait16", cls.getInterfaces().contains(classOf[Trait16]))

    assertTrue("isInstanceOf[Trait17]", anyInstance.isInstanceOf[Trait17])
    assertEquals("invoke[Trait17]", 17, anyInstance.asInstanceOf[Trait17].invoke17())
    assertTrue("isAssignableFrom[Trait17]", classOf[Trait17].isAssignableFrom(cls))
    assertTrue("Trait17 isInstance", classOf[Trait17].isInstance(anyInstance))
    assertTrue("contains interface of Trait17", cls.getInterfaces().contains(classOf[Trait17]))

    assertTrue("isInstanceOf[Trait18]", anyInstance.isInstanceOf[Trait18])
    assertEquals("invoke[Trait18]", 18, anyInstance.asInstanceOf[Trait18].invoke18())
    assertTrue("isAssignableFrom[Trait18]", classOf[Trait18].isAssignableFrom(cls))
    assertTrue("Trait18 isInstance", classOf[Trait18].isInstance(anyInstance))
    assertTrue("contains interface of Trait18", cls.getInterfaces().contains(classOf[Trait18]))

    assertTrue("isInstanceOf[Trait19]", anyInstance.isInstanceOf[Trait19])
    assertEquals("invoke[Trait19]", 19, anyInstance.asInstanceOf[Trait19].invoke19())
    assertTrue("isAssignableFrom[Trait19]", classOf[Trait19].isAssignableFrom(cls))
    assertTrue("Trait19 isInstance", classOf[Trait19].isInstance(anyInstance))
    assertTrue("contains interface of Trait19", cls.getInterfaces().contains(classOf[Trait19]))

    assertTrue("isInstanceOf[Trait20]", anyInstance.isInstanceOf[Trait20])
    assertEquals("invoke[Trait20]", 20, anyInstance.asInstanceOf[Trait20].invoke20())
    assertTrue("isAssignableFrom[Trait20]", classOf[Trait20].isAssignableFrom(cls))
    assertTrue("Trait20 isInstance", classOf[Trait20].isInstance(anyInstance))
    assertTrue("contains interface of Trait20", cls.getInterfaces().contains(classOf[Trait20]))

    assertTrue("isInstanceOf[Trait21]", anyInstance.isInstanceOf[Trait21])
    assertEquals("invoke[Trait21]", 21, anyInstance.asInstanceOf[Trait21].invoke21())
    assertTrue("isAssignableFrom[Trait21]", classOf[Trait21].isAssignableFrom(cls))
    assertTrue("Trait21 isInstance", classOf[Trait21].isInstance(anyInstance))
    assertTrue("contains interface of Trait21", cls.getInterfaces().contains(classOf[Trait21]))

    assertTrue("isInstanceOf[Trait22]", anyInstance.isInstanceOf[Trait22])
    assertEquals("invoke[Trait22]", 22, anyInstance.asInstanceOf[Trait22].invoke22())
    assertTrue("isAssignableFrom[Trait22]", classOf[Trait22].isAssignableFrom(cls))
    assertTrue("Trait22 isInstance", classOf[Trait22].isInstance(anyInstance))
    assertTrue("contains interface of Trait22", cls.getInterfaces().contains(classOf[Trait22]))

    assertTrue("isInstanceOf[Trait23]", anyInstance.isInstanceOf[Trait23])
    assertEquals("invoke[Trait23]", 23, anyInstance.asInstanceOf[Trait23].invoke23())
    assertTrue("isAssignableFrom[Trait23]", classOf[Trait23].isAssignableFrom(cls))
    assertTrue("Trait23 isInstance", classOf[Trait23].isInstance(anyInstance))
    assertTrue("contains interface of Trait23", cls.getInterfaces().contains(classOf[Trait23]))

    assertTrue("isInstanceOf[Trait24]", anyInstance.isInstanceOf[Trait24])
    assertEquals("invoke[Trait24]", 24, anyInstance.asInstanceOf[Trait24].invoke24())
    assertTrue("isAssignableFrom[Trait24]", classOf[Trait24].isAssignableFrom(cls))
    assertTrue("Trait24 isInstance", classOf[Trait24].isInstance(anyInstance))
    assertTrue("contains interface of Trait24", cls.getInterfaces().contains(classOf[Trait24]))

    assertTrue("isInstanceOf[Trait25]", anyInstance.isInstanceOf[Trait25])
    assertEquals("invoke[Trait25]", 25, anyInstance.asInstanceOf[Trait25].invoke25())
    assertTrue("isAssignableFrom[Trait25]", classOf[Trait25].isAssignableFrom(cls))
    assertTrue("Trait25 isInstance", classOf[Trait25].isInstance(anyInstance))
    assertTrue("contains interface of Trait25", cls.getInterfaces().contains(classOf[Trait25]))

    assertTrue("isInstanceOf[Trait26]", anyInstance.isInstanceOf[Trait26])
    assertEquals("invoke[Trait26]", 26, anyInstance.asInstanceOf[Trait26].invoke26())
    assertTrue("isAssignableFrom[Trait26]", classOf[Trait26].isAssignableFrom(cls))
    assertTrue("Trait26 isInstance", classOf[Trait26].isInstance(anyInstance))
    assertTrue("contains interface of Trait26", cls.getInterfaces().contains(classOf[Trait26]))

    assertTrue("isInstanceOf[Trait27]", anyInstance.isInstanceOf[Trait27])
    assertEquals("invoke[Trait27]", 27, anyInstance.asInstanceOf[Trait27].invoke27())
    assertTrue("isAssignableFrom[Trait27]", classOf[Trait27].isAssignableFrom(cls))
    assertTrue("Trait27 isInstance", classOf[Trait27].isInstance(anyInstance))
    assertTrue("contains interface of Trait27", cls.getInterfaces().contains(classOf[Trait27]))

    assertTrue("isInstanceOf[Trait28]", anyInstance.isInstanceOf[Trait28])
    assertEquals("invoke[Trait28]", 28, anyInstance.asInstanceOf[Trait28].invoke28())
    assertTrue("isAssignableFrom[Trait28]", classOf[Trait28].isAssignableFrom(cls))
    assertTrue("Trait28 isInstance", classOf[Trait28].isInstance(anyInstance))
    assertTrue("contains interface of Trait28", cls.getInterfaces().contains(classOf[Trait28]))

    assertTrue("isInstanceOf[Trait29]", anyInstance.isInstanceOf[Trait29])
    assertEquals("invoke[Trait29]", 29, anyInstance.asInstanceOf[Trait29].invoke29())
    assertTrue("isAssignableFrom[Trait29]", classOf[Trait29].isAssignableFrom(cls))
    assertTrue("Trait29 isInstance", classOf[Trait29].isInstance(anyInstance))
    assertTrue("contains interface of Trait29", cls.getInterfaces().contains(classOf[Trait29]))

    assertTrue("isInstanceOf[Trait30]", anyInstance.isInstanceOf[Trait30])
    assertEquals("invoke[Trait30]", 30, anyInstance.asInstanceOf[Trait30].invoke30())
    assertTrue("isAssignableFrom[Trait30]", classOf[Trait30].isAssignableFrom(cls))
    assertTrue("Trait30 isInstance", classOf[Trait30].isInstance(anyInstance))
    assertTrue("contains interface of Trait30", cls.getInterfaces().contains(classOf[Trait30]))

    assertTrue("isInstanceOf[Trait31]", anyInstance.isInstanceOf[Trait31])
    assertEquals("invoke[Trait31]", 31, anyInstance.asInstanceOf[Trait31].invoke31())
    assertTrue("isAssignableFrom[Trait31]", classOf[Trait31].isAssignableFrom(cls))
    assertTrue("Trait31 isInstance", classOf[Trait31].isInstance(anyInstance))
    assertTrue("contains interface of Trait31", cls.getInterfaces().contains(classOf[Trait31]))

    assertTrue("isInstanceOf[Trait32]", anyInstance.isInstanceOf[Trait32])
    assertEquals("invoke[Trait32]", 32, anyInstance.asInstanceOf[Trait32].invoke32())
    assertTrue("isAssignableFrom[Trait32]", classOf[Trait32].isAssignableFrom(cls))
    assertTrue("Trait32 isInstance", classOf[Trait32].isInstance(anyInstance))
    assertTrue("contains interface of Trait32", cls.getInterfaces().contains(classOf[Trait32]))

    assertTrue("isInstanceOf[Trait33]", anyInstance.isInstanceOf[Trait33])
    assertEquals("invoke[Trait33]", 33, anyInstance.asInstanceOf[Trait33].invoke33())
    assertTrue("isAssignableFrom[Trait33]", classOf[Trait33].isAssignableFrom(cls))
    assertTrue("Trait33 isInstance", classOf[Trait33].isInstance(anyInstance))
    assertTrue("contains interface of Trait33", cls.getInterfaces().contains(classOf[Trait33]))

    assertTrue("isInstanceOf[Trait34]", anyInstance.isInstanceOf[Trait34])
    assertEquals("invoke[Trait34]", 34, anyInstance.asInstanceOf[Trait34].invoke34())
    assertTrue("isAssignableFrom[Trait34]", classOf[Trait34].isAssignableFrom(cls))
    assertTrue("Trait34 isInstance", classOf[Trait34].isInstance(anyInstance))
    assertTrue("contains interface of Trait34", cls.getInterfaces().contains(classOf[Trait34]))

    assertTrue("isInstanceOf[Trait35]", anyInstance.isInstanceOf[Trait35])
    assertEquals("invoke[Trait35]", 35, anyInstance.asInstanceOf[Trait35].invoke35())
    assertTrue("isAssignableFrom[Trait35]", classOf[Trait35].isAssignableFrom(cls))
    assertTrue("Trait35 isInstance", classOf[Trait35].isInstance(anyInstance))
    assertTrue("contains interface of Trait35", cls.getInterfaces().contains(classOf[Trait35]))

    assertTrue("isInstanceOf[Trait36]", anyInstance.isInstanceOf[Trait36])
    assertEquals("invoke[Trait36]", 36, anyInstance.asInstanceOf[Trait36].invoke36())
    assertTrue("isAssignableFrom[Trait36]", classOf[Trait36].isAssignableFrom(cls))
    assertTrue("Trait36 isInstance", classOf[Trait36].isInstance(anyInstance))
    assertTrue("contains interface of Trait36", cls.getInterfaces().contains(classOf[Trait36]))

    assertTrue("isInstanceOf[Trait37]", anyInstance.isInstanceOf[Trait37])
    assertEquals("invoke[Trait37]", 37, anyInstance.asInstanceOf[Trait37].invoke37())
    assertTrue("isAssignableFrom[Trait37]", classOf[Trait37].isAssignableFrom(cls))
    assertTrue("Trait37 isInstance", classOf[Trait37].isInstance(anyInstance))
    assertTrue("contains interface of Trait37", cls.getInterfaces().contains(classOf[Trait37]))

    assertTrue("isInstanceOf[Trait38]", anyInstance.isInstanceOf[Trait38])
    assertEquals("invoke[Trait38]", 38, anyInstance.asInstanceOf[Trait38].invoke38())
    assertTrue("isAssignableFrom[Trait38]", classOf[Trait38].isAssignableFrom(cls))
    assertTrue("Trait38 isInstance", classOf[Trait38].isInstance(anyInstance))
    assertTrue("contains interface of Trait38", cls.getInterfaces().contains(classOf[Trait38]))

    assertTrue("isInstanceOf[Trait39]", anyInstance.isInstanceOf[Trait39])
    assertEquals("invoke[Trait39]", 39, anyInstance.asInstanceOf[Trait39].invoke39())
    assertTrue("isAssignableFrom[Trait39]", classOf[Trait39].isAssignableFrom(cls))
    assertTrue("Trait39 isInstance", classOf[Trait39].isInstance(anyInstance))
    assertTrue("contains interface of Trait39", cls.getInterfaces().contains(classOf[Trait39]))

    assertTrue("isInstanceOf[Trait40]", anyInstance.isInstanceOf[Trait40])
    assertEquals("invoke[Trait40]", 40, anyInstance.asInstanceOf[Trait40].invoke40())
    assertTrue("isAssignableFrom[Trait40]", classOf[Trait40].isAssignableFrom(cls))
    assertTrue("Trait40 isInstance", classOf[Trait40].isInstance(anyInstance))
    assertTrue("contains interface of Trait40", cls.getInterfaces().contains(classOf[Trait40]))

    assertTrue("isInstanceOf[Trait41]", anyInstance.isInstanceOf[Trait41])
    assertEquals("invoke[Trait41]", 41, anyInstance.asInstanceOf[Trait41].invoke41())
    assertTrue("isAssignableFrom[Trait41]", classOf[Trait41].isAssignableFrom(cls))
    assertTrue("Trait41 isInstance", classOf[Trait41].isInstance(anyInstance))
    assertTrue("contains interface of Trait41", cls.getInterfaces().contains(classOf[Trait41]))

    assertTrue("isInstanceOf[Trait42]", anyInstance.isInstanceOf[Trait42])
    assertEquals("invoke[Trait42]", 42, anyInstance.asInstanceOf[Trait42].invoke42())
    assertTrue("isAssignableFrom[Trait42]", classOf[Trait42].isAssignableFrom(cls))
    assertTrue("Trait42 isInstance", classOf[Trait42].isInstance(anyInstance))
    assertTrue("contains interface of Trait42", cls.getInterfaces().contains(classOf[Trait42]))

    assertTrue("isInstanceOf[Trait43]", anyInstance.isInstanceOf[Trait43])
    assertEquals("invoke[Trait43]", 43, anyInstance.asInstanceOf[Trait43].invoke43())
    assertTrue("isAssignableFrom[Trait43]", classOf[Trait43].isAssignableFrom(cls))
    assertTrue("Trait43 isInstance", classOf[Trait43].isInstance(anyInstance))
    assertTrue("contains interface of Trait43", cls.getInterfaces().contains(classOf[Trait43]))

    assertTrue("isInstanceOf[Trait44]", anyInstance.isInstanceOf[Trait44])
    assertEquals("invoke[Trait44]", 44, anyInstance.asInstanceOf[Trait44].invoke44())
    assertTrue("isAssignableFrom[Trait44]", classOf[Trait44].isAssignableFrom(cls))
    assertTrue("Trait44 isInstance", classOf[Trait44].isInstance(anyInstance))
    assertTrue("contains interface of Trait44", cls.getInterfaces().contains(classOf[Trait44]))

    assertTrue("isInstanceOf[Trait45]", anyInstance.isInstanceOf[Trait45])
    assertEquals("invoke[Trait45]", 45, anyInstance.asInstanceOf[Trait45].invoke45())
    assertTrue("isAssignableFrom[Trait45]", classOf[Trait45].isAssignableFrom(cls))
    assertTrue("Trait45 isInstance", classOf[Trait45].isInstance(anyInstance))
    assertTrue("contains interface of Trait45", cls.getInterfaces().contains(classOf[Trait45]))

    assertTrue("isInstanceOf[Trait46]", anyInstance.isInstanceOf[Trait46])
    assertEquals("invoke[Trait46]", 46, anyInstance.asInstanceOf[Trait46].invoke46())
    assertTrue("isAssignableFrom[Trait46]", classOf[Trait46].isAssignableFrom(cls))
    assertTrue("Trait46 isInstance", classOf[Trait46].isInstance(anyInstance))
    assertTrue("contains interface of Trait46", cls.getInterfaces().contains(classOf[Trait46]))

    assertTrue("isInstanceOf[Trait47]", anyInstance.isInstanceOf[Trait47])
    assertEquals("invoke[Trait47]", 47, anyInstance.asInstanceOf[Trait47].invoke47())
    assertTrue("isAssignableFrom[Trait47]", classOf[Trait47].isAssignableFrom(cls))
    assertTrue("Trait47 isInstance", classOf[Trait47].isInstance(anyInstance))
    assertTrue("contains interface of Trait47", cls.getInterfaces().contains(classOf[Trait47]))

    assertTrue("isInstanceOf[Trait48]", anyInstance.isInstanceOf[Trait48])
    assertEquals("invoke[Trait48]", 48, anyInstance.asInstanceOf[Trait48].invoke48())
    assertTrue("isAssignableFrom[Trait48]", classOf[Trait48].isAssignableFrom(cls))
    assertTrue("Trait48 isInstance", classOf[Trait48].isInstance(anyInstance))
    assertTrue("contains interface of Trait48", cls.getInterfaces().contains(classOf[Trait48]))

    assertTrue("isInstanceOf[Trait49]", anyInstance.isInstanceOf[Trait49])
    assertEquals("invoke[Trait49]", 49, anyInstance.asInstanceOf[Trait49].invoke49())
    assertTrue("isAssignableFrom[Trait49]", classOf[Trait49].isAssignableFrom(cls))
    assertTrue("Trait49 isInstance", classOf[Trait49].isInstance(anyInstance))
    assertTrue("contains interface of Trait49", cls.getInterfaces().contains(classOf[Trait49]))

    assertTrue("isInstanceOf[Trait50]", anyInstance.isInstanceOf[Trait50])
    assertEquals("invoke[Trait50]", 50, anyInstance.asInstanceOf[Trait50].invoke50())
    assertTrue("isAssignableFrom[Trait50]", classOf[Trait50].isAssignableFrom(cls))
    assertTrue("Trait50 isInstance", classOf[Trait50].isInstance(anyInstance))
    assertTrue("contains interface of Trait50", cls.getInterfaces().contains(classOf[Trait50]))

    assertTrue("isInstanceOf[Trait51]", anyInstance.isInstanceOf[Trait51])
    assertEquals("invoke[Trait51]", 51, anyInstance.asInstanceOf[Trait51].invoke51())
    assertTrue("isAssignableFrom[Trait51]", classOf[Trait51].isAssignableFrom(cls))
    assertTrue("Trait51 isInstance", classOf[Trait51].isInstance(anyInstance))
    assertTrue("contains interface of Trait51", cls.getInterfaces().contains(classOf[Trait51]))

    assertTrue("isInstanceOf[Trait52]", anyInstance.isInstanceOf[Trait52])
    assertEquals("invoke[Trait52]", 52, anyInstance.asInstanceOf[Trait52].invoke52())
    assertTrue("isAssignableFrom[Trait52]", classOf[Trait52].isAssignableFrom(cls))
    assertTrue("Trait52 isInstance", classOf[Trait52].isInstance(anyInstance))
    assertTrue("contains interface of Trait52", cls.getInterfaces().contains(classOf[Trait52]))

    assertTrue("isInstanceOf[Trait53]", anyInstance.isInstanceOf[Trait53])
    assertEquals("invoke[Trait53]", 53, anyInstance.asInstanceOf[Trait53].invoke53())
    assertTrue("isAssignableFrom[Trait53]", classOf[Trait53].isAssignableFrom(cls))
    assertTrue("Trait53 isInstance", classOf[Trait53].isInstance(anyInstance))
    assertTrue("contains interface of Trait53", cls.getInterfaces().contains(classOf[Trait53]))

    assertTrue("isInstanceOf[Trait54]", anyInstance.isInstanceOf[Trait54])
    assertEquals("invoke[Trait54]", 54, anyInstance.asInstanceOf[Trait54].invoke54())
    assertTrue("isAssignableFrom[Trait54]", classOf[Trait54].isAssignableFrom(cls))
    assertTrue("Trait54 isInstance", classOf[Trait54].isInstance(anyInstance))
    assertTrue("contains interface of Trait54", cls.getInterfaces().contains(classOf[Trait54]))

    assertTrue("isInstanceOf[Trait55]", anyInstance.isInstanceOf[Trait55])
    assertEquals("invoke[Trait55]", 55, anyInstance.asInstanceOf[Trait55].invoke55())
    assertTrue("isAssignableFrom[Trait55]", classOf[Trait55].isAssignableFrom(cls))
    assertTrue("Trait55 isInstance", classOf[Trait55].isInstance(anyInstance))
    assertTrue("contains interface of Trait55", cls.getInterfaces().contains(classOf[Trait55]))

    assertTrue("isInstanceOf[Trait56]", anyInstance.isInstanceOf[Trait56])
    assertEquals("invoke[Trait56]", 56, anyInstance.asInstanceOf[Trait56].invoke56())
    assertTrue("isAssignableFrom[Trait56]", classOf[Trait56].isAssignableFrom(cls))
    assertTrue("Trait56 isInstance", classOf[Trait56].isInstance(anyInstance))
    assertTrue("contains interface of Trait56", cls.getInterfaces().contains(classOf[Trait56]))

    assertTrue("isInstanceOf[Trait57]", anyInstance.isInstanceOf[Trait57])
    assertEquals("invoke[Trait57]", 57, anyInstance.asInstanceOf[Trait57].invoke57())
    assertTrue("isAssignableFrom[Trait57]", classOf[Trait57].isAssignableFrom(cls))
    assertTrue("Trait57 isInstance", classOf[Trait57].isInstance(anyInstance))
    assertTrue("contains interface of Trait57", cls.getInterfaces().contains(classOf[Trait57]))

    assertTrue("isInstanceOf[Trait58]", anyInstance.isInstanceOf[Trait58])
    assertEquals("invoke[Trait58]", 58, anyInstance.asInstanceOf[Trait58].invoke58())
    assertTrue("isAssignableFrom[Trait58]", classOf[Trait58].isAssignableFrom(cls))
    assertTrue("Trait58 isInstance", classOf[Trait58].isInstance(anyInstance))
    assertTrue("contains interface of Trait58", cls.getInterfaces().contains(classOf[Trait58]))

    assertTrue("isInstanceOf[Trait59]", anyInstance.isInstanceOf[Trait59])
    assertEquals("invoke[Trait59]", 59, anyInstance.asInstanceOf[Trait59].invoke59())
    assertTrue("isAssignableFrom[Trait59]", classOf[Trait59].isAssignableFrom(cls))
    assertTrue("Trait59 isInstance", classOf[Trait59].isInstance(anyInstance))
    assertTrue("contains interface of Trait59", cls.getInterfaces().contains(classOf[Trait59]))

    assertTrue("isInstanceOf[Trait60]", anyInstance.isInstanceOf[Trait60])
    assertEquals("invoke[Trait60]", 60, anyInstance.asInstanceOf[Trait60].invoke60())
    assertTrue("isAssignableFrom[Trait60]", classOf[Trait60].isAssignableFrom(cls))
    assertTrue("Trait60 isInstance", classOf[Trait60].isInstance(anyInstance))
    assertTrue("contains interface of Trait60", cls.getInterfaces().contains(classOf[Trait60]))

    assertTrue("isInstanceOf[Trait61]", anyInstance.isInstanceOf[Trait61])
    assertEquals("invoke[Trait61]", 61, anyInstance.asInstanceOf[Trait61].invoke61())
    assertTrue("isAssignableFrom[Trait61]", classOf[Trait61].isAssignableFrom(cls))
    assertTrue("Trait61 isInstance", classOf[Trait61].isInstance(anyInstance))
    assertTrue("contains interface of Trait61", cls.getInterfaces().contains(classOf[Trait61]))

    assertTrue("isInstanceOf[Trait62]", anyInstance.isInstanceOf[Trait62])
    assertEquals("invoke[Trait62]", 62, anyInstance.asInstanceOf[Trait62].invoke62())
    assertTrue("isAssignableFrom[Trait62]", classOf[Trait62].isAssignableFrom(cls))
    assertTrue("Trait62 isInstance", classOf[Trait62].isInstance(anyInstance))
    assertTrue("contains interface of Trait62", cls.getInterfaces().contains(classOf[Trait62]))

    assertTrue("isInstanceOf[Trait63]", anyInstance.isInstanceOf[Trait63])
    assertEquals("invoke[Trait63]", 63, anyInstance.asInstanceOf[Trait63].invoke63())
    assertTrue("isAssignableFrom[Trait63]", classOf[Trait63].isAssignableFrom(cls))
    assertTrue("Trait63 isInstance", classOf[Trait63].isInstance(anyInstance))
    assertTrue("contains interface of Trait63", cls.getInterfaces().contains(classOf[Trait63]))
  }
}

class Impl1 extends Trait0 with Trait1 with Trait2 with Trait3 with Trait4 with Trait5 with Trait6 with Trait7 with Trait8 with Trait9 with Trait10 with Trait11 with Trait12 with Trait13 with Trait14 with Trait15 with Trait16 with Trait17 with Trait18 with Trait19 with Trait20 with Trait21 with Trait22 with Trait23 with Trait24 with Trait25 with Trait26 with Trait27 with Trait28 with Trait29 with Trait30 with Trait31 with Trait32 with Trait33 with Trait34 with Trait35 with Trait36 with Trait37 with Trait38 with Trait39 with Trait40 with Trait41 with Trait42 with Trait43 with Trait44 with Trait45 with Trait46 with Trait47 with Trait48 with Trait49 with Trait50 with Trait51 with Trait52 with Trait53 with Trait54 with Trait55 with Trait56 with Trait57 with Trait58 with Trait59 with Trait60 with Trait61 with Trait62 with Trait63 {
  override def invoke0(): Int = 1000
  override def invoke1(): Int = 1001
  override def invoke2(): Int = 1002
  override def invoke3(): Int = 1003
  override def invoke4(): Int = 1004
  override def invoke5(): Int = 1005
  override def invoke6(): Int = 1006
  override def invoke7(): Int = 1007
  override def invoke8(): Int = 1008
  override def invoke9(): Int = 1009
  override def invoke10(): Int = 1010
  override def invoke11(): Int = 1011
  override def invoke12(): Int = 1012
  override def invoke13(): Int = 1013
  override def invoke14(): Int = 1014
  override def invoke15(): Int = 1015
  override def invoke16(): Int = 1016
  override def invoke17(): Int = 1017
  override def invoke18(): Int = 1018
  override def invoke19(): Int = 1019
  override def invoke20(): Int = 1020
  override def invoke21(): Int = 1021
  override def invoke22(): Int = 1022
  override def invoke23(): Int = 1023
  override def invoke24(): Int = 1024
  override def invoke25(): Int = 1025
  override def invoke26(): Int = 1026
  override def invoke27(): Int = 1027
  override def invoke28(): Int = 1028
  override def invoke29(): Int = 1029
  override def invoke30(): Int = 1030
  override def invoke31(): Int = 1031
  override def invoke32(): Int = 1032
  override def invoke33(): Int = 1033
  override def invoke34(): Int = 1034
  override def invoke35(): Int = 1035
  override def invoke36(): Int = 1036
  override def invoke37(): Int = 1037
  override def invoke38(): Int = 1038
  override def invoke39(): Int = 1039
  override def invoke40(): Int = 1040
  override def invoke41(): Int = 1041
  override def invoke42(): Int = 1042
  override def invoke43(): Int = 1043
  override def invoke44(): Int = 1044
  override def invoke45(): Int = 1045
  override def invoke46(): Int = 1046
  override def invoke47(): Int = 1047
  override def invoke48(): Int = 1048
  override def invoke49(): Int = 1049
  override def invoke50(): Int = 1050
  override def invoke51(): Int = 1051
  override def invoke52(): Int = 1052
  override def invoke53(): Int = 1053
  override def invoke54(): Int = 1054
  override def invoke55(): Int = 1055
  override def invoke56(): Int = 1056
  override def invoke57(): Int = 1057
  override def invoke58(): Int = 1058
  override def invoke59(): Int = 1059
  override def invoke60(): Int = 1060
  override def invoke61(): Int = 1061
  override def invoke62(): Int = 1062
  override def invoke63(): Int = 1063
}
object Impl1 {
  def test(): Unit = {
    val instance = new Impl1()
    val anyInstance: Any = instance
    val cls = anyInstance.getClass()

    assertTrue("isInstanceOf[Trait0]", anyInstance.isInstanceOf[Trait0])
    assertEquals("invoke[Trait0]", 1000, anyInstance.asInstanceOf[Trait0].invoke0())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait0].isAssignableFrom(cls))
    assertTrue("Trait0 isInstance", classOf[Trait0].isInstance(anyInstance))
    assertTrue("contains interface of Trait0", cls.getInterfaces().contains(classOf[Trait0]))

    assertTrue("isInstanceOf[Trait1]", anyInstance.isInstanceOf[Trait1])
    assertEquals("invoke[Trait1]", 1001, anyInstance.asInstanceOf[Trait1].invoke1())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait1].isAssignableFrom(cls))
    assertTrue("Trait1 isInstance", classOf[Trait1].isInstance(anyInstance))
    assertTrue("contains interface of Trait1", cls.getInterfaces().contains(classOf[Trait1]))

    assertTrue("isInstanceOf[Trait2]", anyInstance.isInstanceOf[Trait2])
    assertEquals("invoke[Trait2]", 1002, anyInstance.asInstanceOf[Trait2].invoke2())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait2].isAssignableFrom(cls))
    assertTrue("Trait2 isInstance", classOf[Trait2].isInstance(anyInstance))
    assertTrue("contains interface of Trait2", cls.getInterfaces().contains(classOf[Trait2]))

    assertTrue("isInstanceOf[Trait3]", anyInstance.isInstanceOf[Trait3])
    assertEquals("invoke[Trait3]", 1003, anyInstance.asInstanceOf[Trait3].invoke3())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait3].isAssignableFrom(cls))
    assertTrue("Trait3 isInstance", classOf[Trait3].isInstance(anyInstance))
    assertTrue("contains interface of Trait3", cls.getInterfaces().contains(classOf[Trait3]))

    assertTrue("isInstanceOf[Trait4]", anyInstance.isInstanceOf[Trait4])
    assertEquals("invoke[Trait4]", 1004, anyInstance.asInstanceOf[Trait4].invoke4())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait4].isAssignableFrom(cls))
    assertTrue("Trait4 isInstance", classOf[Trait4].isInstance(anyInstance))
    assertTrue("contains interface of Trait4", cls.getInterfaces().contains(classOf[Trait4]))

    assertTrue("isInstanceOf[Trait5]", anyInstance.isInstanceOf[Trait5])
    assertEquals("invoke[Trait5]", 1005, anyInstance.asInstanceOf[Trait5].invoke5())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait5].isAssignableFrom(cls))
    assertTrue("Trait5 isInstance", classOf[Trait5].isInstance(anyInstance))
    assertTrue("contains interface of Trait5", cls.getInterfaces().contains(classOf[Trait5]))

    assertTrue("isInstanceOf[Trait6]", anyInstance.isInstanceOf[Trait6])
    assertEquals("invoke[Trait6]", 1006, anyInstance.asInstanceOf[Trait6].invoke6())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait6].isAssignableFrom(cls))
    assertTrue("Trait6 isInstance", classOf[Trait6].isInstance(anyInstance))
    assertTrue("contains interface of Trait6", cls.getInterfaces().contains(classOf[Trait6]))

    assertTrue("isInstanceOf[Trait7]", anyInstance.isInstanceOf[Trait7])
    assertEquals("invoke[Trait7]", 1007, anyInstance.asInstanceOf[Trait7].invoke7())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait7].isAssignableFrom(cls))
    assertTrue("Trait7 isInstance", classOf[Trait7].isInstance(anyInstance))
    assertTrue("contains interface of Trait7", cls.getInterfaces().contains(classOf[Trait7]))

    assertTrue("isInstanceOf[Trait8]", anyInstance.isInstanceOf[Trait8])
    assertEquals("invoke[Trait8]", 1008, anyInstance.asInstanceOf[Trait8].invoke8())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait8].isAssignableFrom(cls))
    assertTrue("Trait8 isInstance", classOf[Trait8].isInstance(anyInstance))
    assertTrue("contains interface of Trait8", cls.getInterfaces().contains(classOf[Trait8]))

    assertTrue("isInstanceOf[Trait9]", anyInstance.isInstanceOf[Trait9])
    assertEquals("invoke[Trait9]", 1009, anyInstance.asInstanceOf[Trait9].invoke9())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait9].isAssignableFrom(cls))
    assertTrue("Trait9 isInstance", classOf[Trait9].isInstance(anyInstance))
    assertTrue("contains interface of Trait9", cls.getInterfaces().contains(classOf[Trait9]))

    assertTrue("isInstanceOf[Trait10]", anyInstance.isInstanceOf[Trait10])
    assertEquals("invoke[Trait10]", 1010, anyInstance.asInstanceOf[Trait10].invoke10())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait10].isAssignableFrom(cls))
    assertTrue("Trait10 isInstance", classOf[Trait10].isInstance(anyInstance))
    assertTrue("contains interface of Trait10", cls.getInterfaces().contains(classOf[Trait10]))

    assertTrue("isInstanceOf[Trait11]", anyInstance.isInstanceOf[Trait11])
    assertEquals("invoke[Trait11]", 1011, anyInstance.asInstanceOf[Trait11].invoke11())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait11].isAssignableFrom(cls))
    assertTrue("Trait11 isInstance", classOf[Trait11].isInstance(anyInstance))
    assertTrue("contains interface of Trait11", cls.getInterfaces().contains(classOf[Trait11]))

    assertTrue("isInstanceOf[Trait12]", anyInstance.isInstanceOf[Trait12])
    assertEquals("invoke[Trait12]", 1012, anyInstance.asInstanceOf[Trait12].invoke12())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait12].isAssignableFrom(cls))
    assertTrue("Trait12 isInstance", classOf[Trait12].isInstance(anyInstance))
    assertTrue("contains interface of Trait12", cls.getInterfaces().contains(classOf[Trait12]))

    assertTrue("isInstanceOf[Trait13]", anyInstance.isInstanceOf[Trait13])
    assertEquals("invoke[Trait13]", 1013, anyInstance.asInstanceOf[Trait13].invoke13())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait13].isAssignableFrom(cls))
    assertTrue("Trait13 isInstance", classOf[Trait13].isInstance(anyInstance))
    assertTrue("contains interface of Trait13", cls.getInterfaces().contains(classOf[Trait13]))

    assertTrue("isInstanceOf[Trait14]", anyInstance.isInstanceOf[Trait14])
    assertEquals("invoke[Trait14]", 1014, anyInstance.asInstanceOf[Trait14].invoke14())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait14].isAssignableFrom(cls))
    assertTrue("Trait14 isInstance", classOf[Trait14].isInstance(anyInstance))
    assertTrue("contains interface of Trait14", cls.getInterfaces().contains(classOf[Trait14]))

    assertTrue("isInstanceOf[Trait15]", anyInstance.isInstanceOf[Trait15])
    assertEquals("invoke[Trait15]", 1015, anyInstance.asInstanceOf[Trait15].invoke15())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait15].isAssignableFrom(cls))
    assertTrue("Trait15 isInstance", classOf[Trait15].isInstance(anyInstance))
    assertTrue("contains interface of Trait15", cls.getInterfaces().contains(classOf[Trait15]))

    assertTrue("isInstanceOf[Trait16]", anyInstance.isInstanceOf[Trait16])
    assertEquals("invoke[Trait16]", 1016, anyInstance.asInstanceOf[Trait16].invoke16())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait16].isAssignableFrom(cls))
    assertTrue("Trait16 isInstance", classOf[Trait16].isInstance(anyInstance))
    assertTrue("contains interface of Trait16", cls.getInterfaces().contains(classOf[Trait16]))

    assertTrue("isInstanceOf[Trait17]", anyInstance.isInstanceOf[Trait17])
    assertEquals("invoke[Trait17]", 1017, anyInstance.asInstanceOf[Trait17].invoke17())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait17].isAssignableFrom(cls))
    assertTrue("Trait17 isInstance", classOf[Trait17].isInstance(anyInstance))
    assertTrue("contains interface of Trait17", cls.getInterfaces().contains(classOf[Trait17]))

    assertTrue("isInstanceOf[Trait18]", anyInstance.isInstanceOf[Trait18])
    assertEquals("invoke[Trait18]", 1018, anyInstance.asInstanceOf[Trait18].invoke18())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait18].isAssignableFrom(cls))
    assertTrue("Trait18 isInstance", classOf[Trait18].isInstance(anyInstance))
    assertTrue("contains interface of Trait18", cls.getInterfaces().contains(classOf[Trait18]))

    assertTrue("isInstanceOf[Trait19]", anyInstance.isInstanceOf[Trait19])
    assertEquals("invoke[Trait19]", 1019, anyInstance.asInstanceOf[Trait19].invoke19())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait19].isAssignableFrom(cls))
    assertTrue("Trait19 isInstance", classOf[Trait19].isInstance(anyInstance))
    assertTrue("contains interface of Trait19", cls.getInterfaces().contains(classOf[Trait19]))

    assertTrue("isInstanceOf[Trait20]", anyInstance.isInstanceOf[Trait20])
    assertEquals("invoke[Trait20]", 1020, anyInstance.asInstanceOf[Trait20].invoke20())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait20].isAssignableFrom(cls))
    assertTrue("Trait20 isInstance", classOf[Trait20].isInstance(anyInstance))
    assertTrue("contains interface of Trait20", cls.getInterfaces().contains(classOf[Trait20]))

    assertTrue("isInstanceOf[Trait21]", anyInstance.isInstanceOf[Trait21])
    assertEquals("invoke[Trait21]", 1021, anyInstance.asInstanceOf[Trait21].invoke21())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait21].isAssignableFrom(cls))
    assertTrue("Trait21 isInstance", classOf[Trait21].isInstance(anyInstance))
    assertTrue("contains interface of Trait21", cls.getInterfaces().contains(classOf[Trait21]))

    assertTrue("isInstanceOf[Trait22]", anyInstance.isInstanceOf[Trait22])
    assertEquals("invoke[Trait22]", 1022, anyInstance.asInstanceOf[Trait22].invoke22())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait22].isAssignableFrom(cls))
    assertTrue("Trait22 isInstance", classOf[Trait22].isInstance(anyInstance))
    assertTrue("contains interface of Trait22", cls.getInterfaces().contains(classOf[Trait22]))

    assertTrue("isInstanceOf[Trait23]", anyInstance.isInstanceOf[Trait23])
    assertEquals("invoke[Trait23]", 1023, anyInstance.asInstanceOf[Trait23].invoke23())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait23].isAssignableFrom(cls))
    assertTrue("Trait23 isInstance", classOf[Trait23].isInstance(anyInstance))
    assertTrue("contains interface of Trait23", cls.getInterfaces().contains(classOf[Trait23]))

    assertTrue("isInstanceOf[Trait24]", anyInstance.isInstanceOf[Trait24])
    assertEquals("invoke[Trait24]", 1024, anyInstance.asInstanceOf[Trait24].invoke24())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait24].isAssignableFrom(cls))
    assertTrue("Trait24 isInstance", classOf[Trait24].isInstance(anyInstance))
    assertTrue("contains interface of Trait24", cls.getInterfaces().contains(classOf[Trait24]))

    assertTrue("isInstanceOf[Trait25]", anyInstance.isInstanceOf[Trait25])
    assertEquals("invoke[Trait25]", 1025, anyInstance.asInstanceOf[Trait25].invoke25())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait25].isAssignableFrom(cls))
    assertTrue("Trait25 isInstance", classOf[Trait25].isInstance(anyInstance))
    assertTrue("contains interface of Trait25", cls.getInterfaces().contains(classOf[Trait25]))

    assertTrue("isInstanceOf[Trait26]", anyInstance.isInstanceOf[Trait26])
    assertEquals("invoke[Trait26]", 1026, anyInstance.asInstanceOf[Trait26].invoke26())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait26].isAssignableFrom(cls))
    assertTrue("Trait26 isInstance", classOf[Trait26].isInstance(anyInstance))
    assertTrue("contains interface of Trait26", cls.getInterfaces().contains(classOf[Trait26]))

    assertTrue("isInstanceOf[Trait27]", anyInstance.isInstanceOf[Trait27])
    assertEquals("invoke[Trait27]", 1027, anyInstance.asInstanceOf[Trait27].invoke27())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait27].isAssignableFrom(cls))
    assertTrue("Trait27 isInstance", classOf[Trait27].isInstance(anyInstance))
    assertTrue("contains interface of Trait27", cls.getInterfaces().contains(classOf[Trait27]))

    assertTrue("isInstanceOf[Trait28]", anyInstance.isInstanceOf[Trait28])
    assertEquals("invoke[Trait28]", 1028, anyInstance.asInstanceOf[Trait28].invoke28())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait28].isAssignableFrom(cls))
    assertTrue("Trait28 isInstance", classOf[Trait28].isInstance(anyInstance))
    assertTrue("contains interface of Trait28", cls.getInterfaces().contains(classOf[Trait28]))

    assertTrue("isInstanceOf[Trait29]", anyInstance.isInstanceOf[Trait29])
    assertEquals("invoke[Trait29]", 1029, anyInstance.asInstanceOf[Trait29].invoke29())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait29].isAssignableFrom(cls))
    assertTrue("Trait29 isInstance", classOf[Trait29].isInstance(anyInstance))
    assertTrue("contains interface of Trait29", cls.getInterfaces().contains(classOf[Trait29]))

    assertTrue("isInstanceOf[Trait30]", anyInstance.isInstanceOf[Trait30])
    assertEquals("invoke[Trait30]", 1030, anyInstance.asInstanceOf[Trait30].invoke30())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait30].isAssignableFrom(cls))
    assertTrue("Trait30 isInstance", classOf[Trait30].isInstance(anyInstance))
    assertTrue("contains interface of Trait30", cls.getInterfaces().contains(classOf[Trait30]))

    assertTrue("isInstanceOf[Trait31]", anyInstance.isInstanceOf[Trait31])
    assertEquals("invoke[Trait31]", 1031, anyInstance.asInstanceOf[Trait31].invoke31())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait31].isAssignableFrom(cls))
    assertTrue("Trait31 isInstance", classOf[Trait31].isInstance(anyInstance))
    assertTrue("contains interface of Trait31", cls.getInterfaces().contains(classOf[Trait31]))

    assertTrue("isInstanceOf[Trait32]", anyInstance.isInstanceOf[Trait32])
    assertEquals("invoke[Trait32]", 1032, anyInstance.asInstanceOf[Trait32].invoke32())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait32].isAssignableFrom(cls))
    assertTrue("Trait32 isInstance", classOf[Trait32].isInstance(anyInstance))
    assertTrue("contains interface of Trait32", cls.getInterfaces().contains(classOf[Trait32]))

    assertTrue("isInstanceOf[Trait33]", anyInstance.isInstanceOf[Trait33])
    assertEquals("invoke[Trait33]", 1033, anyInstance.asInstanceOf[Trait33].invoke33())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait33].isAssignableFrom(cls))
    assertTrue("Trait33 isInstance", classOf[Trait33].isInstance(anyInstance))
    assertTrue("contains interface of Trait33", cls.getInterfaces().contains(classOf[Trait33]))

    assertTrue("isInstanceOf[Trait34]", anyInstance.isInstanceOf[Trait34])
    assertEquals("invoke[Trait34]", 1034, anyInstance.asInstanceOf[Trait34].invoke34())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait34].isAssignableFrom(cls))
    assertTrue("Trait34 isInstance", classOf[Trait34].isInstance(anyInstance))
    assertTrue("contains interface of Trait34", cls.getInterfaces().contains(classOf[Trait34]))

    assertTrue("isInstanceOf[Trait35]", anyInstance.isInstanceOf[Trait35])
    assertEquals("invoke[Trait35]", 1035, anyInstance.asInstanceOf[Trait35].invoke35())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait35].isAssignableFrom(cls))
    assertTrue("Trait35 isInstance", classOf[Trait35].isInstance(anyInstance))
    assertTrue("contains interface of Trait35", cls.getInterfaces().contains(classOf[Trait35]))

    assertTrue("isInstanceOf[Trait36]", anyInstance.isInstanceOf[Trait36])
    assertEquals("invoke[Trait36]", 1036, anyInstance.asInstanceOf[Trait36].invoke36())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait36].isAssignableFrom(cls))
    assertTrue("Trait36 isInstance", classOf[Trait36].isInstance(anyInstance))
    assertTrue("contains interface of Trait36", cls.getInterfaces().contains(classOf[Trait36]))

    assertTrue("isInstanceOf[Trait37]", anyInstance.isInstanceOf[Trait37])
    assertEquals("invoke[Trait37]", 1037, anyInstance.asInstanceOf[Trait37].invoke37())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait37].isAssignableFrom(cls))
    assertTrue("Trait37 isInstance", classOf[Trait37].isInstance(anyInstance))
    assertTrue("contains interface of Trait37", cls.getInterfaces().contains(classOf[Trait37]))

    assertTrue("isInstanceOf[Trait38]", anyInstance.isInstanceOf[Trait38])
    assertEquals("invoke[Trait38]", 1038, anyInstance.asInstanceOf[Trait38].invoke38())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait38].isAssignableFrom(cls))
    assertTrue("Trait38 isInstance", classOf[Trait38].isInstance(anyInstance))
    assertTrue("contains interface of Trait38", cls.getInterfaces().contains(classOf[Trait38]))

    assertTrue("isInstanceOf[Trait39]", anyInstance.isInstanceOf[Trait39])
    assertEquals("invoke[Trait39]", 1039, anyInstance.asInstanceOf[Trait39].invoke39())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait39].isAssignableFrom(cls))
    assertTrue("Trait39 isInstance", classOf[Trait39].isInstance(anyInstance))
    assertTrue("contains interface of Trait39", cls.getInterfaces().contains(classOf[Trait39]))

    assertTrue("isInstanceOf[Trait40]", anyInstance.isInstanceOf[Trait40])
    assertEquals("invoke[Trait40]", 1040, anyInstance.asInstanceOf[Trait40].invoke40())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait40].isAssignableFrom(cls))
    assertTrue("Trait40 isInstance", classOf[Trait40].isInstance(anyInstance))
    assertTrue("contains interface of Trait40", cls.getInterfaces().contains(classOf[Trait40]))

    assertTrue("isInstanceOf[Trait41]", anyInstance.isInstanceOf[Trait41])
    assertEquals("invoke[Trait41]", 1041, anyInstance.asInstanceOf[Trait41].invoke41())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait41].isAssignableFrom(cls))
    assertTrue("Trait41 isInstance", classOf[Trait41].isInstance(anyInstance))
    assertTrue("contains interface of Trait41", cls.getInterfaces().contains(classOf[Trait41]))

    assertTrue("isInstanceOf[Trait42]", anyInstance.isInstanceOf[Trait42])
    assertEquals("invoke[Trait42]", 1042, anyInstance.asInstanceOf[Trait42].invoke42())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait42].isAssignableFrom(cls))
    assertTrue("Trait42 isInstance", classOf[Trait42].isInstance(anyInstance))
    assertTrue("contains interface of Trait42", cls.getInterfaces().contains(classOf[Trait42]))

    assertTrue("isInstanceOf[Trait43]", anyInstance.isInstanceOf[Trait43])
    assertEquals("invoke[Trait43]", 1043, anyInstance.asInstanceOf[Trait43].invoke43())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait43].isAssignableFrom(cls))
    assertTrue("Trait43 isInstance", classOf[Trait43].isInstance(anyInstance))
    assertTrue("contains interface of Trait43", cls.getInterfaces().contains(classOf[Trait43]))

    assertTrue("isInstanceOf[Trait44]", anyInstance.isInstanceOf[Trait44])
    assertEquals("invoke[Trait44]", 1044, anyInstance.asInstanceOf[Trait44].invoke44())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait44].isAssignableFrom(cls))
    assertTrue("Trait44 isInstance", classOf[Trait44].isInstance(anyInstance))
    assertTrue("contains interface of Trait44", cls.getInterfaces().contains(classOf[Trait44]))

    assertTrue("isInstanceOf[Trait45]", anyInstance.isInstanceOf[Trait45])
    assertEquals("invoke[Trait45]", 1045, anyInstance.asInstanceOf[Trait45].invoke45())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait45].isAssignableFrom(cls))
    assertTrue("Trait45 isInstance", classOf[Trait45].isInstance(anyInstance))
    assertTrue("contains interface of Trait45", cls.getInterfaces().contains(classOf[Trait45]))

    assertTrue("isInstanceOf[Trait46]", anyInstance.isInstanceOf[Trait46])
    assertEquals("invoke[Trait46]", 1046, anyInstance.asInstanceOf[Trait46].invoke46())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait46].isAssignableFrom(cls))
    assertTrue("Trait46 isInstance", classOf[Trait46].isInstance(anyInstance))
    assertTrue("contains interface of Trait46", cls.getInterfaces().contains(classOf[Trait46]))

    assertTrue("isInstanceOf[Trait47]", anyInstance.isInstanceOf[Trait47])
    assertEquals("invoke[Trait47]", 1047, anyInstance.asInstanceOf[Trait47].invoke47())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait47].isAssignableFrom(cls))
    assertTrue("Trait47 isInstance", classOf[Trait47].isInstance(anyInstance))
    assertTrue("contains interface of Trait47", cls.getInterfaces().contains(classOf[Trait47]))

    assertTrue("isInstanceOf[Trait48]", anyInstance.isInstanceOf[Trait48])
    assertEquals("invoke[Trait48]", 1048, anyInstance.asInstanceOf[Trait48].invoke48())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait48].isAssignableFrom(cls))
    assertTrue("Trait48 isInstance", classOf[Trait48].isInstance(anyInstance))
    assertTrue("contains interface of Trait48", cls.getInterfaces().contains(classOf[Trait48]))

    assertTrue("isInstanceOf[Trait49]", anyInstance.isInstanceOf[Trait49])
    assertEquals("invoke[Trait49]", 1049, anyInstance.asInstanceOf[Trait49].invoke49())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait49].isAssignableFrom(cls))
    assertTrue("Trait49 isInstance", classOf[Trait49].isInstance(anyInstance))
    assertTrue("contains interface of Trait49", cls.getInterfaces().contains(classOf[Trait49]))

    assertTrue("isInstanceOf[Trait50]", anyInstance.isInstanceOf[Trait50])
    assertEquals("invoke[Trait50]", 1050, anyInstance.asInstanceOf[Trait50].invoke50())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait50].isAssignableFrom(cls))
    assertTrue("Trait50 isInstance", classOf[Trait50].isInstance(anyInstance))
    assertTrue("contains interface of Trait50", cls.getInterfaces().contains(classOf[Trait50]))

    assertTrue("isInstanceOf[Trait51]", anyInstance.isInstanceOf[Trait51])
    assertEquals("invoke[Trait51]", 1051, anyInstance.asInstanceOf[Trait51].invoke51())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait51].isAssignableFrom(cls))
    assertTrue("Trait51 isInstance", classOf[Trait51].isInstance(anyInstance))
    assertTrue("contains interface of Trait51", cls.getInterfaces().contains(classOf[Trait51]))

    assertTrue("isInstanceOf[Trait52]", anyInstance.isInstanceOf[Trait52])
    assertEquals("invoke[Trait52]", 1052, anyInstance.asInstanceOf[Trait52].invoke52())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait52].isAssignableFrom(cls))
    assertTrue("Trait52 isInstance", classOf[Trait52].isInstance(anyInstance))
    assertTrue("contains interface of Trait52", cls.getInterfaces().contains(classOf[Trait52]))

    assertTrue("isInstanceOf[Trait53]", anyInstance.isInstanceOf[Trait53])
    assertEquals("invoke[Trait53]", 1053, anyInstance.asInstanceOf[Trait53].invoke53())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait53].isAssignableFrom(cls))
    assertTrue("Trait53 isInstance", classOf[Trait53].isInstance(anyInstance))
    assertTrue("contains interface of Trait53", cls.getInterfaces().contains(classOf[Trait53]))

    assertTrue("isInstanceOf[Trait54]", anyInstance.isInstanceOf[Trait54])
    assertEquals("invoke[Trait54]", 1054, anyInstance.asInstanceOf[Trait54].invoke54())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait54].isAssignableFrom(cls))
    assertTrue("Trait54 isInstance", classOf[Trait54].isInstance(anyInstance))
    assertTrue("contains interface of Trait54", cls.getInterfaces().contains(classOf[Trait54]))

    assertTrue("isInstanceOf[Trait55]", anyInstance.isInstanceOf[Trait55])
    assertEquals("invoke[Trait55]", 1055, anyInstance.asInstanceOf[Trait55].invoke55())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait55].isAssignableFrom(cls))
    assertTrue("Trait55 isInstance", classOf[Trait55].isInstance(anyInstance))
    assertTrue("contains interface of Trait55", cls.getInterfaces().contains(classOf[Trait55]))

    assertTrue("isInstanceOf[Trait56]", anyInstance.isInstanceOf[Trait56])
    assertEquals("invoke[Trait56]", 1056, anyInstance.asInstanceOf[Trait56].invoke56())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait56].isAssignableFrom(cls))
    assertTrue("Trait56 isInstance", classOf[Trait56].isInstance(anyInstance))
    assertTrue("contains interface of Trait56", cls.getInterfaces().contains(classOf[Trait56]))

    assertTrue("isInstanceOf[Trait57]", anyInstance.isInstanceOf[Trait57])
    assertEquals("invoke[Trait57]", 1057, anyInstance.asInstanceOf[Trait57].invoke57())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait57].isAssignableFrom(cls))
    assertTrue("Trait57 isInstance", classOf[Trait57].isInstance(anyInstance))
    assertTrue("contains interface of Trait57", cls.getInterfaces().contains(classOf[Trait57]))

    assertTrue("isInstanceOf[Trait58]", anyInstance.isInstanceOf[Trait58])
    assertEquals("invoke[Trait58]", 1058, anyInstance.asInstanceOf[Trait58].invoke58())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait58].isAssignableFrom(cls))
    assertTrue("Trait58 isInstance", classOf[Trait58].isInstance(anyInstance))
    assertTrue("contains interface of Trait58", cls.getInterfaces().contains(classOf[Trait58]))

    assertTrue("isInstanceOf[Trait59]", anyInstance.isInstanceOf[Trait59])
    assertEquals("invoke[Trait59]", 1059, anyInstance.asInstanceOf[Trait59].invoke59())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait59].isAssignableFrom(cls))
    assertTrue("Trait59 isInstance", classOf[Trait59].isInstance(anyInstance))
    assertTrue("contains interface of Trait59", cls.getInterfaces().contains(classOf[Trait59]))

    assertTrue("isInstanceOf[Trait60]", anyInstance.isInstanceOf[Trait60])
    assertEquals("invoke[Trait60]", 1060, anyInstance.asInstanceOf[Trait60].invoke60())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait60].isAssignableFrom(cls))
    assertTrue("Trait60 isInstance", classOf[Trait60].isInstance(anyInstance))
    assertTrue("contains interface of Trait60", cls.getInterfaces().contains(classOf[Trait60]))

    assertTrue("isInstanceOf[Trait61]", anyInstance.isInstanceOf[Trait61])
    assertEquals("invoke[Trait61]", 1061, anyInstance.asInstanceOf[Trait61].invoke61())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait61].isAssignableFrom(cls))
    assertTrue("Trait61 isInstance", classOf[Trait61].isInstance(anyInstance))
    assertTrue("contains interface of Trait61", cls.getInterfaces().contains(classOf[Trait61]))

    assertTrue("isInstanceOf[Trait62]", anyInstance.isInstanceOf[Trait62])
    assertEquals("invoke[Trait62]", 1062, anyInstance.asInstanceOf[Trait62].invoke62())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait62].isAssignableFrom(cls))
    assertTrue("Trait62 isInstance", classOf[Trait62].isInstance(anyInstance))
    assertTrue("contains interface of Trait62", cls.getInterfaces().contains(classOf[Trait62]))

    assertTrue("isInstanceOf[Trait63]", anyInstance.isInstanceOf[Trait63])
    assertEquals("invoke[Trait63]", 1063, anyInstance.asInstanceOf[Trait63].invoke63())
    assertTrue("isAssignableFrom[Trait1]", classOf[Trait63].isAssignableFrom(cls))
    assertTrue("Trait63 isInstance", classOf[Trait63].isInstance(anyInstance))
    assertTrue("contains interface of Trait63", cls.getInterfaces().contains(classOf[Trait63]))
  }
}
