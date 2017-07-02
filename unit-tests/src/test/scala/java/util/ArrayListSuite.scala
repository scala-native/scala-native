package java.util

import scala.collection.JavaConverters._

object ArrayListSuite extends tests.Suite {
  test("Constructor()") {
    val al = new ArrayList()
    assert(al.size() == 0)
    assert(al.isEmpty())
  }

  test("Constructor(Int)") {
    val al = new ArrayList(10)
    assert(al.size() == 0)
    assert(al.isEmpty())
    // the capacity is opaque. exposing it just for testing is avoided
  }

  test("Constructor(Collection[java.lang.Integer])") { // for AnyVal
    val is = Seq(1, 2, 3)
    val al = new ArrayList(is.asJava)
    assert(al.size() == 3)
    assert(!al.isEmpty())
    assert(al.get(0) == 1)
    assert(al.get(1) == 2)
    assert(al.get(2) == 3)
    assert(al.asScala == Seq(1, 2, 3))
  }

  test("Constructor(Collection[String])") { // for AnyRef
    val is = Seq(1, 2, 3).map(_.toString)
    import scala.collection.JavaConverters._
    val al = new ArrayList(is.asJava)
    assert(al.size() == 3)
    assert(!al.isEmpty())
    assert(al.get(0) == "1")
    assert(al.get(1) == "2")
    assert(al.get(2) == "3")
    assert(al.asScala == Seq("1", "2", "3"))
  }

  test("Constructor(null) throws NullPointerException") {
    assertThrows[NullPointerException] {
      new ArrayList(null)
    }
  }

  test("equals() for empty lists") {
    val e1  = new ArrayList()
    val e2  = new ArrayList()
    val ne1 = new ArrayList(Seq(1).asJava)
    assert(e1 == e2)
    assert(e2 == e1)
    assert(e1 != ne1)
    assert(ne1 != e1)
  }

  test("equals() for nonempty lists") {
    val ne1a = new ArrayList(Seq(1, 2, 3).asJava)
    val ne1b = new ArrayList(Seq(1, 2, 3).asJava)
    val ne2  = new ArrayList(Seq(1).asJava)
    assert(ne1a == ne1b)
    assert(ne1b == ne1a)
    assert(ne1a != ne2)
    assert(ne2 != ne1a)
    assert(ne1b != ne2)
    assert(ne2 != ne1b)
  }

  test("trimToSize() for non-empty lists with different capacities") {
    val al1 = new ArrayList(Seq(1, 2, 3).asJava)
    val al2 = new ArrayList(Seq(1, 2, 3).asJava)
    al2.ensureCapacity(100)
    val al3 = new ArrayList[Int](50)
    al3.add(1)
    al3.add(2)
    al3.add(3)
    assert(al1 == al2)
    assert(al2 == al3)
  }

  test("trimToSize() for empty lists") {
    val al1 = new ArrayList()
    al1.trimToSize()
    val al2 = new ArrayList()
    assert(al1 == al2)
  }

  test("trimToSize() for non-empty lists") {
    val al1 = new ArrayList(Seq(1, 2, 3).asJava)
    val al2 = new ArrayList(Seq(1, 2, 3).asJava)
    al2.ensureCapacity(100)
    val al3 = new ArrayList[Int](50)
    al3.add(1)
    al3.add(2)
    al3.add(3)
    al1.trimToSize()
    al2.trimToSize()
    al3.trimToSize()
    assert(al1 == al2)
    assert(al2 == al3)
  }

  test("size()") {
    val al1 = new ArrayList[Int]()
    assert(al1.size() == 0)
    val al2 = new ArrayList[Int](Seq(1, 2, 3).asJava)
    assert(al2.size() == 3)
    val al3 = new ArrayList[Int](10)
    // not to be confused with its capacity.
    assert(al3.size() == 0)
  }

  test("isEmpty()") {
    val al1 = new ArrayList[Int]()
    assert(al1.isEmpty())
    val al2 = new ArrayList[Int](Seq(1, 2, 3).asJava)
    assert(!al2.isEmpty())
    val al3 = new ArrayList[Int](10)
    // not to be confused with its capacity.
    assert(al3.isEmpty())
  }

  test("indexOf(Any)") {
    val al1 = new ArrayList[Int](Seq(1, 2, 3, 2).asJava)
    assert(al1.indexOf(2) == 1)
  }

  test("lastIndexOf(Any)") {
    val al1 = new ArrayList[Int](Seq(1, 2, 3, 2).asJava)
    assert(al1.lastIndexOf(2) == 3)
  }

  test("clone()") {
    val al1 = new ArrayList[Int](Seq(1, 2, 3, 2).asJava)
    val al2 = al1.clone().asInstanceOf[ArrayList[Int]]
    assert(al1 == al2)
    al1.add(1)
    assert(al1 != al2)
    al2.add(1)
    assert(al1 == al2)
  }

