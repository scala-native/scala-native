package scala.scalanative.junit.plugin

import dotty.tools.dotc.core
import scala.annotation.threadUnsafe
import scala.compiletime.uninitialized

import core.Contexts._
import core.StdNames._
import core.Symbols.{_, given}
import core.Types._

object JUnitDefinitions {
  private var cached: JUnitDefinitions = uninitialized
  private var lastContext: Context = uninitialized
  def defnJUnit(using ctx: Context): JUnitDefinitions = {
    if (lastContext != ctx) {
      cached = JUnitDefinitions()
      lastContext = ctx
    }
    cached
  }
}

/** Definitions required by JUnit plugin
 *
 *  Scala.js port based on dotty.tools.backend.sjs.JSDefinitions#junit from
 *  Scala release 3.1.0. It contains the same definitions as original class
 *  which cannot be used directly in Scala Native plugin
 */
final class JUnitDefinitions()(using ctx: Context) {
  // scalafmt: { maxColumn = 120}
  @threadUnsafe lazy val TestAnnotType: TypeRef = requiredClassRef("org.junit.Test")
  def TestAnnotClass(using Context): ClassSymbol = TestAnnotType.symbol.asClass

  @threadUnsafe lazy val BeforeAnnotType: TypeRef = requiredClassRef("org.junit.Before")
  def BeforeAnnotClass(using Context): ClassSymbol = BeforeAnnotType.symbol.asClass

  @threadUnsafe lazy val AfterAnnotType: TypeRef = requiredClassRef("org.junit.After")
  def AfterAnnotClass(using Context): ClassSymbol = AfterAnnotType.symbol.asClass

  @threadUnsafe lazy val BeforeClassAnnotType: TypeRef = requiredClassRef("org.junit.BeforeClass")
  def BeforeClassAnnotClass(using Context): ClassSymbol = BeforeClassAnnotType.symbol.asClass

  @threadUnsafe lazy val AfterClassAnnotType: TypeRef = requiredClassRef("org.junit.AfterClass")
  def AfterClassAnnotClass(using Context): ClassSymbol = AfterClassAnnotType.symbol.asClass

  @threadUnsafe lazy val IgnoreAnnotType: TypeRef = requiredClassRef("org.junit.Ignore")
  def IgnoreAnnotClass(using Context): ClassSymbol = IgnoreAnnotType.symbol.asClass

  @threadUnsafe lazy val BootstrapperType: TypeRef = requiredClassRef("scala.scalanative.junit.Bootstrapper")
  @threadUnsafe lazy val TestMetadataType: TypeRef = requiredClassRef("scala.scalanative.junit.TestMetadata")
  @threadUnsafe lazy val TestClassMetadataType: TypeRef = requiredClassRef("scala.scalanative.junit.TestClassMetadata")

  @threadUnsafe lazy val NoSuchMethodExceptionType: TypeRef = requiredClassRef("java.lang.NoSuchMethodException")

  @threadUnsafe lazy val FutureType: TypeRef = requiredClassRef("scala.concurrent.Future")
  def FutureClass(using Context): ClassSymbol = FutureType.symbol.asClass

  @threadUnsafe private lazy val FutureModule_successfulR = requiredModule("scala.concurrent.Future")
    .requiredMethodRef("successful")
  def FutureModule_successful(using Context): Symbol = FutureModule_successfulR.symbol

  @threadUnsafe private lazy val SuccessModule_applyR = requiredModule("scala.util.Success")
    .requiredMethodRef(nme.apply)
  def SuccessModule_apply(using Context): Symbol = SuccessModule_applyR.symbol
}
