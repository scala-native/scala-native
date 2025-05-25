package org.scalanative.testsuite.javalib.nio.file

import java.nio.file._
import java.{util => ju}

import org.junit.{Test, BeforeClass}
import org.junit.Assert._
import org.junit.Assume._

import scala.collection.mutable

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform.isWindows

object WindowsPathTest {
  @BeforeClass
  def assumeIsWindows(): Unit = {
    assumeTrue(
      "Not checking Windows paths on Unix",
      isWindows
    )
  }
}

class WindowsPathTest {
  // Test that are commented and marked with TODO represents known issues.

  @Test def pathsGet(): Unit = {
    assertThrows(classOf[InvalidPathException], Paths.get("///"))
  }

  @Test def pathGetNameCount(): Unit = {
    assertTrue(Paths.get("/").getNameCount == 0)
    assertTrue(Paths.get("x:/").getNameCount == 0)
    // TODO: In JVM empty path has count 1
    // assertTrue(Paths.get("").getNameCount == 1)
    assertTrue(Paths.get("foo").getNameCount == 1)
    assertTrue(Paths.get("foo//bar").getNameCount == 2)
    assertTrue(Paths.get("foo/bar/baz").getNameCount == 3)
    assertTrue(Paths.get("/foo/bar/baz").getNameCount == 3)
    assertTrue(Paths.get("x:/foo/bar/baz").getNameCount == 3)
    assertTrue(Paths.get("././").getNameCount == 2)
//    // TODO JVM 17 throws: InvalidPathException: Trailing char < > at index 4: ././
//    assertTrue(Paths.get("././ ").getNameCount == 3)
  }

  @Test def pathGetName(): Unit = {
    // TODO:
    // assertEquals("", Paths.get("").getName(0).toString)
    assertEquals("foo", Paths.get("foo").getName(0).toString)
    assertEquals("foo", Paths.get("foo//bar").getName(0).toString)
    assertEquals("bar", Paths.get("foo//bar").getName(1).toString)

    assertEquals("foo", Paths.get("foo/bar/baz").getName(0).toString)
    assertEquals("bar", Paths.get("foo/bar/baz").getName(1).toString)
    assertEquals("baz", Paths.get("foo/bar/baz").getName(2).toString)

    assertEquals("foo", Paths.get("/foo/bar/baz").getName(0).toString)
    assertEquals("bar", Paths.get("/foo/bar/baz").getName(1).toString)
    assertEquals("baz", Paths.get("/foo/bar/baz").getName(2).toString)

    assertEquals("foo", Paths.get("x:/foo/bar/baz").getName(0).toString)
    assertEquals("bar", Paths.get("x:/foo/bar/baz").getName(1).toString)
    assertEquals("baz", Paths.get("x:/foo/bar/baz").getName(2).toString)
  }

  @Test def pathEndsWithWithAbsolutePath(): Unit = {
    assertTrue(Paths.get("/foo/bar/baz").endsWith(Paths.get("baz")))
    assertTrue(Paths.get("x:/foo/bar/baz").endsWith(Paths.get("baz")))
    // TODO: on JVM ending cannot start with /
    // assertFalse(Paths.get("/foo/bar/baz").endsWith(Paths.get("/baz")))
    // TODO: on JVM ending cannot start with /
    // assertFalse(Paths.get("x:/foo/bar/baz").endsWith(Paths.get("/baz")))
    assertTrue(Paths.get("/foo/bar/baz").endsWith(Paths.get("bar/baz")))
    assertTrue(Paths.get("x:/foo/bar/baz").endsWith(Paths.get("bar/baz")))
    // TODO: on JVM ending cannot start with /
    // assertFalse(Paths.get("/foo/bar/baz").endsWith(Paths.get("/bar/baz")))
    // TODO: on JVM ending cannot start with /
    // assertFalse(Paths.get("x:/foo/bar/baz").endsWith(Paths.get("/bar/baz")))
    assertTrue(Paths.get("/foo/bar/baz").endsWith(Paths.get("foo/bar/baz")))
    assertTrue(Paths.get("x:/foo/bar/baz").endsWith(Paths.get("foo/bar/baz")))
    assertTrue(Paths.get("/foo/bar/baz").endsWith(Paths.get("/foo/bar/baz")))
    assertTrue(
      Paths.get("x:/foo/bar/baz").endsWith(Paths.get("x:/foo/bar/baz"))
    )
  }

