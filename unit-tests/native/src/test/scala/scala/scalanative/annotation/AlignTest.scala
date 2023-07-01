package scala.scalanative
package annotation

import org.junit.{Test, Assume}
import org.junit.Assert._
import org.junit.Assume._

import scala.scalanative.unsafe.{sizeOf, Ptr}
import scala.scalanative.runtime.MemoryLayout.Object.FieldsOffset
import scala.scalanative.runtime.Intrinsics.{
  castObjectToRawPtr,
  classFieldRawPtr
}
import scala.scalanative.runtime.fromRawPtr
import scala.scalanative.meta.LinktimeInfo

package AlignTestCases {
  class NoAlign {
    var a: Int = 0
    var b: Int = 1
    var c: Int = 2
    var d: Int = 3

    assert((a, b, c, d) != null, "ensure linked")
  }

  @align(64) class AlignAllFixed {
    var a: Int = 0
    var b: Int = 1
    var c: Int = 2
    var d: Int = 3

    assert((a, b, c, d) != null, "ensure linked")
  }

  @align() class AlignAllDynamic {
    var a: Int = 0
    var b: Int = 1
    var c: Int = 2
    var d: Int = 3

    assert((a, b, c, d) != null, "ensure linked")
  }

  class AlignFields {
    var a: Int = 0
    var b: Int = 1
    @align(64) var c: Int = 2
    @align(64) var d: Int = 3

    assert((a, b, c, d) != null, "ensure linked")
  }

  class AlignFieldsGrouped {
    @align(64, "a") var a: Int = 0
    @align(64, "b") var b: Int = 1
    @align(64, "a") var c: Int = 2
    @align(64, "b") var d: Int = 3

    assert((a, b, c, d) != null, "ensure linked")
  }
}

class AlignTest {
  import AlignTestCases._
  private def checkClassSize(expected: Int, classSize: Int) = assertEquals(
    "class fields size size",
    expected,
    classSize
  )

  private def checkOffsets(
      expected: Seq[Int],
      basePointer: Ptr[Any],
      fieldPointers: Seq[Ptr[Any]]
  ) = {

    assertEquals(
      s"probes amount",
      expected.size,
      fieldPointers.size
    )
    assertEquals(
      "offsets",
      expected.toList,
      fieldPointers
        .map(_.toLong - basePointer.toLong)
        .ensuring(_.forall(_ >= 0), "negative calucated offset")
        .toList
    )
  }

  @Test def noAlign(): Unit = {
    val obj = new NoAlign()
    checkClassSize(16 + FieldsOffset, sizeOf[NoAlign])
    checkOffsets(
      expected = Seq(0, 4, 8, 12).map(_ + FieldsOffset),
      basePointer = fromRawPtr(castObjectToRawPtr(obj)),
      fieldPointers = Seq(
        fromRawPtr(classFieldRawPtr(obj, "a")),
        fromRawPtr(classFieldRawPtr(obj, "b")),
        fromRawPtr(classFieldRawPtr(obj, "c")),
        fromRawPtr(classFieldRawPtr(obj, "d"))
      )
    )
  }

  @Test def allignAllFixed(): Unit = {
    val obj = new AlignAllFixed()
    checkClassSize(320, sizeOf[AlignAllFixed])
    checkOffsets(
      expected = Seq(64, 128, 192, 256),
      basePointer = fromRawPtr(castObjectToRawPtr(obj)),
      fieldPointers = Seq(
        fromRawPtr(classFieldRawPtr(obj, "a")),
        fromRawPtr(classFieldRawPtr(obj, "b")),
        fromRawPtr(classFieldRawPtr(obj, "c")),
        fromRawPtr(classFieldRawPtr(obj, "d"))
      )
    )
  }

  @Test def allignAllDynamic(): Unit = {
    val obj = new AlignAllDynamic()
    assumeTrue(
      "non default contention padding width",
      64 == LinktimeInfo.contendedPaddingWidth
    )
    checkClassSize(320, sizeOf[AlignAllDynamic])
    checkOffsets(
      expected = Seq(64, 128, 192, 256),
      basePointer = fromRawPtr(castObjectToRawPtr(obj)),
      fieldPointers = Seq(
        fromRawPtr(classFieldRawPtr(obj, "a")),
        fromRawPtr(classFieldRawPtr(obj, "b")),
        fromRawPtr(classFieldRawPtr(obj, "c")),
        fromRawPtr(classFieldRawPtr(obj, "d"))
      )
    )
  }

  @Test def allignFields(): Unit = {
    val obj = new AlignFields()
    checkClassSize(192, sizeOf[AlignFields])
    checkOffsets(
      expected = Seq(FieldsOffset, FieldsOffset + 4, 64, 128),
      basePointer = fromRawPtr(castObjectToRawPtr(obj)),
      fieldPointers = Seq(
        fromRawPtr(classFieldRawPtr(obj, "a")),
        fromRawPtr(classFieldRawPtr(obj, "b")),
        fromRawPtr(classFieldRawPtr(obj, "c")),
        fromRawPtr(classFieldRawPtr(obj, "d"))
      )
    )
  }

  @Test def allignFieldsGrouped(): Unit = {
    val obj = new AlignFieldsGrouped()
    checkClassSize(192, sizeOf[AlignFieldsGrouped])
    checkOffsets(
      // grouped by: a b a b
      expected = Seq(64, 128, 68, 132),
      basePointer = fromRawPtr(castObjectToRawPtr(obj)),
      fieldPointers = Seq(
        fromRawPtr(classFieldRawPtr(obj, "a")),
        fromRawPtr(classFieldRawPtr(obj, "b")),
        fromRawPtr(classFieldRawPtr(obj, "c")),
        fromRawPtr(classFieldRawPtr(obj, "d"))
      )
    )
  }
}
