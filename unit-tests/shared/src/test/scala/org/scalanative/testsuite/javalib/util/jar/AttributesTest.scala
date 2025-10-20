package org.scalanative.testsuite.javalib.util.jar

// Ported from Apache Harmony

import java.util.jar.*
import java.util.HashSet

import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class AttributesTest {
  private var a: Attributes = null

  @Before
  def setUp(): Unit = {
    a = new Attributes()
    a.putValue("1", "one")
    a.putValue("2", "two")
    a.putValue("3", "three")
    a.putValue("4", "four")
  }

  @Test def constructorAttributes(): Unit = {
    val a2 = new Attributes(a)
    assertTrue(a == a2)
    a.putValue("1", "one(1)")
    assertTrue(a != a2)
  }

  @Test def clear(): Unit = {
    a.clear()
    assertTrue(a.get("1") == null)
    assertTrue(a.get("2") == null)
    assertTrue(a.get("3") == null)
    assertTrue(a.get("4") == null)
    assertFalse(a.containsKey("1"))
  }

  @deprecated @Test def containsKeyObject(): Unit = {
    assertFalse(a.containsKey(new Integer(1)))
    assertFalse(a.containsKey("0"))
    assertTrue(a.containsKey(new Attributes.Name("1")))
  }

  @Test def containsValueObject(): Unit = {
    assertFalse(a.containsValue("One"))
    assertTrue(a.containsValue("one"))
  }

  @Test def entrySet(): Unit = {
    val entrySet = a.entrySet()
    val keySet = new HashSet[Object]()
    val valueSet = new HashSet[Object]()
    assertTrue(entrySet.size() == 4)

    val i = entrySet.iterator()
    while (i.hasNext()) {
      val e = i.next()
      keySet.add(e.getKey())
      valueSet.add(e.getValue())
    }

    assertTrue(valueSet.size() == 4)
    assertTrue(valueSet.contains("one"))
    assertTrue(valueSet.contains("two"))
    assertTrue(valueSet.contains("three"))
    assertTrue(valueSet.contains("four"))

    assertTrue(keySet.size() == 4)
    assertTrue(keySet.contains(new Attributes.Name("1")))
    assertTrue(keySet.contains(new Attributes.Name("2")))
    assertTrue(keySet.contains(new Attributes.Name("3")))
    assertTrue(keySet.contains(new Attributes.Name("4")))
  }

  @Test def getObject(): Unit = {
    assertTrue(a.getValue("1") == "one")
    assertTrue(a.getValue("0") == null)
  }

  @Test def isEmpty(): Unit = {
    assertFalse(a.isEmpty())
    a.clear()
    assertTrue(a.isEmpty())
    a = new Attributes()
    assertTrue(a.isEmpty())
  }

  @Test def keySet(): Unit = {
    val s = a.keySet()
    assertTrue(s.size() == 4)
    assertTrue(s.contains(new Attributes.Name("1")))
    assertTrue(s.contains(new Attributes.Name("2")))
    assertTrue(s.contains(new Attributes.Name("3")))
    assertTrue(s.contains(new Attributes.Name("4")))
  }

  @Test def putAllMap(): Unit = {
    val b = new Attributes()
    b.putValue("3", "san")
    b.putValue("4", "shi")
    b.putValue("5", "go")
    b.putValue("6", "roku")
    a.putAll(b.asInstanceOf[java.util.Map[?, ?]])
    assertTrue(a.getValue("1") == "one")
    assertTrue(a.getValue("3") == "san")
    assertTrue(a.getValue("5") == "go")

    val atts = new Attributes()
    assertTrue(atts.put(Attributes.Name.CLASS_PATH, "tools.jar") == null)
    assertTrue(atts.put(Attributes.Name.MANIFEST_VERSION, "1") == null)

    val atts2 = new Attributes()
    atts2.putAll(atts.asInstanceOf[java.util.Map[?, ?]])
    assertTrue(atts2.get(Attributes.Name.CLASS_PATH) == "tools.jar")
    assertTrue(atts2.get(Attributes.Name.MANIFEST_VERSION) == "1")

    assertThrows(
      classOf[ClassCastException],
      atts.putAll(java.util.Collections.EMPTY_MAP)
    )
  }

  @Test def removeObject(): Unit = {
    a.remove(new Attributes.Name("1"))
    a.remove(new Attributes.Name("3"))
    assertTrue(a.getValue("1") == null)
    assertTrue(a.getValue("4") == "four")
  }

  @Test def size(): Unit = {
    assertTrue(a.size() == 4)
    a.clear()
    assertTrue(a.size() == 0)
  }

  @Test def values(): Unit = {
    val valueCollection = a.values()
    assertTrue(valueCollection.size() == 4)
    assertTrue(valueCollection.contains("one"))
    assertTrue(valueCollection.contains("two"))
    assertTrue(valueCollection.contains("three"))
    assertTrue(valueCollection.contains("four"))
  }

  @Test def testClone(): Unit = {
    val a2 = a.clone()
    assertTrue(a == a2)
    a.putValue("1", "one(1)")
    assertTrue(a != a2)
  }

  @Test def equalsObject(): Unit = {
    val n1 = new Attributes.Name("name")
    val n2 = new Attributes.Name("Name")
    assertTrue(n1 == n2)
    val a1 = new Attributes()
    a1.putValue("one", "1")
    a1.putValue("two", "2")
    var a2 = new Attributes()
    a2.putValue("One", "1")
    a2.putValue("Two", "2")
    assertTrue(a1 == a2)
    assertTrue(a1 == a1)
    a2 = null
    assertTrue(a1 != a2)
  }

  @Test def put(): Unit = {
    val attribute = new Attributes()
    assertFalse(attribute.containsKey(null))
    assertFalse(attribute.containsValue(null))
    attribute.put(null, null)
    attribute.put(null, null)
    assertTrue(1 == attribute.size())
    assertTrue(attribute.containsKey(null))
    assertTrue(attribute.containsValue(null))
    assertTrue(attribute.get(null) == null)

    val value = "It's null"
    attribute.put(null, value)
    assertTrue(1 == attribute.size())
    assertTrue(attribute.get(null) == value)

    val name = new Attributes.Name("null")
    attribute.put(name, null)
    assertTrue(2 == attribute.size())
    assertTrue(attribute.get(name) == null)
  }

  @Test def testHashCode(): Unit = {
    val mockAttr = new MockAttributes()
    mockAttr.putValue("1", "one")
    assertTrue(mockAttr.getMap().hashCode() == mockAttr.hashCode())
  }

  private class MockAttributes extends Attributes {
    def getMap(): java.util.Map[Object, Object] = map
  }
}