  @Test def pathEndsWithWithRelativePath(): Unit = {
    assertTrue(Paths.get("foo/bar/baz").endsWith(Paths.get("baz")))
    // TODO: on JVM ending cannot start with /
    // assertFalse(Paths.get("foo/bar/baz").endsWith(Paths.get("/baz")))
    assertTrue(Paths.get("foo/bar/baz").endsWith(Paths.get("bar/baz")))
    // TODO: on JVM ending cannot start with /
    // assertFalse(Paths.get("foo/bar/baz").endsWith(Paths.get("/bar/baz")))
    assertTrue(Paths.get("foo/bar/baz").endsWith(Paths.get("foo/bar/baz")))
    // TODO: on JVM ending cannot start with /
    // assertFalse(Paths.get("foo/bar/baz").endsWith(Paths.get("/foo/bar/baz")))
  }

  @Test def pathGetFileName(): Unit = {
    // TODO: on JVM empty path has a name ""
    // assertEquals("", Paths.get("").getFileName.toString)
    assertEquals("foo", Paths.get("foo").getFileName.toString)
    assertEquals("foo", Paths.get("/foo").getFileName.toString)
    assertEquals("foo", Paths.get("x:/foo").getFileName.toString)
    assertEquals("bar", Paths.get("foo/bar").getFileName.toString)
    assertEquals("bar", Paths.get("/foo/bar").getFileName.toString)
    assertEquals("bar", Paths.get("x:/foo/bar").getFileName.toString)
    // TODO: on JVM "/" has a no name
    // assertEquals(null, Paths.get("/").getFileName)
    // TODO: on JVM "x:/" has a no name
    // assertEquals(null, Paths.get("x:/").getFileName)
    assertEquals(null, Paths.get("x:").getFileName)
  }

  @Test def pathSubpath(): Unit = {
    assertEquals("", Paths.get("").subpath(0, 1).toString)
    // TODO
    // assertThrows(classOf[IllegalArgumentException], Paths.get("").subpath(0, 2))

    assertEquals("foo", Paths.get("foo/bar/baz").subpath(0, 1).toString)
    assertEquals("foo\\bar", Paths.get("foo/bar/baz").subpath(0, 2).toString)
    assertEquals(
      "foo\\bar\\baz",
      Paths.get("foo/bar/baz").subpath(0, 3).toString
    )
    assertEquals("bar\\baz", Paths.get("foo/bar/baz").subpath(1, 3).toString)
    assertEquals("baz", Paths.get("foo/bar/baz").subpath(2, 3).toString)

    assertEquals("foo", Paths.get("/foo/bar/baz").subpath(0, 1).toString)
    assertEquals("foo", Paths.get("x:/foo/bar/baz").subpath(0, 1).toString)
    assertEquals("foo\\bar", Paths.get("/foo/bar/baz").subpath(0, 2).toString)
    assertEquals("foo\\bar", Paths.get("x:/foo/bar/baz").subpath(0, 2).toString)
    assertEquals(
      "foo\\bar\\baz",
      Paths.get("/foo/bar/baz").subpath(0, 3).toString
    )
    assertEquals(
      "foo\\bar\\baz",
      Paths.get("x:/foo/bar/baz").subpath(0, 3).toString
    )
    assertEquals("bar\\baz", Paths.get("/foo/bar/baz").subpath(1, 3).toString)
    assertEquals("bar\\baz", Paths.get("x:/foo/bar/baz").subpath(1, 3).toString)
    assertEquals("baz", Paths.get("/foo/bar/baz").subpath(2, 3).toString)
    assertEquals("baz", Paths.get("x:/foo/bar/baz").subpath(2, 3).toString)
  }

