package org.scalanative.testsuite.javalib.nio.file

import java.nio.file._

import org.junit.{Test, BeforeClass}
import org.junit.Assert._
import org.junit.Assume._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform.isWindows

object UnixPathTest {
  @BeforeClass
  def assumeIsUnix(): Unit = {
    assumeFalse(
      "Not checking Unix paths on Windows",
      isWindows
    )
  }
}

class UnixPathTest {

  @Test def pathGetNameCount(): Unit = {
    assertTrue(Paths.get("/").getNameCount == 0)
    assertTrue(Paths.get("///").getNameCount == 0)
    assertTrue(Paths.get("").getNameCount == 1)
    assertTrue(Paths.get("foo").getNameCount == 1)
    assertTrue(Paths.get("foo//bar").getNameCount == 2)
    assertTrue(Paths.get("foo/bar/baz").getNameCount == 3)
    assertTrue(Paths.get("/foo/bar/baz").getNameCount == 3)
    assertTrue(Paths.get("././").getNameCount == 2)
    assertTrue(Paths.get("././ ").getNameCount == 3)
  }

  @Test def pathGetName(): Unit = {
    assertTrue(Paths.get("").getName(0).toString == "")
    assertTrue(Paths.get("foo").getName(0).toString == "foo")
    assertTrue(Paths.get("foo//bar").getName(0).toString == "foo")
    assertTrue(Paths.get("foo//bar").getName(1).toString == "bar")

    assertTrue(Paths.get("foo/bar/baz").getName(0).toString == "foo")
    assertTrue(Paths.get("foo/bar/baz").getName(1).toString == "bar")
    assertTrue(Paths.get("foo/bar/baz").getName(2).toString == "baz")

    assertTrue(Paths.get("/foo/bar/baz").getName(0).toString == "foo")
    assertTrue(Paths.get("/foo/bar/baz").getName(1).toString == "bar")
    assertTrue(Paths.get("/foo/bar/baz").getName(2).toString == "baz")

  }

  @Test def pathEndsWithWithAbsolutePath(): Unit = {
    assertTrue(Paths.get("/foo/bar/baz").endsWith(Paths.get("baz")))
    assertFalse(Paths.get("/foo/bar/baz").endsWith(Paths.get("/baz")))
    assertTrue(Paths.get("/foo/bar/baz").endsWith(Paths.get("bar/baz")))
    assertFalse(Paths.get("/foo/bar/baz").endsWith(Paths.get("/bar/baz")))
    assertTrue(Paths.get("/foo/bar/baz").endsWith(Paths.get("foo/bar/baz")))
    assertTrue(Paths.get("/foo/bar/baz").endsWith(Paths.get("/foo/bar/baz")))
  }

  @Test def pathEndsWithWithRelativePath(): Unit = {
    assertTrue(Paths.get("foo/bar/baz").endsWith(Paths.get("baz")))
    assertFalse(Paths.get("foo/bar/baz").endsWith(Paths.get("/baz")))
    assertTrue(Paths.get("foo/bar/baz").endsWith(Paths.get("bar/baz")))
    assertFalse(Paths.get("foo/bar/baz").endsWith(Paths.get("/bar/baz")))
    assertTrue(Paths.get("foo/bar/baz").endsWith(Paths.get("foo/bar/baz")))
    assertFalse(Paths.get("foo/bar/baz").endsWith(Paths.get("/foo/bar/baz")))
  }

  @Test def pathGetFileName(): Unit = {
    assertTrue(Paths.get("").getFileName.toString == "")
    assertTrue(Paths.get("foo").getFileName.toString == "foo")
    assertTrue(Paths.get("/foo").getFileName.toString == "foo")
    assertTrue(Paths.get("foo/bar").getFileName.toString == "bar")
    assertTrue(Paths.get("/foo/bar").getFileName.toString == "bar")
    assertTrue(Paths.get("/").getFileName == null)
    assertTrue(Paths.get("///").getFileName == null)
  }

