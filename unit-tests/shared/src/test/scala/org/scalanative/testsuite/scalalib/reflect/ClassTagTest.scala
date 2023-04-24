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
        assertSame(ClassTag.Any, implicitly[ClassTag[Any]])
        assertSame(ClassTag.Any, implicitly[ClassTag[Any]])
        assertSame(ClassTag.Object, implicitly[ClassTag[Object]])
        assertSame(ClassTag.AnyVal, implicitly[ClassTag[AnyVal]])
        assertSame(ClassTag.AnyRef, implicitly[ClassTag[AnyRef]])
        assertSame(ClassTag.Nothing, implicitly[ClassTag[Nothing]])
        assertSame(ClassTag.Null, implicitly[ClassTag[Null]])
        assertSame(ClassTag.Any, implicitly[ClassTag[Any]])
        assertSame(ClassTag.Any, implicitly[ClassTag[Any]])
    }
}