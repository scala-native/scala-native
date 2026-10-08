package scala.scalanative

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.api.CompilationFailedException

abstract class VarHandleLookupCompilerTestSupport {
  protected def compiles(source: String): Unit =
    NIRCompiler(_.compile(source.stripMargin))

  protected def rejected(source: String, diagnostic: String = ""): Unit = {
    val failure = assertThrows(
      classOf[CompilationFailedException],
      () => NIRCompiler(_.compile(source.stripMargin))
    )
    if (diagnostic.nonEmpty)
      assertTrue(failure.getMessage, failure.getMessage.contains(diagnostic))
  }
}

/** Native rejects invalid literal lookups, and inputs it cannot resolve safely.
 */
class VarHandleLookupValidationTests
    extends VarHandleLookupCompilerTestSupport {
  @Test def privateLookupCreatesSharedCompanionHandle(): Unit = compiles(
    """import java.lang.invoke.MethodHandles
      |class Owner {
      |  private var value: Int = 1
      |  def current: Int = value
      |}
      |object Owner {
      |  val handle = MethodHandles.privateLookupIn(classOf[Owner], MethodHandles.lookup())
      |    .findVarHandle(classOf[Owner], "value", Integer.TYPE)
      |  def increment(owner: Owner): Int = handle.getAndAdd(owner, 1)
      |}
      |"""
  )

  @Test def privateLookupRejectsNonliteralOwner(): Unit = rejected(
    """import java.lang.invoke.MethodHandles
      |class Owner {
      |  private var value: Int = 1
      |  def handle(owner: Class[_]) = MethodHandles.privateLookupIn(owner, MethodHandles.lookup())
      |    .findVarHandle(classOf[Owner], "value", Integer.TYPE)
      |}
      |"""
  )

  private def javaFieldLookup(
      field: String,
      samePackage: Boolean,
      owner: String = "Fields"
  ): Unit = {
    val packageName =
      if (samePackage) "scala.scalanative.varhandlefixtures" else "unrelated"
    val source =
      s"""package $packageName
         |import java.lang.invoke.MethodHandles
         |class Caller {
         |  def handle = MethodHandles.lookup().findVarHandle(
         |    classOf[scala.scalanative.varhandlefixtures.$owner], "$field", Integer.TYPE)
         |}""".stripMargin
    val fixtures = new java.io.File(
      classOf[
        varhandlefixtures.Fields
      ].getProtectionDomain.getCodeSource.getLocation.toURI
    )
    val classpath = sys.props("scalanative.nativeruntime.cp") +
      java.io.File.pathSeparator + fixtures.getAbsolutePath
    NIRCompiler(_.compile(source, Array("-classpath", classpath)))
  }

  @Test def rejectsPackagePrivateJavaFieldOutsidePackage(): Unit = {
    val error = assertThrows(
      classOf[CompilationFailedException],
      () => javaFieldLookup("packageValue", samePackage = false)
    )
    assertTrue(
      error.getMessage,
      error.getMessage.contains("cannot access field")
    )
  }

  @Test def acceptsPackagePrivateJavaFieldInsidePackage(): Unit =
    javaFieldLookup("packageValue", samePackage = true)

  @Test def acceptsProtectedJavaFieldInsidePackage(): Unit =
    javaFieldLookup("protectedValue", samePackage = true)

  @Test def acceptsPublicJavaFieldOutsidePackage(): Unit =
    javaFieldLookup("publicValue", samePackage = false)

  @Test def rejectsInaccessibleJavaOwner(): Unit =
    assertThrows(
      classOf[CompilationFailedException],
      () =>
        javaFieldLookup(
          "publicValue",
          samePackage = false,
          owner = "HiddenFields"
        )
    )

  @Test def acceptsPackagePrivateJavaOwnerInsidePackage(): Unit =
    javaFieldLookup("publicValue", samePackage = true, owner = "HiddenFields")

  @Test def instanceLookupIgnoresSameNamedCompanionField(): Unit = compiles(
    """import java.lang.invoke.MethodHandles
      |class Owner {
      |  private var value: Int = 1
      |  def handle = MethodHandles.lookup().findVarHandle(classOf[Owner], "value", Integer.TYPE)
      |}
      |object Owner { var value: Long = 2L }
      |"""
  )

  @Test def rejectsUnrelatedProtectedJavaField(): Unit = rejected(
    """import java.lang.invoke.MethodHandles
      |class Other {
      |  def handle = MethodHandles.lookup().findVarHandle(
      |    classOf[java.io.FilterInputStream], "in", classOf[java.io.InputStream])
      |}""",
    "cannot access field"
  )

  @Test def acceptsProtectedJavaFieldFromSubclass(): Unit = compiles(
    """import java.lang.invoke.MethodHandles
      |class Child extends java.io.FilterInputStream(null) {
      |  def handle = MethodHandles.lookup().findVarHandle(
      |    classOf[java.io.FilterInputStream], "in", classOf[java.io.InputStream])
      |}"""
  )

  @Test def staticLookupOutsideCompanionValidatesExactField(): Unit =
    scala3Only {
      rejected(
        """import java.lang.invoke.MethodHandles
        |class Owner
        |object Owner {
        |  @scala.annotation.static var first: Int = 1
        |  @scala.annotation.static var second: Long = 2L
        |}
        |object Other {
        |  val handle = MethodHandles.lookup().findStaticVarHandle(classOf[Owner], "second", Integer.TYPE)
        |}""",
        "type does not match"
      )
    }

  @Test def staticLookupOutsideCompanionResolvesExactField(): Unit =
    scala3Only {
      compiles(
        """import java.lang.invoke.MethodHandles
        |class Owner
        |object Owner {
        |  @scala.annotation.static var first: Int = 1
        |  @scala.annotation.static var second: Long = 2L
        |}
        |object Other {
        |  val handle = MethodHandles.lookup().findStaticVarHandle(classOf[Owner], "second", java.lang.Long.TYPE)
        |}"""
      )
    }

  private def ownerLookup(
      owner: String = "classOf[Owner]",
      name: String = "\"value\"",
      tpe: String = "Integer.TYPE"
  ): String =
    s"""import java.lang.invoke.MethodHandles
       |class Owner {
       |  private var value = 37
       |  def current: Int = value
       |  def handle = MethodHandles.lookup().findVarHandle($owner, $name, $tpe)
       |}""".stripMargin

  @Test def rejectsMissingField(): Unit =
    rejected(ownerLookup(name = "\"missing\""), "does not contain field")

  @Test def rejectsEmptyName(): Unit =
    rejected(ownerLookup(name = "\"\""), "does not contain field")

  @Test def rejectsGetterOnlyMember(): Unit = rejected(
    """import java.lang.invoke.MethodHandles
      |class Owner {
      |  def value: Int = 37
      |  def handle = MethodHandles.lookup().findVarHandle(classOf[Owner], "value", Integer.TYPE)
      |}""",
    "does not contain field"
  )

  @Test def rejectsNullOwner(): Unit = rejected(ownerLookup(owner = "null"))
  @Test def rejectsNullName(): Unit = rejected(ownerLookup(name = "null"))
  @Test def rejectsNullType(): Unit = rejected(ownerLookup(tpe = "null"))

  @Test def rejectsLookalikePrimitiveClassToken(): Unit = rejected(
    """import java.lang.invoke.MethodHandles
      |object Integer { val TYPE: Class[Int] = classOf[Int] }
      |class Owner {
      |  var value = 37
      |  def handle = MethodHandles.lookup().findVarHandle(classOf[Owner], "value", Integer.TYPE)
      |}"""
  )

  @Test def rejectsDynamicOwnerEvenWithPreciseClassType(): Unit = rejected(
    """import java.lang.invoke.MethodHandles
      |class Owner {
      |  private var value = 37
      |  def current: Int = value
      |  def handle(owner: Class[Owner]) =
      |    MethodHandles.lookup().findVarHandle(owner, "value", Integer.TYPE)
      |}"""
  )

  @Test def rejectsDynamicVariableTypeEvenWithPreciseClassType(): Unit =
    rejected(
      """import java.lang.invoke.MethodHandles
      |class Owner {
      |  private var value = 37
      |  def current: Int = value
      |  def handle(tpe: Class[Int]) =
      |    MethodHandles.lookup().findVarHandle(classOf[Owner], "value", tpe)
      |}"""
    )

  @Test def rejectsDynamicFieldName(): Unit = rejected(
    """import java.lang.invoke.MethodHandles
      |class Owner {
      |  private var value = 37
      |  def current: Int = value
      |  def handle(name: String) =
      |    MethodHandles.lookup().findVarHandle(classOf[Owner], name, Integer.TYPE)
      |}""",
    "field name must be a literal"
  )

  @Test def rejectsSideEffectingOwnerExpression(): Unit = rejected(
    """import java.lang.invoke.MethodHandles
      |class Owner {
      |  private var value = 37
      |  def current: Int = value
      |  var evaluations = 0
      |  def ownerClass(): Class[Owner] = { evaluations += 1; classOf[Owner] }
      |  def handle = MethodHandles.lookup().findVarHandle(ownerClass(), "value", Integer.TYPE)
      |}"""
  )

  @Test def rejectsUnprovenLookupPrivileges(): Unit = rejected(
    """import java.lang.invoke.{MethodHandles, VarHandle}
      |class Owner {
      |  private var value = 37
      |  def current: Int = value
      |  def handle(lookup: MethodHandles.Lookup): VarHandle =
      |    lookup.findVarHandle(classOf[Owner], "value", Integer.TYPE)
      |}"""
  )

  @Test def rejectsPrivateFieldFromUnrelatedClass(): Unit = rejected(
    """import java.lang.invoke.MethodHandles
      |class Owner { private var value = 37; def current: Int = value }
      |class Other {
      |  def handle = MethodHandles.lookup().findVarHandle(classOf[Owner], "value", Integer.TYPE)
      |}""",
    "cannot access field"
  )

  @Test def publicScalaGetterDoesNotMakeTheBackingFieldAccessible(): Unit =
    rejected(
      """import java.lang.invoke.MethodHandles
      |class Owner { var value = 37 }
      |class Other {
      |  def handle = MethodHandles.lookup().findVarHandle(classOf[Owner], "value", Integer.TYPE)
      |}""",
      "cannot access field"
    )

  @Test def referenceLookupRequiresExactDeclaredType(): Unit = {
    def source(token: String): String =
      s"""import java.lang.invoke.MethodHandles
         |class Owner {
         |  private var value: String = "text"
         |  def current: String = value
         |  def handle = MethodHandles.lookup().findVarHandle(classOf[Owner], "value", $token)
         |}"""
    compiles(source("classOf[String]"))
    rejected(source("classOf[AnyRef]"), "type does not match")
    rejected(source("classOf[CharSequence]"), "type does not match")
  }

  @Test def rejectsOtherPrimitiveTypes(): Unit = {
    val tokens = List(
      "java.lang.Boolean.TYPE",
      "java.lang.Byte.TYPE",
      "java.lang.Short.TYPE",
      "java.lang.Character.TYPE",
      "java.lang.Long.TYPE",
      "java.lang.Float.TYPE",
      "java.lang.Double.TYPE"
    )
    tokens.foreach(token =>
      rejected(ownerLookup(tpe = token), "type does not match")
    )
  }

  @Test def rejectsStaticLookupForInstanceField(): Unit = rejected(
    """import java.lang.invoke.MethodHandles
      |class Owner {
      |  private var value = 37
      |  def current: Int = value
      |  def handle = MethodHandles.lookup().findStaticVarHandle(classOf[Owner], "value", Integer.TYPE)
      |}""",
    "cannot target instance field"
  )

  @Test def explicitlyRejectsReadOnlyFieldInTheCurrentNativeSubset(): Unit =
    rejected(
      """import java.lang.invoke.MethodHandles
      |class Owner {
      |  val value: Int = 37
      |  def handle = MethodHandles.lookup().findVarHandle(classOf[Owner], "value", Integer.TYPE)
      |}""",
      "mutable field"
    )

  private def scala3Only(body: => Unit): Unit = {
    org.junit.Assume.assumeTrue(
      "Scala 3 @static field fixture",
      scala.util.Properties.versionNumberString.startsWith("3.")
    )
    body
  }

  @Test def resolvesEachOfMultipleStaticFields(): Unit = scala3Only {
    compiles(
      """import java.lang.invoke.MethodHandles
        |class Owner
        |object Owner {
        |  @scala.annotation.static var first: Int = 37
        |  @scala.annotation.static var second: Long = 38L
        |  val firstHandle = MethodHandles.lookup().findStaticVarHandle(classOf[Owner], "first", Integer.TYPE)
        |  val secondHandle = MethodHandles.lookup().findStaticVarHandle(classOf[Owner], "second", java.lang.Long.TYPE)
        |}"""
    )
  }

  @Test def rejectsMissingStaticFieldDespiteOtherStaticFields(): Unit =
    scala3Only {
      rejected(
        """import java.lang.invoke.MethodHandles
        |class Owner
        |object Owner {
        |  @scala.annotation.static var present: Int = 37
        |  val handle = MethodHandles.lookup().findStaticVarHandle(classOf[Owner], "missing", Integer.TYPE)
        |}""",
        "does not contain field"
      )
    }

  @Test def rejectsWrongTypeForSecondStaticField(): Unit = scala3Only {
    rejected(
      """import java.lang.invoke.MethodHandles
        |class Owner
        |object Owner {
        |  @scala.annotation.static var first: Int = 37
        |  @scala.annotation.static var second: Long = 38L
        |  val handle = MethodHandles.lookup().findStaticVarHandle(classOf[Owner], "second", Integer.TYPE)
        |}""",
      "type does not match"
    )
  }

  @Test def rejectsInstanceLookupForStaticField(): Unit = scala3Only {
    rejected(
      """import java.lang.invoke.MethodHandles
        |class Owner
        |object Owner {
        |  @scala.annotation.static var value: Int = 37
        |  val handle = MethodHandles.lookup().findVarHandle(classOf[Owner], "value", Integer.TYPE)
        |}""",
      "cannot target static field"
    )
  }
}