  @Test def pathSubpath(): Unit = {
    assertTrue(Paths.get("").subpath(0, 1).toString == "")
    assertThrows(classOf[IllegalArgumentException], Paths.get("").subpath(0, 2))

    assertTrue(Paths.get("foo/bar/baz").subpath(0, 1).toString == "foo")
    assertTrue(Paths.get("foo/bar/baz").subpath(0, 2).toString == "foo/bar")
    assertTrue(Paths.get("foo/bar/baz").subpath(0, 3).toString == "foo/bar/baz")
    assertTrue(Paths.get("foo/bar/baz").subpath(1, 3).toString == "bar/baz")
    assertTrue(Paths.get("foo/bar/baz").subpath(2, 3).toString == "baz")

    assertTrue(Paths.get("/foo/bar/baz").subpath(0, 1).toString == "foo")
    assertTrue(Paths.get("/foo/bar/baz").subpath(0, 2).toString == "foo/bar")
    assertTrue(
      Paths.get("/foo/bar/baz").subpath(0, 3).toString == "foo/bar/baz"
    )
    assertTrue(Paths.get("/foo/bar/baz").subpath(1, 3).toString == "bar/baz")
    assertTrue(Paths.get("/foo/bar/baz").subpath(2, 3).toString == "baz")
  }

  @Test def pathGetParent(): Unit = {
    assertTrue(Paths.get("").getParent == null)
    assertTrue(Paths.get("foo").getParent == null)
    assertTrue(Paths.get("/").getParent == null)
    assertTrue(Paths.get("//").getParent == null)
    assertTrue(Paths.get("foo/bar").getParent.toString == "foo")
    assertTrue(Paths.get("/foo/bar").getParent.toString == "/foo")
    assertTrue(Paths.get("/foo").getParent.toString == "/")
    assertTrue(Paths.get("foo/.").getParent.toString == "foo")
    assertTrue(Paths.get("./.").getParent.toString == ".")
  }

  @Test def pathGetRoot(): Unit = {
    assertTrue(Paths.get("").getRoot == null)
    assertTrue(Paths.get("foo").getRoot == null)
    assertTrue(Paths.get("foo/bar").getRoot == null)
    assertTrue(Paths.get("/foo").getRoot.toString == "/")
    assertTrue(Paths.get("/foo/bar").getRoot.toString == "/")
    assertTrue(Paths.get("/foo///bar").getRoot.toString == "/")
    assertTrue(Paths.get("/").getRoot.toString == "/")
  }

  @Test def pathIsAbsolute(): Unit = {
    assertFalse(Paths.get("").isAbsolute)
    assertFalse(Paths.get("foo").isAbsolute)
    assertFalse(Paths.get("foo/bar").isAbsolute)
    assertTrue(Paths.get("/foo").isAbsolute)
    assertTrue(Paths.get("/foo/bar").isAbsolute)
    assertTrue(Paths.get("/foo///bar").isAbsolute)
    assertTrue(Paths.get("/").isAbsolute)
  }

  @Test def pathIterator(): Unit = {
    import scala.language.implicitConversions
    implicit def iteratorToSeq[T: scala.reflect.ClassTag](
        it: java.util.Iterator[T]
    ): Seq[T] = {
      import scala.collection.mutable.UnrolledBuffer
      val buf = new UnrolledBuffer[T]()
      while (it.hasNext) buf += it.next()
      buf.toSeq
    }

    assertTrue(Paths.get("").iterator.map(_.toString) == Seq(""))
    assertTrue(Paths.get("foo").iterator.map(_.toString) == Seq("foo"))
    assertTrue(
      Paths.get("foo/bar").iterator.map(_.toString) == Seq("foo", "bar")
    )
    assertTrue(
      Paths.get("foo//bar").iterator.map(_.toString) == Seq("foo", "bar")
    )
    assertTrue(Paths.get("/foo").iterator.map(_.toString) == Seq("foo"))
    assertTrue(
      Paths.get("/foo/bar").iterator.map(_.toString) == Seq("foo", "bar")
    )
    assertTrue(
      Paths.get("/foo//bar").iterator.map(_.toString) == Seq("foo", "bar")
    )
  }

