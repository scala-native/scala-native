// Test ported from scala-collection-compat repo, to test regression in Scala 3
/*
 * Scala (https://www.scala-lang.org)
 *
 * Copyright EPFL and Lightbend, Inc.
 *
 * Licensed under Apache License 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package scala.util

import scala.annotation.unused
import scala.reflect.ClassTag
import scala.util.control.ControlThrowable

import org.junit.Assert._
import org.junit.{Assume, BeforeClass, Test}

@deprecated("ThreadDeath is deprecated on JDK 20", "")
class UsingTestNew {
  import UsingTestNew._

  /* `Using.resource` exception preference */

  private def genericResourceThrowing[CloseT <: Throwable: ClassTag](
      resource: => CustomResource[CloseT],
      onLinkage: ThrowBehavior,
      onInterruption: ThrowBehavior,
      onControl: ThrowBehavior,
      onException: ThrowBehavior
  ): Unit = {
    def check[UseT <: Throwable: ClassTag](
        t: String => UseT,
        behavior: ThrowBehavior,
        allowsSuppression: Boolean
    ): Unit = {
      val ex = use(resource, t)
      if (behavior == UseIsThrown) {
        assertThrowableClass[UseT](ex)
        if (allowsSuppression) assertSingleSuppressed[CloseT](ex)
        else assertNoSuppressed(ex)
      } else {
        assertThrowableClass[CloseT](ex)
        if (behavior == CloseIsThrown) assertSingleSuppressed[UseT](ex)
        else assertNoSuppressed(ex)
      }
    }

    check(new UsingVMError(_), behavior = UseIsThrown, allowsSuppression = true)
    check(new UsingLinkageError(_), onLinkage, allowsSuppression = true)
    check(_ => new UsingInterruption, onInterruption, allowsSuppression = true)
    check(new UsingControl(_), onControl, allowsSuppression = false)
    check(new UsingError(_), onException, allowsSuppression = true)
    check(new UsingException(_), onException, allowsSuppression = true)
  }

  @Test
  def resourceThrowingVMError(): Unit = {
    genericResourceThrowing(
      new VMErrorResource,
      onLinkage = CloseIsThrown,
      onInterruption = CloseIsThrown,
      onControl = CloseIsThrown,
      onException = CloseIsThrown
    )
  }

  @Test
  def resourceThrowingLinkageError(): Unit = {
    genericResourceThrowing(
      new LinkageResource,
      onLinkage = UseIsThrown,
      onInterruption = CloseIsThrown,
      onControl = CloseIsThrown,
      onException = CloseIsThrown
    )
  }

  @Test
  def resourceThrowingInterruption(): Unit = {
    genericResourceThrowing(
      new InterruptionResource,
      onLinkage = UseIsThrown,
      onInterruption = UseIsThrown,
      onControl = CloseIsThrown,
      onException = CloseIsThrown
    )
  }

  @Test
  def resourceThrowingControl(): Unit = {
    genericResourceThrowing(
      new ControlResource,
      onLinkage = UseIsThrown,
      onInterruption = UseIsThrown,
      onControl = UseIsThrown,
      onException = UseIsThrown
    )
  }

  @Test
  def resourceThrowingError(): Unit = {
    genericResourceThrowing(
      new ErrorResource,
      onLinkage = UseIsThrown,
      onInterruption = UseIsThrown,
      onControl = CloseIsThrown,
      onException = UseIsThrown
    )
  }

  @Test
  def resourceThrowingException(): Unit = {
    genericResourceThrowing(
      new ExceptionResource,
      onLinkage = UseIsThrown,
      onInterruption = UseIsThrown,
      onControl = CloseIsThrown,
      onException = UseIsThrown
    )
  }

  /* `Using.apply` exception preference */

  private def genericUsingThrowing[CloseT <: Throwable: ClassTag](
      resource: => CustomResource[CloseT],
      onLinkage: ThrowBehavior,
      onInterruption: ThrowBehavior,
      onControl: ThrowBehavior,
      onException: ThrowBehavior
  ): Unit = {
    def check[UseT <: Throwable: ClassTag](
        t: String => UseT,
        behavior: ThrowBehavior,
        allowsSuppression: Boolean
    ): Unit = {
      val ex = useWrapped(resource, t)
      if (behavior == UseIsThrown) {
        assertThrowableClass[UseT](ex)
        if (allowsSuppression) assertSingleSuppressed[CloseT](ex)
        else assertNoSuppressed(ex)
      } else {
        assertThrowableClass[CloseT](ex)
        if (behavior == CloseIsThrown) assertSingleSuppressed[UseT](ex)
        else assertNoSuppressed(ex)
      }
    }

    check(new UsingVMError(_), behavior = UseIsThrown, allowsSuppression = true)
    check(new UsingLinkageError(_), onLinkage, allowsSuppression = true)
    check(_ => new UsingInterruption, onInterruption, allowsSuppression = true)
    check(new UsingControl(_), onControl, allowsSuppression = false)
    check(new UsingError(_), onException, allowsSuppression = true)
    check(new UsingException(_), onException, allowsSuppression = true)
  }

  @Test
  def usingThrowingVMError(): Unit = {
    genericUsingThrowing(
      new VMErrorResource,
      onLinkage = CloseIsThrown,
      onInterruption = CloseIsThrown,
      onControl = CloseIsThrown,
      onException = CloseIsThrown
    )
  }

  @Test
  def usingThrowingLinkageError(): Unit = {
    genericUsingThrowing(
      new LinkageResource,
      onLinkage = UseIsThrown,
      onInterruption = CloseIsThrown,
      onControl = CloseIsThrown,
      onException = CloseIsThrown
    )
  }

  @Test
  def usingThrowingInterruption(): Unit = {
    genericUsingThrowing(
      new InterruptionResource,
      onLinkage = UseIsThrown,
      onInterruption = UseIsThrown,
      onControl = CloseIsThrown,
      onException = CloseIsThrown
    )
  }

  @Test
  def usingThrowingControl(): Unit = {
    genericUsingThrowing(
      new ControlResource,
      onLinkage = UseIsThrown,
      onInterruption = UseIsThrown,
      onControl = UseIsThrown,
      onException = UseIsThrown
    )
  }

  @Test
  def usingThrowingError(): Unit = {
    genericUsingThrowing(
      new ErrorResource,
      onLinkage = UseIsThrown,
      onInterruption = UseIsThrown,
      onControl = CloseIsThrown,
      onException = UseIsThrown
    )
  }

  @Test
  def usingThrowingException(): Unit = {
    genericUsingThrowing(
      new ExceptionResource,
      onLinkage = UseIsThrown,
      onInterruption = UseIsThrown,
      onControl = CloseIsThrown,
      onException = UseIsThrown
    )
  }

  /* `Using.Manager.apply` exception preference */

  private def genericManagerThrowing[CloseT <: Throwable: ClassTag](
      resource: => CustomResource[CloseT],
      onLinkage: ThrowBehavior,
      onInterruption: ThrowBehavior,
      onControl: ThrowBehavior,
      onException: ThrowBehavior
  ): Unit = {
    def check[UseT <: Throwable: ClassTag](
        t: String => UseT,
        behavior: ThrowBehavior,
        allowsSuppression: Boolean
    ): Unit = {
      val ex = useManager(resource, t)
      if (behavior == UseIsThrown) {
        assertThrowableClass[UseT](ex)
        if (allowsSuppression) assertSingleSuppressed[CloseT](ex)
        else assertNoSuppressed(ex)
      } else {
        assertThrowableClass[CloseT](ex)
        if (behavior == CloseIsThrown) assertSingleSuppressed[UseT](ex)
        else assertNoSuppressed(ex)
      }
    }

    check(new UsingVMError(_), behavior = UseIsThrown, allowsSuppression = true)
    check(new UsingLinkageError(_), onLinkage, allowsSuppression = true)
    check(_ => new UsingInterruption, onInterruption, allowsSuppression = true)
    check(new UsingControl(_), onControl, allowsSuppression = false)
    check(new UsingError(_), onException, allowsSuppression = true)
    check(new UsingException(_), onException, allowsSuppression = true)
  }

  @Test
  def managerThrowingVMError(): Unit = {
    genericManagerThrowing(
      new VMErrorResource,
      onLinkage = CloseIsThrown,
      onInterruption = CloseIsThrown,
      onControl = CloseIsThrown,
      onException = CloseIsThrown
    )
  }

  @Test
  def managerThrowingLinkageError(): Unit = {
    genericManagerThrowing(
      new LinkageResource,
      onLinkage = UseIsThrown,
      onInterruption = CloseIsThrown,
      onControl = CloseIsThrown,
      onException = CloseIsThrown
    )
  }

  @Test
  def managerThrowingInterruption(): Unit = {
    genericManagerThrowing(
      new InterruptionResource,
      onLinkage = UseIsThrown,
      onInterruption = UseIsThrown,
      onControl = CloseIsThrown,
      onException = CloseIsThrown
    )
  }

  @Test
  def managerThrowingControl(): Unit = {
    genericManagerThrowing(
      new ControlResource,
      onLinkage = UseIsThrown,
      onInterruption = UseIsThrown,
      onControl = UseIsThrown,
      onException = UseIsThrown
    )
  }

  @Test
  def managerThrowingError(): Unit = {
    genericManagerThrowing(
      new ErrorResource,
      onLinkage = UseIsThrown,
      onInterruption = UseIsThrown,
      onControl = CloseIsThrown,
      onException = UseIsThrown
    )
  }

  @Test
  def managerThrowingException(): Unit = {
    genericManagerThrowing(
      new ExceptionResource,
      onLinkage = UseIsThrown,
      onInterruption = UseIsThrown,
      onControl = CloseIsThrown,
      onException = UseIsThrown
    )
  }

  /* nested resource usage returns the correct exception */

  private def checkMultiplePropagatesCorrectlySimple(
      usingException: Throwable
  ): Unit = {
    /*
    UsingException
     |- ClosingError
     |- ClosingException
     */
    assertThrowableClass[UsingException](usingException)
    val suppressed = usingException.getSuppressed
    assertEquals(suppressed.length, 2)
    val closingError = suppressed(0)
    val closingException = suppressed(1)
    assertThrowableClass[ClosingError](closingError)
    assertThrowableClass[ClosingException](closingException)
  }

  private def checkMultiplePropagatesCorrectlyComplex(
      vmError: Throwable
  ): Unit = {
    /*
    ClosingVMError
     |- UsingException
     |   |- ClosingError
     |- ClosingException
     */
    assertThrowableClass[ClosingVMError](vmError)
    val firstLevelSuppressed = vmError.getSuppressed
    assertEquals(firstLevelSuppressed.length, 2)
    val usingException = firstLevelSuppressed(0)
    val closingException = firstLevelSuppressed(1)
    assertThrowableClass[UsingException](usingException)
    assertThrowableClass[ClosingException](closingException)
    assertSingleSuppressed[ClosingError](usingException)
  }

  private def checkMultiplePropagatesCorrectlyExtremelyComplex(
      vmError: Throwable
  ): Unit = {
    /*
    ClosingVMError
     |- ClosingLinkageError
     |   |- ClosingInterruption
     |   |   |- UsingException
     |   |   |   |- ClosingError
     |   |   |- ClosingException
     |   |- ClosingError
     |   |- ClosingControl
     |- ClosingException
     */
    assertThrowableClass[ClosingVMError](vmError)

    val firstLevelSuppressed = vmError.getSuppressed
    assertEquals(firstLevelSuppressed.length, 2)
    val closingLinkage = firstLevelSuppressed(0)
    val closingException1 = firstLevelSuppressed(1)
    assertThrowableClass[ClosingLinkageError](closingLinkage)
    assertThrowableClass[ClosingException](closingException1)
    assertNoSuppressed(closingException1)

    val secondLevelSuppressed = closingLinkage.getSuppressed
    assertEquals(secondLevelSuppressed.length, 3)
    val closingInterruption = secondLevelSuppressed(0)
    val closingError2 = secondLevelSuppressed(1)
    val closingControl = secondLevelSuppressed(2)
    assertNoSuppressed(closingError2)
    assertNoSuppressed(closingControl)

    val thirdLevelSuppressed = closingInterruption.getSuppressed
    assertEquals(thirdLevelSuppressed.length, 2)
    val usingException = thirdLevelSuppressed(0)
    val closingException2 = thirdLevelSuppressed(1)
    assertSingleSuppressed[ClosingError](usingException)
    assertNoSuppressed(closingException2)
  }

  /* `Using.resource` nesting */

  @Test
  def resourceMultiplePropagatesCorrectlySimple(): Unit = {
    val usingException = catchThrowable {
      Using.resource(new ExceptionResource) { _ =>
        Using.resource(new ErrorResource) { _ =>
          throw new UsingException("nested `Using.resource`")
        }
      }
    }

    // uncomment to debug actual suppression nesting
    // usingException.printStackTrace()

    checkMultiplePropagatesCorrectlySimple(usingException)
  }

  @Test
  def resourceMultiplePropagatesCorrectlyComplex(): Unit = {
    val vmError = catchThrowable {
      Using.resource(new ExceptionResource) { _ =>
        Using.resource(new VMErrorResource) { _ =>
          Using.resource(new ErrorResource) { _ =>
            throw new UsingException("nested `Using.resource`")
          }
        }
      }
    }

    // uncomment to debug actual suppression nesting
    // vmError.printStackTrace()

    checkMultiplePropagatesCorrectlyComplex(vmError)
  }

  @Test
  def resourceMultiplePropagatesCorrectlyExtremelyComplex(): Unit = {
    val vmError = catchThrowable {
      Using.resource(new ExceptionResource) { _ =>
        Using.resource(new VMErrorResource) { _ =>
          Using.resource(new ControlResource) { _ =>
            Using.resource(new ErrorResource) { _ =>
              Using.resource(new LinkageResource) { _ =>
                Using.resource(new ExceptionResource) { _ =>
                  Using.resource(new InterruptionResource) { _ =>
                    Using.resource(new ErrorResource) { _ =>
                      throw new UsingException("nested `Using.resource`")
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    // uncomment to debug actual suppression nesting
    // vmError.printStackTrace()

    checkMultiplePropagatesCorrectlyExtremelyComplex(vmError)
  }

  /* `Using.apply` nesting */

  @Test
  def usingMultiplePropagatesCorrectlySimple(): Unit = {
    val scala.util.Failure(usingException) =
      Using(new ExceptionResource) { _ =>
        Using(new ErrorResource) { _ =>
          throw new UsingException("nested `Using`")
        }.get
      }: @unchecked

    // uncomment to debug actual suppression nesting
    // usingException.printStackTrace()

    checkMultiplePropagatesCorrectlySimple(usingException)
  }

  @Test
  def usingMultiplePropagatesCorrectlyComplex(): Unit = {
    val vmError = catchThrowable {
      Using(new ExceptionResource) { _ =>
        Using(new VMErrorResource) { _ =>
          Using(new ErrorResource) { _ =>
            throw new UsingException("nested `Using`")
          }.get
        }.get
      }.get
    }

    // uncomment to debug actual suppression nesting
    // vmError.printStackTrace()

    checkMultiplePropagatesCorrectlyComplex(vmError)
  }

  @Test
  def usingMultiplePropagatesCorrectlyExtremelyComplex(): Unit = {
    val vmError = catchThrowable {
      Using(new ExceptionResource) { _ =>
        Using(new VMErrorResource) { _ =>
          Using(new ControlResource) { _ =>
            Using(new ErrorResource) { _ =>
              Using(new LinkageResource) { _ =>
                Using(new ExceptionResource) { _ =>
                  Using(new InterruptionResource) { _ =>
                    Using(new ErrorResource) { _ =>
                      throw new UsingException("nested `Using`")
                    }.get
                  }.get
                }.get
              }.get
            }.get
          }.get
        }.get
      }.get
    }

    // uncomment to debug actual suppression nesting
    // vmError.printStackTrace()

    checkMultiplePropagatesCorrectlyExtremelyComplex(vmError)
  }

  /* `Using.Manager.apply` nesting */

  @Test
  def managerMultipleResourcesPropagatesCorrectlySimple(): Unit = {
    val scala.util.Failure(usingException) = Using.Manager { m =>
      @unused val _r1 = m(new ExceptionResource)
      @unused val _r2 = m(new ErrorResource)
      throw new UsingException("`Using.Manager`")
    }: @unchecked

    // uncomment to debug actual suppression nesting
    // usingException.printStackTrace()

    checkMultiplePropagatesCorrectlySimple(usingException)
  }

  @Test
  def managerMultipleResourcesPropagatesCorrectlyComplex(): Unit = {
    val vmError = catchThrowable {
      Using.Manager { m =>
        @unused val _r1 = m(new ExceptionResource)
        @unused val _r2 = m(new VMErrorResource)
        @unused val _r3 = m(new ErrorResource)
        throw new UsingException("`Using.Manager`")
      }
    }

    // uncomment to debug actual suppression nesting
    // vmError.printStackTrace()

    checkMultiplePropagatesCorrectlyComplex(vmError)
  }

  @Test
  def managerMultiplePropagatesCorrectlyExtremelyComplex(): Unit = {
    val vmError = catchThrowable {
      Using.Manager { m =>
        @unused val _r1 = m(new ExceptionResource)
        @unused val _r2 = m(new VMErrorResource)
        @unused val _r3 = m(new ControlResource)
        @unused val _r4 = m(new ErrorResource)
        @unused val _r5 = m(new LinkageResource)
        @unused val _r6 = m(new ExceptionResource)
        @unused val _r7 = m(new InterruptionResource)
        @unused val _r8 = m(new ErrorResource)
        throw new UsingException("`Using.Manager`")
      }
    }

    // uncomment to debug actual suppression nesting
    // vmError.printStackTrace()

    checkMultiplePropagatesCorrectlyExtremelyComplex(vmError)
  }

  /* works when throwing no exceptions */

  @Test
  def resourceWithNoThrow(): Unit = {
    val res = Using.resource(new NoOpResource) { _.identity("test") }
    assertEquals(res, "test")
  }

  @Test
  def usingWithNoThrow(): Unit = {
    val res = Using(new NoOpResource) { _.identity("test") }
    assertEquals(res, scala.util.Success("test"))
  }

  @Test
  def managerWithNoThrow(): Unit = {
    val res = Using.Manager { m => m(new NoOpResource).identity("test") }
    assertEquals(res, scala.util.Success("test"))
  }

  /* works when only throwing one exception */

  @Test
  def resourceOpThrow(): Unit = {
    val ex = use(new NoOpResource, new UsingException(_))
    assertThrowableClass[UsingException](ex)
  }

  @Test
  def usingOpThrow(): Unit = {
    val ex = useWrapped(new NoOpResource, new UsingException(_))
    assertThrowableClass[UsingException](ex)
  }

  @Test
  def managerOpThrow(): Unit = {
    val ex = useManager(new NoOpResource, new UsingException(_))
    assertThrowableClass[UsingException](ex)
  }

  @Test
  def resourceClosingThrow(): Unit = {
    val ex = catchThrowable {
      Using.resource(new ExceptionResource)(_.identity("test"))
    }
    assertThrowableClass[ClosingException](ex)
  }

  @Test
  def usingClosingThrow(): Unit = {
    val ex = Using(new ExceptionResource)(_.identity("test")).failed.get
    assertThrowableClass[ClosingException](ex)
  }

  @Test
  def managerClosingThrow(): Unit = {
    val ex = Using
      .Manager { m =>
        m(new ExceptionResource).identity("test")
      }
      .failed
      .get
    assertThrowableClass[ClosingException](ex)
  }

  /* using multiple resources close in the correct order */

  @Test
  def resources2(): Unit = {
    val group = new ResourceGroup
    val res = Using.resources(
      group.newResource(),
      group.newResource()
    ) { (r1, r2) =>
      r1.identity(1) + r2.identity(1)
    }
    assertEquals(res, 2)
    group.assertAllClosed()
  }

  @Test
  def resources3(): Unit = {
    val group = new ResourceGroup
    val res = Using.resources(
      group.newResource(),
      group.newResource(),
      group.newResource()
    ) { (r1, r2, r3) =>
      r1.identity(1) +
        r2.identity(1) +
        r3.identity(1)
    }
    assertEquals(res, 3)
    group.assertAllClosed()
  }

  @Test
  def resources4(): Unit = {
    val group = new ResourceGroup
    val res = Using.resources(
      group.newResource(),
      group.newResource(),
      group.newResource(),
      group.newResource()
    ) { (r1, r2, r3, r4) =>
      r1.identity(1) +
        r2.identity(1) +
        r3.identity(1) +
        r4.identity(1)
    }
    assertEquals(res, 4)
    group.assertAllClosed()
  }

  @Test
  def manager2(): Unit = {
    val group = new ResourceGroup
    val res = Using.Manager { m =>
      val r1 = m(group.newResource())
      val r2 = m(group.newResource())
      r1.identity(1) + r2.identity(1)
    }
    assertEquals(res, scala.util.Success(2))
    group.assertAllClosed()
  }

  @Test
  def manager3(): Unit = {
    val group = new ResourceGroup
    val res = Using.Manager { m =>
      val r1 = m(group.newResource())
      val r2 = m(group.newResource())
      val r3 = m(group.newResource())

      r1.identity(1) +
        r2.identity(1) +
        r3.identity(1)
    }
    assertEquals(res, scala.util.Success(3))
    group.assertAllClosed()
  }

  @Test
  def manager4(): Unit = {
    val group = new ResourceGroup
    val res = Using.Manager { m =>
      val r1 = m(group.newResource())
      val r2 = m(group.newResource())
      val r3 = m(group.newResource())
      val r4 = m(group.newResource())

      r1.identity(1) +
        r2.identity(1) +
        r3.identity(1) +
        r4.identity(1)
    }
    assertEquals(res, scala.util.Success(4))
    group.assertAllClosed()
  }

  /* misc */

  @Test
  def resourceDisallowsNull(): Unit = {
    val npe = catchThrowable(Using.resource(null: AutoCloseable)(_ => "test"))
    assertThrowableClass[NullPointerException](npe)
  }

  @Test
  def usingDisallowsNull(): Unit = {
    val npe = Using(null: AutoCloseable)(_ => "test").failed.get
    assertThrowableClass[NullPointerException](npe)
  }

  @Test
  def managerDisallowsNull(): Unit = {
    val npe = Using
      .Manager { m =>
        m(null: AutoCloseable)
        "test"
      }
      .failed
      .get
    assertThrowableClass[NullPointerException](npe)
  }

  @Test
  def usingCatchesOpeningException(): Unit = {
    val ex = Using({ throw new RuntimeException }: AutoCloseable)(_ =>
      "test"
    ).failed.get
    assertThrowableClass[RuntimeException](ex)
  }

  @Test
  def managerCatchesOpeningException(): Unit = {
    val ex = Using
      .Manager { m =>
        m({ throw new RuntimeException }: AutoCloseable)
        "test"
      }
      .failed
      .get
    assertThrowableClass[RuntimeException](ex)
  }
}

@deprecated("ThreadDeath is deprecated on JDK 20", "")
object UsingTestNew {
  @BeforeClass def checkRuntime(): Unit = {
    import org.scalanative.testsuite.utils.Platform
    // Implementation has changed in Scala 2.13.17, backported to 3.8.0
    // scalalib_3 is built against scalalib_2.13 using the last Scala version
    // instead of the one defined in natural scala3_library_3 dependenies
    def hasCompliantScalaVersion =
      Platform.scalaVersion
        .split('.')
        .take(3)
        .map(_.takeWhile(_.isDigit)) // becouse of versions like "3.4.0-RC1"
        .map(_.toInt) match {
        case Array(2, 13, patch) => patch >= 17
        case Array(3, major, _) => major >= 8 || Platform.executingInScalaNative
        case _                  => false
      }

    Assume.assumeTrue(
      s"Skipping UsingTestNew because the Scala version is not compliant: ${Platform.scalaVersion}",
      hasCompliantScalaVersion
    )
  }

  final class ClosingVMError(message: String)
      extends VirtualMachineError(message)
  final class UsingVMError(message: String) extends VirtualMachineError(message)
  final class ClosingLinkageError(message: String) extends LinkageError(message)
  final class UsingLinkageError(message: String) extends LinkageError(message)
  type ClosingInterruption = InterruptedException
  type UsingInterruption = ThreadDeath
  final class ClosingControl(message: String) extends ControlThrowable(message)
  final class UsingControl(message: String) extends ControlThrowable(message)
  final class ClosingError(message: String) extends Error(message)
  final class UsingError(message: String) extends Error(message)
  final class ClosingException(message: String) extends Exception(message)
  final class UsingException(message: String) extends Exception(message)

  abstract class BaseResource extends AutoCloseable {
    final def identity[A](a: A): A = a
  }

  final class NoOpResource extends BaseResource {
    override def close(): Unit = ()
  }

  abstract class CustomResource[T <: Throwable](t: String => T)
      extends BaseResource {
    override final def close(): Unit = throw t(
      "closing " + getClass.getSimpleName
    )
  }

  final class VMErrorResource extends CustomResource(new ClosingVMError(_))
  final class LinkageResource extends CustomResource(new ClosingLinkageError(_))
  final class InterruptionResource
      extends CustomResource(new ClosingInterruption(_))
  final class ControlResource extends CustomResource(new ClosingControl(_))
  final class ErrorResource extends CustomResource(new ClosingError(_))
  final class ExceptionResource extends CustomResource(new ClosingException(_))

  sealed trait ThrowBehavior

  /** resource use exception is thrown */
  case object UseIsThrown extends ThrowBehavior

  /** resource closing exception is thrown */
  case object CloseIsThrown extends ThrowBehavior

  def assertThrowableClass[T <: Throwable: ClassTag](t: Throwable): Unit = {
    assertEquals(
      s"Caught [${t.getMessage}]",
      implicitly[ClassTag[T]].runtimeClass,
      t.getClass
    )
  }

  def assertSingleSuppressed[T <: Throwable: ClassTag](t: Throwable): Unit = {
    val suppressed = t.getSuppressed
    assertEquals(1, suppressed.length)
    assertThrowableClass[T](suppressed(0))
  }

  def assertNoSuppressed(t: Throwable): Unit = {
    assertEquals(0, t.getSuppressed.length)
  }

  def catchThrowable(thunk: => Any): Throwable = {
    try {
      thunk
      throw new AssertionError("unreachable")
    } catch {
      case t: Throwable => t
    }
  }

  def use(resource: BaseResource, t: String => Throwable): Throwable =
    catchThrowable(Using.resource(resource)(opThrowing(t)))

  def useWrapped(resource: => BaseResource, t: String => Throwable): Throwable =
    try Using(resource)(opThrowing(t)).failed.get
    catch {
      case t: Throwable => t
    }

  def useManager(resource: => BaseResource, t: String => Throwable): Throwable =
    try {
      Using
        .Manager { m =>
          val r = m(resource)
          opThrowing(t)(r)
        }
        .failed
        .get
    } catch {
      case t: Throwable => t // this is awful
    }

  private def opThrowing(t: String => Throwable): BaseResource => Nothing =
    r => {
      r.identity("test")
      throw t("exception using resource")
    }

  final class ResourceGroup {
    // tracks the number of open resources
    private var openCount: Int = 0

    def newResource(): BaseResource = {
      openCount += 1
      new CountingResource(openCount)
    }

    def assertAllClosed(): Unit = assertEquals(openCount, 0)

    private final class CountingResource(countWhenCreated: Int)
        extends BaseResource {
      override def close(): Unit = {
        assertEquals(countWhenCreated, openCount)
        openCount -= 1
      }
    }
  }
}