  @Test def pathGetParent(): Unit = {
    assertEquals(null, Paths.get("").getParent)
    assertEquals(null, Paths.get("x:").getParent)
    assertEquals(null, Paths.get("foo").getParent)
    assertEquals(null, Paths.get("/").getParent)
    assertEquals(null, Paths.get("x:/").getParent)
    assertEquals(null, Paths.get("\\").getParent)
    assertEquals(null, Paths.get("x:\\").getParent)
    assertEquals("foo", Paths.get("foo/bar").getParent.toString)
    assertEquals("\\foo", Paths.get("/foo/bar").getParent.toString)
    assertEquals("x:\\foo", Paths.get("x:/foo/bar").getParent.toString)
    assertEquals("\\", Paths.get("/foo").getParent.toString)
    assertEquals("x:\\", Paths.get("x:/foo").getParent.toString)
    assertEquals("foo", Paths.get("foo/.").getParent.toString)
    assertEquals(".", Paths.get("./.").getParent.toString)
  }

  @Test def pathGetRoot(): Unit = {
    assertEquals(null, Paths.get("").getRoot)
    assertEquals(null, Paths.get("foo").getRoot)
    assertEquals(null, Paths.get("foo/bar").getRoot)
    assertEquals("\\", Paths.get("/foo").getRoot.toString)
    assertEquals("x:\\", Paths.get("x:/foo").getRoot.toString)
    assertEquals("\\", Paths.get("/foo/bar").getRoot.toString)
    assertEquals("x:\\", Paths.get("x:/foo/bar").getRoot.toString)
    assertEquals("\\", Paths.get("/foo///bar").getRoot.toString)
    assertEquals("x:\\", Paths.get("x:/foo///bar").getRoot.toString)
    assertEquals("\\", Paths.get("/").getRoot.toString)
    assertEquals("x:\\", Paths.get("x:/").getRoot.toString)
  }

  @Test def pathIsAbsolute(): Unit = {
    assertFalse(Paths.get("").isAbsolute)
    assertFalse(Paths.get("foo").isAbsolute)
    assertFalse(Paths.get("foo/bar").isAbsolute)
    assertFalse(Paths.get("/foo").isAbsolute)
    assertTrue(Paths.get("x:/foo").isAbsolute)
    assertFalse(Paths.get("/foo/bar").isAbsolute)
    assertTrue(Paths.get("x:/foo/bar").isAbsolute)
    assertFalse(Paths.get("/foo///bar").isAbsolute)
    assertTrue(Paths.get("x:/foo///bar").isAbsolute)
    assertFalse(Paths.get("/").isAbsolute)
    assertTrue(Paths.get("x:/").isAbsolute)
  }

  @Test def pathIterator(): Unit = {
    import scala.language.implicitConversions
    implicit def iteratorToSeq[T: scala.reflect.ClassTag](
        it: java.util.Iterator[T]
    ): Seq[T] = {
      val buf = new mutable.UnrolledBuffer[T]()
      while (it.hasNext()) buf += it.next()
      buf.toSeq
    }

    // TODO
    // assertEquals(Seq(""), Paths.get("").iterator.map(_.toString))
    assertEquals(Seq("foo"), Paths.get("foo").iterator.map(_.toString))
    assertEquals(
      Seq("foo", "bar"),
      Paths.get("foo/bar").iterator.map(_.toString)
    )
    assertEquals(
      Seq("foo", "bar"),
      Paths.get("foo//bar").iterator.map(_.toString)
    )
    assertEquals(Seq("foo"), Paths.get("/foo").iterator.map(_.toString))
    assertEquals(Seq("foo"), Paths.get("x:/foo").iterator.map(_.toString))
    assertEquals(
      Seq("foo", "bar"),
      Paths.get("/foo/bar").iterator.map(_.toString)
    )
    assertEquals(
      Seq("foo", "bar"),
      Paths.get("x:/foo/bar").iterator.map(_.toString)
    )
    assertEquals(
      Seq("foo", "bar"),
      Paths.get("/foo//bar").iterator.map(_.toString)
    )
    assertEquals(
      Seq("foo", "bar"),
      Paths.get("x:/foo//bar").iterator.map(_.toString)
    )
  }

