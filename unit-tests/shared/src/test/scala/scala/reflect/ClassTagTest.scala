package scala.reflect

import org.junit.Assert._
import org.junit.Test

class ClassTagTest {
  private class Marker

  @Test def cachesTagsOfClasses(): Unit = {
    val tag = ClassTag[Marker](classOf[Marker])

    assertSame(tag, ClassTag[Marker](classOf[Marker]))
    assertEquals(classOf[Marker], tag.runtimeClass)
    assertEquals(3, tag.newArray(3).length)
  }

  @Test def usesPredefinedTags(): Unit = {
    assertSame(ClassTag.Int, ClassTag[Int](classOf[Int]))
    assertSame(ClassTag.Object, ClassTag[Object](classOf[Object]))
    assertSame(ClassTag.Unit, ClassTag[Unit](java.lang.Void.TYPE))
  }
}
