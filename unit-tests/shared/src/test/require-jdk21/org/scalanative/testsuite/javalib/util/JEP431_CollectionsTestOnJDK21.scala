package org.scalanative.testsuite.javalib.util

/* The various JEP431_*TestOnJDK21 file are related and may overlap but
 * they have distinct primary intents.
 *
 * JEP431_CollectionsTestOnJDK21 tests methods declared in
 * Collections.scala and may use methods from SequencedCollection,
 * SequencedMap, SequencedSet.
 *
 * Other JEP431_*TestOnJDK21 files exercise concrete instances, such as
 * JEP431_ArrayListTestOnJDK21.scala
 */

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

/* Keep import environment minimal. Import only the classes we want &
 * knowingly use.
 * Fewer chances for hidden and/or unintended interactions.
 */

import java.util.{Arrays, ArrayList, List}
import java.util.Collections
import java.util.ArrayDeque
import java.util.{LinkedHashMap, Map, TreeMap}
import java.util.TreeSet

class JEP431_CollectionsTestOnJDK21 {
  // format: off

  val months = List.of(
    "janvier", "février",  "mars",
    "avril",   "mai",      "juin",
    "juillet", "août",     "septembre",
    "octobre", "november", "décembre"
  )

  // format: on

  @Test def asLifoQueue(): Unit = {

    val expectedDequeLast = "mineral"
    val deque = new ArrayDeque(
      List.of("animal", "vegatable", expectedDequeLast)
    )

    val baseSize = deque.size()

    val expected_1 = "quintessence"
    val expected_2 = "ether"

    val lifoQueue = Collections.asLifoQueue(deque)

    assertEquals("lifoQueue size", baseSize, lifoQueue.size())

    assertTrue("lifoQueue add", lifoQueue.add(expected_1))
    assertEquals("lifoQueue add size", baseSize + 1, lifoQueue.size())

    assertEquals("deque first, add", expected_1, deque.getFirst())

    assertEquals(
      "unchanged deque tail, add",
      expectedDequeLast,
      deque.getLast()
    )

    assertTrue("lifoQueue offer", lifoQueue.offer(expected_2))
    assertEquals("lifoQueue offer size", baseSize + 2, lifoQueue.size())
    assertEquals("deque first, offer", expected_2, deque.getFirst())

    assertEquals(
      "unchanged deque tail, offer",
      expectedDequeLast,
      deque.getLast()
    )

    assertEquals("lifoQueue element, 1", expected_2, lifoQueue.element())
    assertEquals("lifoQueue remove 1", expected_2, lifoQueue.remove())
    assertEquals("lifoQueue element, 2", expected_1, lifoQueue.peek())

    assertEquals("lifoQueue remove 2", expected_1, lifoQueue.poll())
    assertEquals("lifoQueue element, 2", deque.getFirst(), lifoQueue.element())

    val animals = List.of("dog", "cat", "mountain lion")
    lifoQueue.addAll(animals)

    assertEquals("addAllsize", baseSize + animals.size(), lifoQueue.size())

    // Was the collection added LIFO?
    for (j <- animals.size() - 1 to 0 by -1) {
      assertEquals(s"animal, ${j}", lifoQueue.poll(), animals.get(j))
    }

    lifoQueue.clear()
    assertEquals("cleared deque size", 0, deque.size())
  }

