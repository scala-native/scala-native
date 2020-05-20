package org.scalajs
package testinterface

import scala.scalanative.reflect.Reflect

@deprecated(message = "Use scala.scalanative.reflect.Reflect instead.",
            since = "0.4.0")
object TestUtils {

  def newInstance(fqcn: String, loader: ClassLoader)(
      args: Seq[AnyRef]): AnyVal =
    newInstance(name, loader, Seq.fill(args.length)(null))(args)

  def newInstance(fqcn: String, loader: ClassLoader, paramTypes: Seq[Class[_]])(
      args: Seq[Any]): AnyVal = {
    require(args.size == paramTypes.size, "argument count mismatch")

    Reflect
      .lookupInstantiatableClass(fqcn)
      .getOrElse(throw new Exception(s"instantiatable class not found: $fqcn"))
      .getConstructor(paramTypes: _*)
      .getOrElse(throw new Exception(s"constructor not found: $paramTypes"))
      .newInstance(args: _*)
  }

  def loadModule(fqcn: String, loader: ClassLoader): AnyVal = {
    Reflect
      .lookupLoadableModuleClass(fqcn)
      .getOrElse(throw new Exception(""))
      .loadModule()
  }
}