  @Test def pathNormalize(): Unit = {
    assertTrue(Paths.get("").normalize.toString == "")
    assertTrue(Paths.get("foo").normalize.toString == "foo")
    assertTrue(Paths.get("foo/bar").normalize.toString == "foo/bar")
    assertTrue(Paths.get("foo//bar").normalize.toString == "foo/bar")
    assertTrue(Paths.get("foo/../bar").normalize.toString == "bar")
    assertTrue(Paths.get("foo/../../bar").normalize.toString == "../bar")
    assertTrue(Paths.get("/foo/../../bar").normalize.toString == "/bar")
    assertTrue(Paths.get("/").normalize.toString == "/")
    assertTrue(Paths.get("/foo").normalize.toString == "/foo")
    assertTrue(Paths.get("/foo/bar").normalize.toString == "/foo/bar")
    assertTrue(Paths.get("/foo//bar").normalize.toString == "/foo/bar")
    assertTrue(Paths.get("/foo/bar/").normalize.toString == "/foo/bar")
    assertTrue(Paths.get("./foo/bar/").normalize.toString == "foo/bar")
    assertTrue(Paths.get("../foo/bar/").normalize.toString == "../foo/bar")
    assertTrue(Paths.get("/foo/bar/.").normalize.toString == "/foo/bar")
    assertTrue(Paths.get("foo/bar/.").normalize.toString == "foo/bar")
    assertTrue(Paths.get("../foo/bar/.").normalize.toString == "../foo/bar")
    assertTrue(Paths.get("../foo//bar/.").normalize.toString == "../foo/bar")

    // SN Issue #4341, as reported
    val i4341FileName = "bar.jsonnet"
    val i4341Path_1 = "../../${i4341FileName}"
    assertEquals(
      "i4341_Path_1",
      i4341Path_1,
      Paths.get(i4341Path_1).normalize.toString
    )

    // SN Issue #4341, logical ripple
    val i4341Path_2 = s"a/b/../../${i4341FileName}"
    assertEquals(
      "i4341_Path_2",
      i4341FileName,
      Paths.get(i4341Path_2).normalize.toString
    )
  }

  @Test def pathStartsWith(): Unit = {
    assertTrue(Paths.get("").startsWith(Paths.get("")))
    assertTrue(Paths.get("foo").startsWith(Paths.get("foo")))
    assertTrue(Paths.get("foo/bar").startsWith(Paths.get("foo")))
    assertTrue(Paths.get("foo/bar/baz").startsWith(Paths.get("foo/bar")))
    assertFalse(Paths.get("foo").startsWith(Paths.get("bar")))
    assertFalse(Paths.get("foo/bar").startsWith(Paths.get("bar")))
    assertFalse(Paths.get("/").startsWith(Paths.get("")))
    assertFalse(Paths.get("").startsWith(Paths.get("/")))
    assertTrue(Paths.get("/foo").startsWith(Paths.get("/")))
    assertTrue(Paths.get("/foo/bar").startsWith(Paths.get("/foo")))
    assertTrue(Paths.get("/").startsWith(Paths.get("/")))
    assertFalse(Paths.get("/").startsWith("/foo"))
  }

  @Test def pathRelativize(): Unit = {
    assertTrue(Paths.get("").relativize(Paths.get("")).toString == "")
    assertTrue(
      Paths.get("foo").relativize(Paths.get("foo/bar")).toString == "bar"
    )
    assertTrue(
      Paths.get("foo/bar").relativize(Paths.get("foo")).toString == ".."
    )
    assertTrue(
      Paths.get("foo").relativize(Paths.get("bar")).toString == "../bar"
    )
    assertTrue(
      Paths
        .get("foo/bar")
        .relativize(Paths.get("foo/baz"))
        .toString == "../baz"
    )
    assertTrue(Paths.get("").relativize(Paths.get("foo")).toString == "foo")

    if (org.scalanative.testsuite.utils.Platform.executingInJVMOnJDK8OrLower) {
      assertTrue(
        Paths
          .get("foo/../bar")
          .relativize(Paths.get("bar"))
          .toString == "../../../bar"
      )
    } else {
      assertTrue(
        Paths
          .get("foo/../bar")
          .relativize(Paths.get("bar"))
          .toString == ""
      )
    }
    assertTrue(
      Paths
        .get("bar")
        .relativize(Paths.get("bar/../foo"))
        .toString == "../foo"
    )

    assertThrows(
      classOf[IllegalArgumentException],
      assertTrue(Paths.get("/").relativize(Paths.get("")).toString == "")
    )

    assertTrue(Paths.get("/").relativize(Paths.get("/")).toString == "")
    assertTrue(
      Paths.get("/foo").relativize(Paths.get("/foo/bar")).toString == "bar"
    )
    assertTrue(
      Paths.get("/foo/bar").relativize(Paths.get("/foo")).toString == ".."
    )
    assertTrue(
      Paths.get("/foo").relativize(Paths.get("/bar")).toString == "../bar"
    )
    assertTrue(
      Paths
        .get("/foo/bar")
        .relativize(Paths.get("/foo/baz"))
        .toString == "../baz"
    )
    assertTrue(Paths.get("/").relativize(Paths.get("/foo")).toString == "foo")
    if (org.scalanative.testsuite.utils.Platform.executingInJVMOnJDK8OrLower) {
      assertTrue(
        Paths
          .get("/foo/../bar")
          .relativize(Paths.get("/bar"))
          .toString == "../../../bar"
      )
    } else {
      assertTrue(
        Paths
          .get("/foo/../bar")
          .relativize(Paths.get("/bar"))
          .toString == ""
      )
    }
    assertTrue(
      Paths
        .get("/bar")
        .relativize(Paths.get("/bar/../foo"))
        .toString == "../foo"
    )
  }

