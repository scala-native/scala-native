package scala.scalanative.testinterface

// Ported from Scala.js

import java.io.InputStream
import java.net.URL
import java.util as ju

private[testinterface] final class ScalaNativeClassLoader
    extends ClassLoader(null) {
  private def nimp: Nothing = {
    throw new NotImplementedError("""A ScalaNativeClassLoader is a dummy.
        |Use scala.scalanative.reflect.Reflect
        |to instantiate classes/objects.""".stripMargin)
  }

  override def clearAssertionStatus(): Unit = nimp
  override def getResource(name: String): URL = nimp
  override def getResourceAsStream(name: String): InputStream = nimp
  override def getResources(name: String): ju.Enumeration[URL] = nimp
  override def loadClass(name: String): Class[?] = nimp
  override def setClassAssertionStatus(
      className: String,
      enabled: Boolean
  ): Unit = nimp
  override def setDefaultAssertionStatus(enabled: Boolean): Unit = nimp
  override def setPackageAssertionStatus(
      packageName: String,
      enabled: Boolean
  ): Unit = nimp
}
