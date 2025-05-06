package scala.scalanative
package junit

// Ported from Scala.js

import scala.concurrent.Future
import scala.scalanative.reflect.annotation._
import scala.util.Try

/** Scala Native internal JUnit bootstrapper.
 *
 *  This class is public due to implementation details. Only the junit compiler
 *  plugin may generate classes inheriting from it.
 *
 *  Relying on this trait directly is unspecified behavior.
 */
@EnableReflectiveInstantiation
trait Bootstrapper {
  def beforeClass(): Unit
  def afterClass(): Unit
  def before(instance: AnyRef): Unit
  def after(instance: AnyRef): Unit

  def testClassMetadata(): TestClassMetadata
  def tests(): Array[TestMetadata]
  def invokeTest(instance: AnyRef, name: String): Future[Try[Unit]]

  def newInstance(): AnyRef
}

/** Scala Native internal JUnit test metadata
 *
 *  This class is public due to implementation details. Only the junit compiler
 *  plugin may create instances of it.
 *
 *  Relying on this class directly is unspecified behavior.
 */
final class TestMetadata(
    val name: String,
    val ignored: Boolean,
    val annotation: org.junit.Test
)

/** Scala Native internal JUnit test class metadata
 *
 *  This class is public due to implementation details. Only the junit compiler
 *  plugin may create instances of it.
 *
 *  Relying on this class directly is unspecified behavior.
 */
final class TestClassMetadata(
    val ignored: Boolean
)