  @Test def pathNormalize(): Unit = {
    assertEquals("", Paths.get("").normalize.toString)
    assertEquals("foo", Paths.get("foo").normalize.toString)
    assertEquals("foo\\bar", Paths.get("foo/bar").normalize.toString)
    assertEquals("foo\\bar", Paths.get("foo//bar").normalize.toString)
    assertEquals("bar", Paths.get("foo/../bar").normalize.toString)
    assertEquals("..\\bar", Paths.get("foo/../../bar").normalize.toString)
    // TODO
    // assertEquals("\\bar", Paths.get("/foo/../../bar").normalize.toString)
    assertEquals("x:\\bar", Paths.get("x:/foo/../../bar").normalize.toString)
    assertEquals("\\", Paths.get("/").normalize.toString)
    assertEquals("x:\\", Paths.get("x:/").normalize.toString)
    assertEquals("x:", Paths.get("x:").normalize.toString)
    assertEquals("\\foo", Paths.get("/foo").normalize.toString)
    assertEquals("x:\\foo", Paths.get("x:/foo").normalize.toString)
    assertEquals("\\foo\\bar", Paths.get("/foo/bar").normalize.toString)
    assertEquals("x:\\foo\\bar", Paths.get("x:/foo/bar").normalize.toString)
    assertEquals("\\foo\\bar", Paths.get("/foo//bar").normalize.toString)
    assertEquals("x:\\foo\\bar", Paths.get("x:/foo//bar").normalize.toString)
    assertEquals("\\foo\\bar", Paths.get("/foo/bar/").normalize.toString)
    assertEquals("x:\\foo\\bar", Paths.get("x:/foo/bar/").normalize.toString)
    assertEquals("foo\\bar", Paths.get("./foo/bar/").normalize.toString)
    assertEquals("..\\foo\\bar", Paths.get("../foo/bar/").normalize.toString)
    assertEquals("\\foo\\bar", Paths.get("/foo/bar/.").normalize.toString)
    assertEquals("x:\\foo\\bar", Paths.get("x:/foo/bar/.").normalize.toString)
    assertEquals("foo\\bar", Paths.get("foo/bar/.").normalize.toString)
    assertEquals("..\\foo\\bar", Paths.get("../foo/bar/.").normalize.toString)
    assertEquals("..\\foo\\bar", Paths.get("../foo//bar/.").normalize.toString)

    // SN Issue #4341, as reported & logically related

    case class testPoint(rawPath: String, expected: String)
    val i4341FileName = "bar.jsonnet"

    // The 'expected' JVM path is the same for both WindowsPath & UnixPath.
    val i4341TestPoints = ju.Arrays.asList(
      testPoint(raw"..\\..\\${i4341FileName}", s"../../${i4341FileName}"),
      testPoint(raw"a\\b\\..\\..\\${i4341FileName}", s"${i4341FileName}"),
      testPoint(raw"\\a\\.\\..\\${i4341FileName}", s"/${i4341FileName}")
    )

    i4341TestPoints.forEach(t =>
      assertEquals(
        "i4341",
        t.expected,
        Paths.get(t.rawPath).normalize.toString()
      )
    )
  }

  @Test def pathStartsWith(): Unit = {
    // assertTrue(Paths.get("").startsWith(Paths.get("")))
    assertTrue(Paths.get("foo").startsWith(Paths.get("foo")))
    assertTrue(Paths.get("foo/bar").startsWith(Paths.get("foo")))
    assertTrue(Paths.get("foo/bar/baz").startsWith(Paths.get("foo/bar")))
    assertFalse(Paths.get("foo").startsWith(Paths.get("bar")))
    assertFalse(Paths.get("foo/bar").startsWith(Paths.get("bar")))
    // TODO
    // assertFalse(Paths.get("/").startsWith(Paths.get("")))
    assertFalse(Paths.get("x:/").startsWith(Paths.get("")))
    // TODO
    // assertFalse(Paths.get("").startsWith(Paths.get("/")))
    assertTrue(Paths.get("/foo").startsWith(Paths.get("/")))
    assertTrue(Paths.get("x:/foo").startsWith(Paths.get("x:/")))
    assertTrue(Paths.get("/foo/bar").startsWith(Paths.get("/foo")))
    assertTrue(Paths.get("x:/foo/bar").startsWith(Paths.get("x:/foo")))
    assertTrue(Paths.get("/").startsWith(Paths.get("/")))
    assertFalse(Paths.get("x:/").startsWith(Paths.get("x:")))
    assertTrue(Paths.get("x:/").startsWith(Paths.get("x:\\")))
    assertFalse(Paths.get("/").startsWith("/foo"))
    assertFalse(Paths.get("x:/").startsWith("x:/foo"))
  }