  @Test def unmodifiableSequencedCollection(): Unit = {
    val expectedSize = 3

    val charites = List.of("Aglaea", "Euphrosyne", "Thalia")

    // start with a modifiable source.
    val srcCol = new ArrayList[String](charites)

    val unmodSeqCol = Collections.unmodifiableSequencedCollection(srcCol)
    assertEquals("unmodSeqCol size", expectedSize, unmodSeqCol.size())

    // The two are not reference equal
    assertFalse("reference .eq", unmodSeqCol.eq(srcCol))

    // but are content equal.
    val unmodIter = unmodSeqCol.iterator()
    for (j <- 0 until srcCol.size())
      assertEquals("content(${j}) equals", srcCol.get(j), unmodIter.next())

    assertFalse("unmodIter.hasNext()", unmodIter.hasNext())

    // Methods declared in Collections which should not modify.

    assertThrows(
      "unmodifiableSequencedCollection#add",
      classOf[UnsupportedOperationException],
      unmodSeqCol.add("Styx")
    )

    assertThrows(
      "unmodifiableSequencedCollection#addAll",
      classOf[UnsupportedOperationException],
      unmodSeqCol.addAll(List.of("a", "b", "c"))
    )

    assertThrows(
      "unmodifiableSequencedCollection#clear",
      classOf[UnsupportedOperationException],
      unmodSeqCol.clear()
    )

    val unmodIter_2 = unmodSeqCol.iterator()
    val discarded = unmodIter_2.next()

    assertThrows(
      "unmodifiableSequencedCollection#iterator#remove",
      classOf[UnsupportedOperationException],
      unmodIter_2.remove()
    )

    assertThrows(
      "unmodifiableSequencedCollection#remove",
      classOf[UnsupportedOperationException],
      unmodSeqCol.remove("a")
    )

    assertThrows(
      "unmodifiableSequencedCollection#removeAll",
      classOf[UnsupportedOperationException],
      unmodSeqCol.removeAll(List.of("a", "b", "c"))
    )

    assertThrows(
      "unmodifiableSequencedCollection#removeIf",
      classOf[UnsupportedOperationException],
      unmodSeqCol.removeIf(e => (e == charites.get(1)))
    )

    assertThrows(
      "unmodifiableSequencedCollection#retainAll",
      classOf[UnsupportedOperationException],
      unmodSeqCol.retainAll(List.of("a", "b", "c"))
    )

    // SequencedCollection methods introduced in JEP 431.

    assertThrows(
      "unmodifiableSequencedCollection#addFirst",
      classOf[UnsupportedOperationException],
      unmodSeqCol.addFirst("Hephaestus")
    )

    assertThrows(
      "unmodifiableSequencedCollection#addLast",
      classOf[UnsupportedOperationException],
      unmodSeqCol.addLast("Hephaestus")
    )

    assertEquals(
      "unmodifiableSequencedCollection#getFirst",
      srcCol.getFirst(),
      unmodSeqCol.getFirst()
    )

    assertEquals(
      "unmodifiableSequencedCollection#getLast",
      srcCol.getLast(),
      unmodSeqCol.getLast()
    )

    assertThrows(
      "unmodifiableSequencedCollection#removeFirst",
      classOf[UnsupportedOperationException],
      unmodSeqCol.removeFirst()
    )

    assertThrows(
      "unmodifiableSequencedCollection#removeLast",
      classOf[UnsupportedOperationException],
      unmodSeqCol.removeLast()
    )

    val reversedUnmodSeqCol = unmodSeqCol.reversed()
    assertThrows(
      "unmodifiableSequencedCol reversed().clear()",
      classOf[UnsupportedOperationException],
      reversedUnmodSeqCol.clear()
    )

    // probe a second method. Someday all methods should be exercised.
    assertThrows(
      "unmodifiableSequencedCol reversed().addFirst()",
      classOf[UnsupportedOperationException],
      reversedUnmodSeqCol.addFirst("a")
    )

    /*  JDK 24 JavaDoc for SequencedCollection imposes no requirement
     *  that a result from reversing twice be reference equal (.eq) to
     *  the original, now underlying, Collection.
     *
     *  SequencedCollection uses the .equals() from Collection, which is
     *  essentially a reference equality test. Since the single and twice
     *  reversed collections are not .eq, they can not, without an override,
     *  be .equals.
     *
     *  Since that is a mildly astonishing result. Sub-classes will usually
     *  override equals() & hashCode(). Meaning they need explict tests for
     *  those.
     */

    val twiceReversedUnmodSeqCol = reversedUnmodSeqCol.reversed()
    assertFalse(
      "unmodifiableSequencedCol twice reversed: reference equality",
      twiceReversedUnmodSeqCol.eq(unmodSeqCol)
    )

    assertFalse(
      "unmodifiableSequencedCol twice reversed: content equality",
      twiceReversedUnmodSeqCol.equals(unmodSeqCol)
    )
  }

