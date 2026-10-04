package org.scalanative.testsuite.javalib.util

import java.util.function.{DoubleConsumer, IntConsumer, LongConsumer}
import java.util.{
  DoubleSummaryStatistics, IntSummaryStatistics, LongSummaryStatistics
}

import org.junit.Assert._
import org.junit.Test

class SummaryStatisticsConsumerTest {
  @Test def intStatisticsAsIntConsumer(): Unit = {
    val statistics = new IntSummaryStatistics()
    val consumer = (statistics: AnyRef).asInstanceOf[IntConsumer]

    consumer.accept(Int.MinValue)
    consumer.accept(Int.MaxValue)

    assertEquals(2L, statistics.getCount())
    assertEquals(-1L, statistics.getSum())
    assertEquals(Int.MinValue, statistics.getMin())
    assertEquals(Int.MaxValue, statistics.getMax())
  }

  @Test def doubleStatisticsAsDoubleConsumer(): Unit = {
    val statistics = new DoubleSummaryStatistics()
    val consumer = (statistics: AnyRef).asInstanceOf[DoubleConsumer]
    var observed = 0.0

    consumer.accept(-1.5)
    consumer.andThen((value: Double) => observed = value).accept(2.25)

    assertEquals(2L, statistics.getCount())
    assertEquals(0.75, statistics.getSum(), 0.0)
    assertEquals(-1.5, statistics.getMin(), 0.0)
    assertEquals(2.25, statistics.getMax(), 0.0)
    assertEquals(2.25, observed, 0.0)
  }

  @Test def longStatisticsAsLongConsumer(): Unit = {
    val statistics = new LongSummaryStatistics()
    val consumer = (statistics: AnyRef).asInstanceOf[LongConsumer]
    val largerThanInt = Int.MaxValue.toLong + 1L

    consumer.accept(largerThanInt)
    consumer.accept(-3L)

    assertEquals(2L, statistics.getCount())
    assertEquals(largerThanInt - 3L, statistics.getSum())
    assertEquals(-3L, statistics.getMin())
    assertEquals(largerThanInt, statistics.getMax())
  }

  @Test def longStatisticsAsIntConsumer(): Unit = {
    val statistics = new LongSummaryStatistics()
    val consumer = (statistics: AnyRef).asInstanceOf[IntConsumer]

    consumer.accept(Int.MinValue)
    consumer.accept(Int.MaxValue)

    assertEquals(2L, statistics.getCount())
    assertEquals(-1L, statistics.getSum())
    assertEquals(Int.MinValue.toLong, statistics.getMin())
    assertEquals(Int.MaxValue.toLong, statistics.getMax())
  }
}
