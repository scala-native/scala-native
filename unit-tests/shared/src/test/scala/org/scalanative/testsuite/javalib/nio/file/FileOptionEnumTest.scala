package org.scalanative.testsuite.javalib.nio.file

import java.io.Serializable
import java.nio.file.{LinkOption, StandardCopyOption, StandardOpenOption}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

abstract class FileOptionEnumTest[E <: Enum[E]] {
  protected def expectedNames: Array[String]
  protected def enumTypeName: String
  protected def values(): Array[E]
  protected def lookup(name: String): E

  @Test def allDeclaredNamesReturnCanonicalConstants(): Unit = {
    val constants = values()
    assertEquals(expectedNames.toList, constants.map(_.name()).toList)
    for (i <- constants.indices) {
      val constant = constants(i)
      assertSame(constant, lookup(new String(expectedNames(i).toCharArray())))
      assertEquals(i, constant.ordinal())
      assertTrue(constant.isInstanceOf[Serializable])
    }
  }

  @Test def nullNameIsRejected(): Unit = {
    val failure = assertThrows(classOf[NullPointerException], lookup(null))
    assertEquals("Name is null", failure.getMessage())
  }

  @Test def invalidNamesAreRejectedWithoutNormalization(): Unit = {
    val name = expectedNames(0)
    val invalidNames = Array(
      "",
      "MISSING_CONSTANT",
      name.toLowerCase(),
      " " + name,
      name + " ",
      enumTypeName + "." + name
    )
    for (invalid <- invalidNames) {
      val failure =
        assertThrows(classOf[IllegalArgumentException], lookup(invalid))
      assertEquals(
        "No enum constant " + enumTypeName + "." + invalid,
        failure.getMessage()
      )
    }
  }

  @Test def valuesReturnsDefensiveCopiesInDeclarationOrder(): Unit = {
    val first = values()
    val second = values()
    assertNotSame(first, second)
    val original = first(0)
    first(0) = null.asInstanceOf[E]
    assertSame(original, second(0))
    assertSame(original, values()(0))
    assertSame(original, lookup(expectedNames(0)))
  }
}

class LinkOptionTest extends FileOptionEnumTest[LinkOption] {
  protected def expectedNames: Array[String] = Array("NOFOLLOW_LINKS")
  protected def enumTypeName: String = "java.nio.file.LinkOption"
  protected def values(): Array[LinkOption] = LinkOption.values()
  protected def lookup(name: String): LinkOption = LinkOption.valueOf(name)
}

class StandardCopyOptionTest extends FileOptionEnumTest[StandardCopyOption] {
  protected def expectedNames: Array[String] =
    Array("REPLACE_EXISTING", "COPY_ATTRIBUTES", "ATOMIC_MOVE")
  protected def enumTypeName: String = "java.nio.file.StandardCopyOption"
  protected def values(): Array[StandardCopyOption] =
    StandardCopyOption.values()
  protected def lookup(name: String): StandardCopyOption =
    StandardCopyOption.valueOf(name)
}

class StandardOpenOptionTest extends FileOptionEnumTest[StandardOpenOption] {
  protected def expectedNames: Array[String] = Array(
    "READ",
    "WRITE",
    "APPEND",
    "TRUNCATE_EXISTING",
    "CREATE",
    "CREATE_NEW",
    "DELETE_ON_CLOSE",
    "SPARSE",
    "SYNC",
    "DSYNC"
  )
  protected def enumTypeName: String = "java.nio.file.StandardOpenOption"
  protected def values(): Array[StandardOpenOption] =
    StandardOpenOption.values()
  protected def lookup(name: String): StandardOpenOption =
    StandardOpenOption.valueOf(name)
}