  test("clone() with size() != capacity()") {
    val al1 = new ArrayList[Int](Seq(1, 2, 3, 2).asJava)
    al1.ensureCapacity(20)
    val al2 = al1.clone().asInstanceOf[ArrayList[Int]]
    assert(al1 == al2)
    al1.add(1)
    assert(al1 != al2)
    al2.add(1)
    assert(al1 == al2)
  }

  test("toArray()") {
    val al1 = new ArrayList[Int](Seq(1, 2, 3, 2).asJava)
    assert(
      (Array(1, 2, 3, 2).map(_.asInstanceOf[AnyRef])) sameElements
        (al1.toArray()))
  }

  test("toArray[T](arr: Array[T]) when arr is shorter") {
    val al1  = new ArrayList[String](Seq("apple", "banana", "cherry").asJava)
    val ain  = Array.empty[String]
    val aout = al1.toArray(ain)
    assert(ain ne aout)
    assert(Array("apple", "banana", "cherry") sameElements aout)
  }

  test("toArray[T](arr: Array[T]) when arr is with the same length or longer") {
    val al1  = new ArrayList[String](Seq("apple", "banana", "cherry").asJava)
    val ain  = Array.fill(4)("foo")
    val aout = al1.toArray(ain)
    assert(ain eq aout)
    assert(Array("apple", "banana", "cherry", null) sameElements aout)
  }

  test("Array[E].toArray[T](Array[T]) when T >: E") {
    class SuperClass
    class SubClass extends SuperClass
    val in   = Seq.fill(2)(new SubClass)
    val al1  = new ArrayList[SubClass](in.asJava)
    val aout = al1.toArray(Array.empty[SuperClass])
    assert(in.toArray sameElements aout)
  }

  testFails(
    "Array[E].toArray[T](Array[T]) should throw ArrayStoreException when not T >: E",
    858) {
    class NotSuperClass
    class SubClass
    val al1 = new ArrayList[SubClass]()
    assertThrows[ArrayStoreException] {
      al1.toArray(Array.empty[NotSuperClass])
    }
  }

  test("toArray[T](null) throws null") {
    val al1 = new ArrayList[String](Seq("apple", "banana", "cherry").asJava)
    assertThrows[NullPointerException] { al1.toArray(null) }
  }

  test("get(Int)") {
    val al1 = new ArrayList[Int](Seq(1, 2, 3, 2).asJava)
    assert(al1.get(0) == 1)
    assert(al1.get(1) == 2)
    assert(al1.get(2) == 3)
    assert(al1.get(3) == 2)
    assertThrows[IndexOutOfBoundsException] { al1.get(-1); () }
    assertThrows[IndexOutOfBoundsException] { al1.get(4); () }
  }

  test("set(Int, E)") {
    val al1 = new ArrayList[Int](Seq(1, 2, 3, 2).asJava)
    assert(al1.set(1, 4) == 2)
    assert(Seq(1, 4, 3, 2) == al1.asScala)
  }

  test("add(E)") {
    val al1 = new ArrayList[Int]()
    al1.add(1)
    assert(al1.asScala == Seq(1))
    al1.add(2)
    assert(al1.asScala == Seq(1, 2))
  }

  test("add(Int, E)") {
    val al1 = new ArrayList[Int]()
    al1.add(0, 1)
    assert(al1.asScala == Seq(1))
    al1.add(0, 2)
    assert(al1.asScala == Seq(2, 1))
  }

  test("add(Int, E) when the capacity has to be expanded") {
    val al1 = new ArrayList[Int](0)
    al1.add(0, 1)
    assert(al1.asScala == Seq(1))
    al1.add(0, 2)
    assert(al1.asScala == Seq(2, 1))
  }

  test("remove(Int)") {
    val al1 = new ArrayList[Int](Seq(1, 2, 3, 2, 3).asJava)
    // remove last
    assert(al1.remove(4) == 3)
    // remove head
    assert(al1.remove(0) == 1)
    // remove middle
    assert(al1.remove(1) == 3)
    assertThrows[IndexOutOfBoundsException](al1.remove(4))
    assert(Seq(2, 2) == al1.asScala)
  }

  test("remove(Any)") {
    val al1 = new ArrayList[Int](Seq(1, 2, 3, 2).asJava)
    assert(al1.remove(2: Any) == true)
    assert(Seq(1, 3, 2) == al1.asScala)
    assert(al1.remove(4: Any) == false)
    assert(Seq(1, 3, 2) == al1.asScala)
  }

  test("addAll") {
    val l = new java.util.ArrayList[String]()
    l.add("First")
    l.add("Second")
    val l2 = new java.util.ArrayList[String]()
    l2.addAll(0, l)
    val iter = l2.iterator()
    assert(iter.next() == "First")
    assert(iter.next() == "Second")
    assert(!iter.hasNext())
  }

  test("clear()") {
    val al1 = new ArrayList[Int](Seq(1, 2, 3, 2).asJava)
    al1.clear()
    assert(al1.isEmpty())
    // makes sure that clear()ing an already empty list is safe
    al1.clear()
  }

  test("should throw an error with negative initial capacity") {
    assertThrows[IllegalArgumentException] {
      new ArrayList(-1)
    }
  }
}
