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

  def detectFrameworkNames(names: List[List[String]]): List[Option[String]] = {
    def frameworkExists(name: String): Boolean = {
      Reflect.lookupInstantiatableClass(name).exists { clazz =>
        classOf[sbt.testing.Framework].isAssignableFrom(clazz.runtimeClass)
      }
    }

    for (frameworkNames <- names)
      yield frameworkNames.find(frameworkExists)
  }

  def tryLoadFramework(names: List[String]): Option[Framework] = {
    def tryLoad(name: String): Option[Framework] = {
      Reflect.lookupInstantiatableClass(name).collect {
        case clazz if classOf[Framework].isAssignableFrom(clazz.runtimeClass) =>
          clazz.newInstance().asInstanceOf[Framework]
      }
    }

    names.iterator.map(tryLoad).collectFirst {
      case Some(framework) => framework
    }
  }

}
