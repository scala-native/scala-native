package org.scalanative.testsuite.scalalib.reflect

import scala.reflect.ClassTag

import org.junit._
import org.junit.Assert._

class ClassTagTest {

    @Test def referentialEquality(): Unit = {
        assertSame(ClassTag.Byte, implicitly[ClassTag[Byte]])
        assertSame(ClassTag.Short, implicitly[ClassTag[Short]])
        assertSame(ClassTag.Char, implicitly[ClassTag[Char]])
        assertSame(ClassTag.Int, implicitly[ClassTag[Int]])
        assertSame(ClassTag.Long, implicitly[ClassTag[Long]])
        assertSame(ClassTag.Float, implicitly[ClassTag[Float]])
        assertSame(ClassTag.Double, implicitly[ClassTag[Double]])
        assertSame(ClassTag.Boolean, implicitly[ClassTag[Boolean]])
        assertSame(ClassTag.Unit, implicitly[ClassTag[Unit]])
        assertSame(ClassTag.Object, implicitly[ClassTag[Object]])
        assertSame(ClassTag.AnyVal, implicitly[ClassTag[AnyVal]])
        assertSame(ClassTag.AnyRef, implicitly[ClassTag[AnyRef]])
        assertSame(ClassTag.Any, implicitly[ClassTag[Any]])
        // No implicit ClassTag in Scala 3
        assertSame(ClassTag.Nothing, ClassTag.Nothing)
        assertSame(ClassTag.Null, ClassTag.Null)
    }
}