  @Test def pathRelativize(): Unit = {
    assertEquals("#1", "", Paths.get("").relativize(Paths.get("")).toString)
    assertEquals(
      "#2",
      "bar",
      Paths.get("foo").relativize(Paths.get("foo/bar")).toString
    )
    assertEquals(
      "#3",
      "..",
      Paths.get("foo/bar").relativize(Paths.get("foo")).toString
    )
    assertEquals(
      "#4",
      "..\\bar",
      Paths.get("foo").relativize(Paths.get("bar")).toString
    )
    assertEquals(
      "#5",
      "..\\baz",
      Paths
        .get("foo/bar")
        .relativize(Paths.get("foo/baz"))
        .toString
    )
    if (org.scalanative.testsuite.utils.Platform.executingInJVMOnJDK8OrLower) {
      // TODO Java 8-
//      assertEquals(
//        "#6-JVM8",
//        "..\\foo",
//        Paths.get("").relativize(Paths.get("foo")).toString
//      )
    } else {
      assertEquals(
        "#6",
        "foo",
        Paths.get("").relativize(Paths.get("foo")).toString
      )
    }
    if (org.scalanative.testsuite.utils.Platform.executingInJVMOnJDK8OrLower) {
      // TODO Java 8-
//      assertEquals(
//        "#7-JVM8",
//        "..\\..\\..\\bar",
//        Paths
//          .get("foo/../bar")
//          .relativize(Paths.get("bar"))
//          .toString
//      )
    } else {
      assertEquals(
        "#7",
        "",
        Paths
          .get("foo/../bar")
          .relativize(Paths.get("bar"))
          .toString
      )
    }
    assertEquals(
      "#8",
      "..\\foo",
      Paths
        .get("bar")
        .relativize(Paths.get("bar/../foo"))
        .toString
    )
    assertThrows(
      "#9",
      classOf[IllegalArgumentException],
      assertEquals("", Paths.get("/").relativize(Paths.get("")).toString)
    )
    assertEquals("#10", "", Paths.get("/").relativize(Paths.get("/")).toString)
    assertEquals(
      "#11",
      "",
      Paths.get("x:/").relativize(Paths.get("x:/")).toString
    )
    assertEquals(
      "#12",
      "bar",
      Paths.get("/foo").relativize(Paths.get("/foo/bar")).toString
    )
    assertEquals(
      "#13",
      "bar",
      Paths.get("x:/foo").relativize(Paths.get("x:/foo/bar")).toString
    )
    assertEquals(
      "#14",
      "..",
      Paths.get("/foo/bar").relativize(Paths.get("/foo")).toString
    )
    assertEquals(
      "#15",
      "..",
      Paths.get("x:/foo/bar").relativize(Paths.get("x:/foo")).toString
    )
    assertEquals(
      "#17",
      "..\\bar",
      Paths.get("/foo").relativize(Paths.get("/bar")).toString
    )
    assertEquals(
      "#18",
      "..\\bar",
      Paths.get("x:/foo").relativize(Paths.get("x:/bar")).toString
    )
    assertEquals(
      "#19",
      "..\\baz",
      Paths
        .get("/foo/bar")
        .relativize(Paths.get("/foo/baz"))
        .toString
    )
    assertEquals(
      "#20",
      "..\\baz",
      Paths
        .get("x:/foo/bar")
        .relativize(Paths.get("x:/foo/baz"))
        .toString
    )
    assertEquals(
      "#21",
      "foo",
      Paths.get("/").relativize(Paths.get("/foo")).toString
    )
    if (org.scalanative.testsuite.utils.Platform.executingInJVMOnJDK8OrLower) {
      // TODO Java 8-
//      assertEquals(
//        "#22-JVM8",
//        "..\\..\\..\\bar",
//        Paths
//          .get("/foo/../bar")
//          .relativize(Paths.get("/bar"))
//          .toString
//      )
    } else {
      assertEquals(
        "#22",
        "",
        Paths
          .get("/foo/../bar")
          .relativize(Paths.get("/bar"))
          .toString
      )
    }
    if (org.scalanative.testsuite.utils.Platform.executingInJVMOnJDK8OrLower) {
      // TODO Java 8-
//      assertEquals(
//        "#23-JVM8",
//        "..\\..\\..\\bar",
//        Paths
//          .get("x:/foo/../bar")
//          .relativize(Paths.get("x:/bar"))
//          .toString
//      )
    } else {
      assertEquals(
        "#24",
        "",
        Paths
          .get("x:/foo/../bar")
          .relativize(Paths.get("x:/bar"))
          .toString
      )
    }
    assertEquals(
      "#25",
      "..\\foo",
      Paths
        .get("/bar")
        .relativize(Paths.get("/bar/../foo"))
        .toString
    )
    assertEquals(
      "#26",
      "..\\foo",
      Paths
        .get("x:/bar")
        .relativize(Paths.get("x:/bar/../foo"))
        .toString
    )
    assertEquals(
      "#27",
      "b\\c.jar",
      Paths
        .get("C:\\a")
        .relativize(Paths.get("C:\\a\\b\\c.jar"))
        .toString
    )
  }