  @Test def pathResolve()(): Unit = {
    assertTrue(Paths.get("").resolve(Paths.get("")).toString == "")
    assertTrue(Paths.get("/").resolve(Paths.get("")).toString == "/")
    assertTrue(
      Paths.get("foo").resolve(Paths.get("foo/bar")).toString == "foo/foo/bar"
    )
    assertTrue(
      Paths.get("foo/bar").resolve(Paths.get("foo")).toString == "foo/bar/foo"
    )
    assertTrue(Paths.get("foo").resolve(Paths.get("bar")).toString == "foo/bar")
    assertTrue(
      Paths
        .get("foo/bar")
        .resolve(Paths.get("foo/baz"))
        .toString == "foo/bar/foo/baz"
    )
    assertTrue(Paths.get("").resolve(Paths.get("foo")).toString == "foo")
    assertTrue(
      Paths
        .get("foo/../bar")
        .resolve(Paths.get("bar"))
        .toString == "foo/../bar/bar"
    )

    assertTrue(Paths.get("/").resolve(Paths.get("/")).toString == "/")
    assertTrue(
      Paths.get("/foo").resolve(Paths.get("/foo/bar")).toString == "/foo/bar"
    )
    assertTrue(
      Paths.get("/foo/bar").resolve(Paths.get("/foo")).toString == "/foo"
    )
    assertTrue(Paths.get("/foo").resolve(Paths.get("/bar")).toString == "/bar")
    assertTrue(
      Paths
        .get("/foo/bar")
        .resolve(Paths.get("/foo/baz"))
        .toString == "/foo/baz"
    )
    assertTrue(Paths.get("/").resolve(Paths.get("/foo")).toString == "/foo")
    assertTrue(
      Paths.get("/foo/../bar").resolve(Paths.get("/bar")).toString == "/bar"
    )
  }

  @Test def pathResolveSibling()(): Unit = {
    assertTrue(Paths.get("").resolveSibling(Paths.get("")).toString == "")
    assertTrue(Paths.get("/").resolveSibling(Paths.get("")).toString == "")
    assertTrue(
      Paths
        .get("foo")
        .resolveSibling(Paths.get("foo/bar"))
        .toString == "foo/bar"
    )
    assertTrue(
      Paths
        .get("foo/bar")
        .resolveSibling(Paths.get("foo"))
        .toString == "foo/foo"
    )
    assertTrue(
      Paths.get("foo").resolveSibling(Paths.get("bar")).toString == "bar"
    )
    assertTrue(
      Paths
        .get("foo/bar")
        .resolveSibling(Paths.get("foo/baz"))
        .toString == "foo/foo/baz"
    )
    assertTrue(Paths.get("").resolveSibling(Paths.get("foo")).toString == "foo")
    assertTrue(
      Paths
        .get("foo/../bar")
        .resolveSibling(Paths.get("bar"))
        .toString == "foo/../bar"
    )

    assertTrue(Paths.get("/").resolveSibling(Paths.get("/")).toString == "/")
    assertTrue(
      Paths
        .get("/foo")
        .resolveSibling(Paths.get("/foo/bar"))
        .toString == "/foo/bar"
    )
    assertTrue(
      Paths
        .get("/foo/bar")
        .resolveSibling(Paths.get("/foo"))
        .toString == "/foo"
    )
    assertTrue(
      Paths.get("/foo").resolveSibling(Paths.get("/bar")).toString == "/bar"
    )
    assertTrue(
      Paths
        .get("/foo/bar")
        .resolveSibling(Paths.get("/foo/baz"))
        .toString == "/foo/baz"
    )
    assertTrue(
      Paths.get("/").resolveSibling(Paths.get("/foo")).toString == "/foo"
    )
    assertTrue(
      Paths
        .get("/foo/../bar")
        .resolveSibling(Paths.get("/bar"))
        .toString == "/bar"
    )
  }

  @Test def pathEquals(): Unit = {
    assertTrue(Paths.get("") == Paths.get(""))
    assertTrue(Paths.get("////") == Paths.get("/"))
    assertTrue(Paths.get("/.") != Paths.get("/"))
  }
}
