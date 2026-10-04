package org.scalanative.testsuite.javalib.util.stream

import java.util.function.{BiConsumer, BinaryOperator, Function, Supplier}
import java.util.stream.Collector.Characteristics
import java.util.stream.{Collector, Stream}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class CollectorFactoryTest {
  private val supplier: Supplier[StringBuilder] = () => new StringBuilder()
  private val accumulator: BiConsumer[StringBuilder, String] = (b, s) => {
    b.append(s)
    ()
  }
  private val combiner: BinaryOperator[StringBuilder] = (a, b) => a.append(b)
  private val finisher: Function[StringBuilder, String] = _.toString()

  @Test def identityFactory(): Unit = {
    val collector = Collector.of[String, StringBuilder](
      supplier,
      accumulator,
      combiner
    )
    assertSame(supplier, collector.supplier())
    assertSame(accumulator, collector.accumulator())
    assertSame(combiner, collector.combiner())
    assertEquals(1, collector.characteristics().size())
    assertTrue(
      collector.characteristics().contains(Characteristics.IDENTITY_FINISH)
    )
    val builder = collector.supplier().get()
    collector.accumulator().accept(builder, "a")
    val combined = collector.combiner().apply(builder, new StringBuilder("b"))
    assertSame(builder, combined)
    assertSame(builder, collector.finisher().apply(combined))
    assertEquals("ab", combined.toString())
  }

  @Test def finishingFactory(): Unit = {
    val collector = Collector.of[String, StringBuilder, String](
      supplier,
      accumulator,
      combiner,
      finisher
    )
    assertSame(finisher, collector.finisher())
    assertTrue(collector.characteristics().isEmpty())
    val stream = Stream.of("a", "b", "c")
    try assertEquals("abc", stream.collect(collector))
    finally stream.close()
  }

  @Test def characteristicsAreImmutableSnapshots(): Unit = {
    val flags = Array(Characteristics.UNORDERED, Characteristics.UNORDERED)
    val identity = Collector.of[String, StringBuilder](
      supplier,
      accumulator,
      combiner,
      flags: _*
    )
    val finishing = Collector.of[String, StringBuilder, String](
      supplier,
      accumulator,
      combiner,
      finisher,
      flags: _*
    )
    flags(0) = Characteristics.CONCURRENT
    flags(1) = Characteristics.CONCURRENT
    assertEquals(2, identity.characteristics().size())
    assertTrue(
      identity.characteristics().contains(Characteristics.IDENTITY_FINISH)
    )
    assertTrue(identity.characteristics().contains(Characteristics.UNORDERED))
    assertFalse(identity.characteristics().contains(Characteristics.CONCURRENT))
    assertEquals(1, finishing.characteristics().size())
    assertTrue(finishing.characteristics().contains(Characteristics.UNORDERED))
    assertFalse(
      finishing.characteristics().contains(Characteristics.CONCURRENT)
    )
    assertThrows(
      classOf[UnsupportedOperationException],
      identity.characteristics().clear()
    )
    assertThrows(
      classOf[UnsupportedOperationException],
      finishing.characteristics().add(Characteristics.CONCURRENT)
    )
  }

  @Test def identityFactoryRejectsNullComponents(): Unit = {
    assertThrows(
      classOf[NullPointerException],
      Collector.of[String, StringBuilder](null, accumulator, combiner)
    )
    assertThrows(
      classOf[NullPointerException],
      Collector.of[String, StringBuilder](supplier, null, combiner)
    )
    assertThrows(
      classOf[NullPointerException],
      Collector.of[String, StringBuilder](supplier, accumulator, null)
    )
  }

  @Test def finishingFactoryRejectsNullComponents(): Unit = {
    assertThrows(
      classOf[NullPointerException],
      Collector.of[String, StringBuilder, String](
        null,
        accumulator,
        combiner,
        finisher
      )
    )
    assertThrows(
      classOf[NullPointerException],
      Collector
        .of[String, StringBuilder, String](supplier, null, combiner, finisher)
    )
    assertThrows(
      classOf[NullPointerException],
      Collector.of[String, StringBuilder, String](
        supplier,
        accumulator,
        null,
        finisher
      )
    )
    assertThrows(
      classOf[NullPointerException],
      Collector.of[String, StringBuilder, String](
        supplier,
        accumulator,
        combiner,
        null
      )
    )
  }

  @Test def nullCharacteristicsRejected(): Unit = {
    val nullFlags: Array[Characteristics] = null
    assertThrows(
      classOf[NullPointerException],
      Collector.of[String, StringBuilder](
        supplier,
        accumulator,
        combiner,
        nullFlags: _*
      )
    )
    assertThrows(
      classOf[NullPointerException],
      Collector.of[String, StringBuilder, String](
        supplier,
        accumulator,
        combiner,
        finisher,
        nullFlags: _*
      )
    )
    assertThrows(
      classOf[NullPointerException],
      Collector.of[String, StringBuilder](
        supplier,
        accumulator,
        combiner,
        null: Characteristics
      )
    )
    assertThrows(
      classOf[NullPointerException],
      Collector.of[String, StringBuilder, String](
        supplier,
        accumulator,
        combiner,
        finisher,
        null: Characteristics
      )
    )
  }
}