  @Test def unmodifiableSequencedSet(): Unit = {
    val expectedSize = 3

    // start with a modifiable source.
    val charites = new TreeSet[String]()
    charites.add("Aglaea")
    charites.add("Euphrosyne")
    charites.add("Thalia")

    val unmodSeqSet = Collections.unmodifiableSequencedSet(charites)
    assertEquals("set size", expectedSize, unmodSeqSet.size())

    // Methods declared in Collections which should not modify.

    assertThrows(
      "unmodifiableSequencedSet#removeIf",
      classOf[UnsupportedOperationException],
      unmodSeqSet.removeIf(e => (e == charites.last()))
    )

    // Methods declared in Set which should not modify.

    assertThrows(
      "unmodifiableSequencedSet#add",
      classOf[UnsupportedOperationException],
      unmodSeqSet.add("Styx")
    )

    assertThrows(
      "unmodifiableSequencedSet#addAll",
      classOf[UnsupportedOperationException],
      unmodSeqSet.addAll(List.of("a", "b", "c"))
    )

    assertThrows(
      "unmodifiableSequencedSet#clear",
      classOf[UnsupportedOperationException],
      unmodSeqSet.clear()
    )

    val unmodIter_2 = unmodSeqSet.iterator()
    val discarded = unmodIter_2.next()

    // Sneak-path modification via iterator must be rejected.
    assertThrows(
      "unmodifiableSequencedSet#iterator#remove",
      classOf[UnsupportedOperationException],
      unmodIter_2.remove()
    )

    assertThrows(
      "unmodifiableSequencedSet#remove",
      classOf[UnsupportedOperationException],
      unmodSeqSet.remove("a")
    )

    assertThrows(
      "unmodifiableSequencedSet#removeAll",
      classOf[UnsupportedOperationException],
      unmodSeqSet.removeAll(List.of("a", "b", "c"))
    )

    assertThrows(
      "unmodifiableSequencedSet#removeIf",
      classOf[UnsupportedOperationException],
      unmodSeqSet.removeIf(e => (e == charites.first()))
    )

    assertThrows(
      "unmodifiableSequencedSet#retainAll",
      classOf[UnsupportedOperationException],
      unmodSeqSet.retainAll(List.of("a", "b", "c"))
    )

    // SequencedSet methods introduced in JEP 431. partial coverage.

    assertEquals(
      "unmodifiableSequencedSet#getFirst",
      charites.first(),
      unmodSeqSet.getFirst()
    )

    assertThrows(
      "unmodifiableSequencedSet#addFirst",
      classOf[UnsupportedOperationException],
      unmodSeqSet.addFirst("Hephaestus")
    )

    assertThrows(
      "unmodifiableSequencedSet#removeFirst",
      classOf[UnsupportedOperationException],
      unmodSeqSet.removeFirst()
    )

    assertThrows(
      "unmodifiableSequencedSet#removeLast",
      classOf[UnsupportedOperationException],
      unmodSeqSet.removeLast()
    )

    val reversedUnmodSeqSet = unmodSeqSet.reversed()
    assertThrows(
      "unmodifiableSequencedSet reversed().clear()",
      classOf[UnsupportedOperationException],
      reversedUnmodSeqSet.clear()
    )

    /* SequencedSet overrides the strict reference equality of
     * Collections.equals() to provide content equality.
     * 
     * Reference equality is allowed but not required and not JVM 24 practice.
     */

    val twiceReversedUnmodSeqSet = reversedUnmodSeqSet.reversed()
    assertTrue(
      "unmodifiableSequencedSet twice reversed: content equality",
      twiceReversedUnmodSeqSet.equals(unmodSeqSet)
    )
  }
}
