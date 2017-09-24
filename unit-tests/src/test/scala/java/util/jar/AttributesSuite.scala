package java.util.jar

// Ported from Apache Harmony

import java.util.HashSet

object AttributesSuite extends tests.Suite {

  test("Constructor(Attributes)") {
    setUp()
    val a2 = new Attributes(a)
    assert(a == a2)
    a.putValue("1", "one(1)")
    assert(a != a2)
  }

  test("clear()") {
    setUp()
    a.clear()
    assert(a.get("1") == null)
    assert(a.get("2") == null)
    assert(a.get("3") == null)
    assert(a.get("4") == null)
    assert(!a.containsKey("1"))
  }

  test("containsKey(Object)") {
    setUp()
    assert(!a.containsKey(new Integer(1)))
    assert(!a.containsKey("0"))
    assert(a.containsKey(new Attributes.Name("1")))
  }

  test("containsValue(Object)") {
    setUp()
    assert(!a.containsValue("One"))
    assert(a.containsValue("one"))
  }

  test("entrySet()") {
    setUp()
    val entrySet = a.entrySet()
    val keySet   = new HashSet[Object]()
    val valueSet = new HashSet[Object]()
    assert(entrySet.size() == 4)

    val i = entrySet.iterator()
    while (i.hasNext()) {
      val e = i.next()
      keySet.add(e.getKey())
      valueSet.add(e.getValue())
    }

    assert(valueSet.size() == 4)
    assert(valueSet.contains("one"))
    assert(valueSet.contains("two"))
    assert(valueSet.contains("three"))
    assert(valueSet.contains("four"))

    assert(keySet.size() == 4)
    assert(keySet.contains(new Attributes.Name("1")))
    assert(keySet.contains(new Attributes.Name("2")))
    assert(keySet.contains(new Attributes.Name("3")))
    assert(keySet.contains(new Attributes.Name("4")))
  }

  test("get(Object)") {
    setUp()
    assert(a.getValue("1") == "one")
    assert(a.getValue("0") == null)
  }

  test("isEmpty()") {
    setUp()
    assert(!a.isEmpty())
    a.clear()
    assert(a.isEmpty())
    a = new Attributes()
    assert(a.isEmpty())
  }

  test("keySet()") {
    setUp()
    val s = a.keySet()
    assert(s.size() == 4)
    assert(s.contains(new Attributes.Name("1")))
    assert(s.contains(new Attributes.Name("2")))
    assert(s.contains(new Attributes.Name("3")))
    assert(s.contains(new Attributes.Name("4")))
  }

  test("putAll(Map)") {
    setUp()
    val b = new Attributes()
    b.putValue("3", "san")
    b.putValue("4", "shi")
    b.putValue("5", "go")
    b.putValue("6", "roku")
    a.putAll(b.asInstanceOf[java.util.Map[_, _]])
    assert(a.getValue("1") == "one")
    assert(a.getValue("3") == "san")
    assert(a.getValue("5") == "go")

    val atts = new Attributes()
    assert(atts.put(Attributes.Name.CLASS_PATH, "tools.jar") == null)
    assert(atts.put(Attributes.Name.MANIFEST_VERSION, "1") == null)

    val atts2 = new Attributes()
    atts2.putAll(atts.asInstanceOf[java.util.Map[_, _]])
    assert(atts2.get(Attributes.Name.CLASS_PATH) == "tools.jar")
    assert(atts2.get(Attributes.Name.MANIFEST_VERSION) == "1")

    assertThrows[ClassCastException] {
      atts.putAll(java.util.Collections.EMPTY_MAP)
    }
  }

  test("remove(Object)") {
    setUp()
    a.remove(new Attributes.Name("1"))
    a.remove(new Attributes.Name("3"))
    assert(a.getValue("1") == null)
    assert(a.getValue("4") == "four")
  }

  test("size()") {
    setUp()
    assert(a.size() == 4)
    a.clear()
    assert(a.size() == 0)
  }

  test("values()") {
    setUp()
    val valueCollection = a.values()
    assert(valueCollection.size() == 4)
    assert(valueCollection.contains("one"))
    assert(valueCollection.contains("two"))
    assert(valueCollection.contains("three"))
    assert(valueCollection.contains("four"))
  }

  test("clone()") {
    setUp()
    val a2 = a.clone()
    assert(a == a2)
    a.putValue("1", "one(1)")
    assert(a != a2)
  }

  test("equals(Object)") {
    val n1 = new Attributes.Name("name")
    val n2 = new Attributes.Name("Name")
    assert(n1 == n2)
    val a1 = new Attributes()
    a1.putValue("one", "1")
    a1.putValue("two", "2")
    var a2 = new Attributes()
    a2.putValue("One", "1")
    a2.putValue("Two", "2")
    assert(a1 == a2)
    assert(a1 == a1)
    a2 = null
    assert(a1 != a2)
  }

  test("put()") {
    val attribute = new Attributes()
    assert(!attribute.containsKey(null))
    assert(!attribute.containsValue(null))
    attribute.put(null, null)
    attribute.put(null, null)
    assert(1 == attribute.size())
    assert(attribute.containsKey(null))
    assert(attribute.containsValue(null))
    assert(attribute.get(null) == null)

    val value = "It's null"
    attribute.put(null, value)
    assert(1 == attribute.size())
    assert(attribute.get(null) == value)

    val name = new Attributes.Name("null")
    attribute.put(name, null)
    assert(2 == attribute.size())
    assert(attribute.get(name) == null)
  }

  test("hashCode()") {
    val mockAttr = new MockAttributes()
    mockAttr.putValue("1", "one")
    assert(mockAttr.getMap().hashCode() == mockAttr.hashCode())
  }

  private class MockAttributes extends Attributes {
    def getMap(): java.util.Map[Object, Object] =
      map
  }

  private var a: Attributes = null

  private def setUp(): Unit = {
    a = new Attributes()
    a.putValue("1", "one")
    a.putValue("2", "two")
    a.putValue("3", "three")
    a.putValue("4", "four")
  }
}
