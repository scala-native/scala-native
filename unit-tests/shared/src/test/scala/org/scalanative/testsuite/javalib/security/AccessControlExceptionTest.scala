package org.scalanative.testsuite.javalib.security

import java.security.{AccessControlException, Permission}

import org.junit.Assert._
import org.junit.Test

class AccessControlExceptionTest {
  import AccessControlExceptionTest.TestPermission

  @Test def messageOnly(): Unit = {
    val e = new AccessControlException("denied")
    assertEquals("denied", e.getMessage())
    assertNull(e.getPermission())
    assertNull(e.getCause())
    assertTrue(e.isInstanceOf[SecurityException])
  }

  @Test def messageAndPermission(): Unit = {
    val p = new TestPermission("file", "read")
    val e = new AccessControlException("denied", p)
    assertEquals("denied", e.getMessage())
    assertSame(p, e.getPermission())

    val noMessage = new AccessControlException(null, p)
    assertNull(noMessage.getMessage())
    assertSame(p, noMessage.getPermission())
  }

  @Test def catchAsSecurityException(): Unit = {
    val caught =
      try throw new AccessControlException("denied")
      catch { case e: SecurityException => e }
    assertEquals("denied", caught.getMessage())
  }

  @Test def permissionNameAndToString(): Unit = {
    val name = classOf[TestPermission].getName()
    val p = new TestPermission("file", "read")
    assertEquals("file", p.getName())
    assertEquals("(\"" + name + "\" \"file\" \"read\")", p.toString())
    assertEquals(
      "(\"" + name + "\" \"file\")",
      new TestPermission("file", "").toString()
    )
    assertEquals(
      "(\"" + name + "\" \"null\")",
      new TestPermission(null, null).toString()
    )
  }
}

object AccessControlExceptionTest {
  class TestPermission(name: String, actions: String) extends Permission(name) {
    def implies(permission: Permission): Boolean = permission eq this
    def getActions(): String = actions
    override def equals(obj: Any): Boolean = obj.asInstanceOf[AnyRef] eq this
    override def hashCode(): Int = System.identityHashCode(this)
  }
}
