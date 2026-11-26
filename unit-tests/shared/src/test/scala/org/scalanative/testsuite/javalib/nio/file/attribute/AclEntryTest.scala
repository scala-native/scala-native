package org.scalanative.testsuite.javalib.nio.file.attribute

import java.nio.file.attribute._
import java.util.{EnumSet, HashSet, Set}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.javalib.nio.file.FileOptionEnumTest
import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class AclEntryTypeTest extends FileOptionEnumTest[AclEntryType] {
  protected def expectedNames: Array[String] =
    Array("ALLOW", "DENY", "AUDIT", "ALARM")
  protected def enumTypeName: String = "java.nio.file.attribute.AclEntryType"
  protected def values(): Array[AclEntryType] = AclEntryType.values()
  protected def lookup(name: String): AclEntryType = AclEntryType.valueOf(name)
}

class AclEntryFlagTest extends FileOptionEnumTest[AclEntryFlag] {
  protected def expectedNames: Array[String] = Array(
    "FILE_INHERIT",
    "DIRECTORY_INHERIT",
    "NO_PROPAGATE_INHERIT",
    "INHERIT_ONLY"
  )
  protected def enumTypeName: String = "java.nio.file.attribute.AclEntryFlag"
  protected def values(): Array[AclEntryFlag] = AclEntryFlag.values()
  protected def lookup(name: String): AclEntryFlag = AclEntryFlag.valueOf(name)
}

class AclEntryPermissionTest extends FileOptionEnumTest[AclEntryPermission] {
  protected def expectedNames: Array[String] = Array(
    "READ_DATA",
    "WRITE_DATA",
    "APPEND_DATA",
    "READ_NAMED_ATTRS",
    "WRITE_NAMED_ATTRS",
    "EXECUTE",
    "DELETE_CHILD",
    "READ_ATTRIBUTES",
    "WRITE_ATTRIBUTES",
    "DELETE",
    "READ_ACL",
    "WRITE_ACL",
    "WRITE_OWNER",
    "SYNCHRONIZE"
  )
  protected def enumTypeName: String =
    "java.nio.file.attribute.AclEntryPermission"
  protected def values(): Array[AclEntryPermission] =
    AclEntryPermission.values()
  protected def lookup(name: String): AclEntryPermission =
    AclEntryPermission.valueOf(name)

  @Test def aliases(): Unit = {
    import AclEntryPermission._
    assertSame(READ_DATA, LIST_DIRECTORY)
    assertSame(WRITE_DATA, ADD_FILE)
    assertSame(APPEND_DATA, ADD_SUBDIRECTORY)
    assertThrows(
      classOf[IllegalArgumentException],
      AclEntryPermission.valueOf("LIST_DIRECTORY")
    )
  }
}

class AclEntryTest {
  import AclEntryPermission._
  import AclEntryFlag._

  private case class Principal(name: String) extends UserPrincipal {
    def getName(): String = name
    override def toString(): String = s"Principal($name)"
  }

  private val bob = Principal("bob")

  private def entry(): AclEntry = AclEntry
    .newBuilder()
    .setType(AclEntryType.ALLOW)
    .setPrincipal(bob)
    .setPermissions(WRITE_DATA, READ_DATA)
    .setFlags(INHERIT_ONLY, FILE_INHERIT)
    .build()

  @Test def accessors(): Unit = {
    val e = entry()
    assertSame(AclEntryType.ALLOW, e.`type`())
    assertSame(bob, e.principal())
    assertEquals(EnumSet.of(READ_DATA, WRITE_DATA), e.permissions())
    assertEquals(EnumSet.of(FILE_INHERIT, INHERIT_ONLY), e.flags())
  }

  @Test def accessorsReturnCopies(): Unit = {
    val e = entry()
    e.permissions().clear()
    e.flags().clear()
    assertEquals(2, e.permissions().size())
    assertEquals(2, e.flags().size())
  }

  @Test def builderCopiesInputSets(): Unit = {
    val perms = new HashSet[AclEntryPermission]()
    perms.add(DELETE)
    val flags = new HashSet[AclEntryFlag]()
    flags.add(FILE_INHERIT)
    val builder = AclEntry
      .newBuilder()
      .setType(AclEntryType.DENY)
      .setPrincipal(bob)
      .setPermissions(perms)
      .setFlags(flags)
    perms.add(READ_ACL)
    flags.add(INHERIT_ONLY)
    val e = builder.build()
    assertEquals(EnumSet.of(DELETE), e.permissions())
    assertEquals(EnumSet.of(FILE_INHERIT), e.flags())
  }

