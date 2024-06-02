package org.scalanative.testsuite.runtime

import org.junit.Test
import org.junit.Assert._
import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class NullGuardsTest {
  @Test def issue3939(): Unit = {
    import NullGuardsTest.issue3939._
    val sample = Month.JANUARY
    val field = JulianFields.JULIAN_DAY
    assertNotNull(sample.range(field))
    assertThrows(classOf[NullPointerException], sample.range(null))
  }
}

object NullGuardsTest {
  object issue3939 {
    trait TemporalAccessor {
      def isSupported(field: TemporalField): Boolean
      def range(field: TemporalField): ValueRange
    }

    final class Month extends TemporalAccessor {
      override def isSupported(field: TemporalField): Boolean = field.## == 0
      override def range(field: TemporalField): ValueRange =
        if (field eq ChronoField.MONTH_OF_YEAR) field.range
        else field.rangeRefinedBy(this)
    }
    object Month {
      lazy val JANUARY = new Month()
    }

    trait TemporalField {
      def range: ValueRange
      def rangeRefinedBy(temporal: TemporalAccessor): ValueRange
    }
    final class ValueRange

    final class ChronoField extends TemporalField {
      def range: ValueRange = new ValueRange()
      def rangeRefinedBy(temporal: TemporalAccessor): ValueRange =
        new ValueRange()
    }
    object ChronoField {
      lazy val MONTH_OF_YEAR = new ChronoField()
    }

    object JulianFields {
      lazy val JULIAN_DAY: TemporalField = new Field()
      private final class Field extends TemporalField {
        override def range: ValueRange = new ValueRange()
        override def rangeRefinedBy(temporal: TemporalAccessor): ValueRange =
          new ValueRange()
      }
    }
  }
}
