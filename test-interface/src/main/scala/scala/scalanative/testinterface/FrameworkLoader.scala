package scala.scalanative.testinterface

import sbt.testing.Framework

import scala.scalanative.reflect.Reflect

private[testinterface] object FrameworkLoader {
  def loadFramework(frameworkName: String): Framework = {
    Reflect
      .lookupInstantiatableClass(frameworkName)
      .getOrElse(throw new InstantiationError(frameworkName))
      .newInstance()
      .asInstanceOf[Framework]
  }
}