  @Test def pathResolve(): Unit = {
    assertEquals("", Paths.get("").resolve(Paths.get("")).toString)
    assertEquals("\\", Paths.get("/").resolve(Paths.get("")).toString)
    assertEquals("x:\\", Paths.get("x:/").resolve(Paths.get("")).toString)
    assertEquals(
      "foo\\foo\\bar",
      Paths.get("foo").resolve(Paths.get("foo/bar")).toString
    )
    assertEquals(
      "foo\\bar\\foo",
      Paths.get("foo/bar").resolve(Paths.get("foo")).toString
    )
    assertEquals(
      "foo\\bar",
      Paths.get("foo").resolve(Paths.get("bar")).toString
    )
    assertEquals(
      "foo\\bar\\foo\\baz",
      Paths
        .get("foo/bar")
        .resolve(Paths.get("foo/baz"))
        .toString
    )
    assertEquals("foo", Paths.get("").resolve(Paths.get("foo")).toString)
    assertEquals(
      "foo\\..\\bar\\bar",
      Paths
        .get("foo/../bar")
        .resolve(Paths.get("bar"))
        .toString
    )

    assertEquals("\\", Paths.get("/").resolve(Paths.get("/")).toString)
    assertEquals("x:\\", Paths.get("x:/").resolve(Paths.get("x:/")).toString)
    // TODO
    // assertEquals(
    //   "\\foo\\bar", Paths.get("/foo").resolve(Paths.get("/foo/bar")).toString
    // )
    assertEquals(
      "x:\\foo\\bar",
      Paths.get("x:/foo").resolve(Paths.get("x:/foo/bar")).toString
    )
    // TODO
    // assertEquals(
    //   "\\foo", Paths.get("/foo/bar").resolve(Paths.get("/foo")).toString,
    // )
    assertEquals(
      "x:\\foo",
      Paths.get("x:/foo/bar").resolve(Paths.get("x:/foo")).toString
    )
    // TODO
    // assertEquals("\\bar", Paths.get("/foo").resolve(Paths.get("/bar")).toString, )
    assertEquals(
      "x:\\bar",
      Paths.get("x:/foo").resolve(Paths.get("x:/bar")).toString
    )
    // TODO
    // assertEquals(
    //   "\\foo\\baz",
    //   Paths
    //     .get("/foo/bar")
    //     .resolve(Paths.get("/foo/baz"))
    //     .toString
    // )
    assertEquals(
      "x:\\foo\\baz",
      Paths
        .get("x:/foo/bar")
        .resolve(Paths.get("x:/foo/baz"))
        .toString
    )

    assertEquals("\\foo", Paths.get("/").resolve(Paths.get("/foo")).toString)
    assertEquals(
      "x:\\foo",
      Paths.get("x:/").resolve(Paths.get("x:/foo")).toString
    )
    // TODO
    // assertEquals(
    //   "\\bar", Paths.get("/foo/../bar").resolve(Paths.get("/bar")).toString
    // )
    assertEquals(
      "x:\\bar",
      Paths.get("x:/foo/../bar").resolve(Paths.get("x:/bar")).toString
    )
  }