  @Test def buildRequiresTypeAndPrincipal(): Unit = {
    val noType = assertThrows(
      classOf[IllegalStateException],
      AclEntry.newBuilder().setPrincipal(bob).build()
    )
    assertEquals("Missing type component", noType.getMessage())
    val noPrincipal = assertThrows(
      classOf[IllegalStateException],
      AclEntry.newBuilder().setType(AclEntryType.ALLOW).build()
    )
    assertEquals("Missing who component", noPrincipal.getMessage())
  }

  @Test def nullArgumentsAreRejected(): Unit = {
    val b = AclEntry.newBuilder()
    assertThrows(classOf[NullPointerException], b.setType(null))
    assertThrows(classOf[NullPointerException], b.setPrincipal(null))
    assertThrows(
      classOf[NullPointerException],
      b.setPermissions(null: Set[AclEntryPermission])
    )
    assertThrows(
      classOf[NullPointerException],
      b.setFlags(null: Set[AclEntryFlag])
    )
    assertThrows(classOf[NullPointerException], b.setPermissions(DELETE, null))
    assertThrows(classOf[NullPointerException], b.setFlags(FILE_INHERIT, null))
    val permsWithNull = new HashSet[AclEntryPermission]()
    permsWithNull.add(null)
    assertThrows(classOf[NullPointerException], b.setPermissions(permsWithNull))
    val flagsWithNull = new HashSet[AclEntryFlag]()
    flagsWithNull.add(null)
    assertThrows(classOf[NullPointerException], b.setFlags(flagsWithNull))
    assertThrows(classOf[NullPointerException], AclEntry.newBuilder(null))
  }

  @Test def wrongElementTypesAreRejected(): Unit = {
    val raw = new HashSet[AnyRef]()
    raw.add("x")
    assertThrows(
      classOf[ClassCastException],
      AclEntry
        .newBuilder()
        .setPermissions(raw.asInstanceOf[Set[AclEntryPermission]])
    )
    assertThrows(
      classOf[ClassCastException],
      AclEntry.newBuilder().setFlags(raw.asInstanceOf[Set[AclEntryFlag]])
    )
  }

  @Test def newBuilderFromEntry(): Unit = {
    val e = entry()
    val copy = AclEntry.newBuilder(e).build()
    assertEquals(e, copy)
    assertEquals(e.hashCode(), copy.hashCode())
    val denied = AclEntry.newBuilder(e).setType(AclEntryType.DENY).build()
    assertSame(AclEntryType.ALLOW, e.`type`())
    assertSame(AclEntryType.DENY, denied.`type`())
  }

  @Test def equalsAndHashCode(): Unit = {
    val e = entry()
    assertEquals(e, entry())
    assertEquals(e.hashCode(), entry().hashCode())
    assertEquals(
      e,
      AclEntry.newBuilder(e).setPrincipal(Principal("bob")).build()
    )
    assertNotEquals(
      e,
      AclEntry.newBuilder(e).setType(AclEntryType.DENY).build()
    )
    assertNotEquals(
      e,
      AclEntry.newBuilder(e).setPrincipal(Principal("x")).build()
    )
    assertNotEquals(e, AclEntry.newBuilder(e).setPermissions().build())
    assertNotEquals(e, AclEntry.newBuilder(e).setFlags().build())
    assertFalse(e.equals(null))
    assertFalse(e.equals("bob"))
  }

  @Test def toStringFormat(): Unit = {
    assertEquals(
      "bob:READ_DATA/WRITE_DATA:FILE_INHERIT/INHERIT_ONLY:ALLOW",
      entry().toString()
    )
    val base =
      AclEntry.newBuilder().setType(AclEntryType.AUDIT).setPrincipal(bob)
    assertEquals("bob:AUDIT", base.build().toString())
    assertEquals(
      "bob:DELETE:AUDIT",
      AclEntry
        .newBuilder(base.build())
        .setPermissions(DELETE)
        .build()
        .toString()
    )
    assertEquals(
      "bob:DIRECTORY_INHERIT:AUDIT",
      AclEntry
        .newBuilder(base.build())
        .setFlags(DIRECTORY_INHERIT)
        .build()
        .toString()
    )
    assertEquals(
      "bob:READ_DATA/WRITE_DATA/APPEND_DATA/READ_NAMED_ATTRS/" +
        "WRITE_NAMED_ATTRS/EXECUTE/DELETE_CHILD/READ_ATTRIBUTES/" +
        "WRITE_ATTRIBUTES/DELETE/READ_ACL/WRITE_ACL/WRITE_OWNER/SYNCHRONIZE:" +
        "FILE_INHERIT/DIRECTORY_INHERIT/NO_PROPAGATE_INHERIT/INHERIT_ONLY:ALARM",
      AclEntry
        .newBuilder()
        .setType(AclEntryType.ALARM)
        .setPrincipal(bob)
        .setPermissions(AclEntryPermission.values(): _*)
        .setFlags(AclEntryFlag.values(): _*)
        .build()
        .toString()
    )
  }
}