  @Test def pathResolveSibling(): Unit = {
    assertEquals("", Paths.get("").resolveSibling(Paths.get("")).toString)
    assertEquals("", Paths.get("/").resolveSibling(Paths.get("")).toString)
    assertEquals("", Paths.get("x:/").resolveSibling(Paths.get("")).toString)
    assertEquals(
      "foo\\bar",
      Paths
        .get("foo")
        .resolveSibling(Paths.get("foo/bar"))
        .toString
    )
    assertEquals(
      "foo\\foo",
      Paths
        .get("foo/bar")
        .resolveSibling(Paths.get("foo"))
        .toString
    )
    assertEquals(
      "bar",
      Paths.get("foo").resolveSibling(Paths.get("bar")).toString
    )
    assertEquals(
      "foo\\foo\\baz",
      Paths
        .get("foo/bar")
        .resolveSibling(Paths.get("foo/baz"))
        .toString
    )
    assertEquals("foo", Paths.get("").resolveSibling(Paths.get("foo")).toString)
    assertEquals(
      "foo\\..\\bar",
      Paths
        .get("foo/../bar")
        .resolveSibling(Paths.get("bar"))
        .toString
    )

    assertEquals("\\", Paths.get("/").resolveSibling(Paths.get("/")).toString)
    assertEquals(
      "x:\\",
      Paths.get("x:/").resolveSibling(Paths.get("x:/")).toString
    )
    assertEquals(
      "\\foo\\bar",
      Paths
        .get("/foo")
        .resolveSibling(Paths.get("/foo/bar"))
        .toString
    )
    assertEquals(
      "x:\\foo\\bar",
      Paths
        .get("x:/foo")
        .resolveSibling(Paths.get("x:/foo/bar"))
        .toString
    )
    // TODO
    // assertEquals(
    //   "\\foo",
    //   Paths
    //     .get("/foo/bar")
    //     .resolveSibling(Paths.get("/foo"))
    //     .toString,
    // )
    assertEquals(
      "x:\\foo",
      Paths
        .get("x:/foo/bar")
        .resolveSibling(Paths.get("x:/foo"))
        .toString
    )
    assertEquals(
      "\\bar",
      Paths.get("/foo").resolveSibling(Paths.get("/bar")).toString
    )
    assertEquals(
      "x:\\bar",
      Paths.get("x:/foo").resolveSibling(Paths.get("x:/bar")).toString
    )
    // TODO
    // assertEquals(
    //   "\\foo\\baz",
    //   Paths
    //     .get("/foo/bar")
    //     .resolveSibling(Paths.get("/foo/baz"))
    //     .toString,
    // )
    assertEquals(
      "x:\\foo\\baz",
      Paths
        .get("x:/foo/bar")
        .resolveSibling(Paths.get("x:/foo/baz"))
        .toString
    )
    assertEquals(
      "\\foo",
      Paths.get("/").resolveSibling(Paths.get("/foo")).toString
    )
    assertEquals(
      "x:\\foo",
      Paths.get("x:/").resolveSibling(Paths.get("x:/foo")).toString
    )
    // TODO
    // assertEquals(
    //   "\\bar"
    //   Paths
    //     .get("/foo/../bar")
    //     .resolveSibling(Paths.get("/bar"))
    //     .toString,
    // )
    assertEquals(
      "x:\\bar",
      Paths
        .get("x:/foo/../bar")
        .resolveSibling(Paths.get("x:/bar"))
        .toString
    )
  }

  @Test def pathEquals(): Unit = {
    assertTrue(Paths.get("") == Paths.get(""))
    assertTrue(Paths.get("x:////") == Paths.get("x:\\"))
    assertTrue(Paths.get("/.") != Paths.get("\\"))
    assertTrue(Paths.get("x:/.") != Paths.get("x:\\"))
  }
}
