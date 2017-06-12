package org.scalajs.testinterface

import scala.scalanative.testinterface.PreloadedClassLoader

// ScalaJS defines this to mimic reflection.
// We substitute it with our implementation, expecting that
// the `ClassLoader` that we'll receive is a `PreloadedClassLoader`,
// from which we'll directly get the instance we're interested in.
object TestUtils {

  def newInstance(name: String, loader: ClassLoader)(
      args: Seq[AnyRef]): AnyRef =
    newInstance(name, loader, Seq.fill(args.length)(null))(args)

  def newInstance(name: String,
                  loader: ClassLoader,
                  paramTypes: Seq[Class[_]])(args: Seq[Any]): AnyRef = {
    require(args.size == paramTypes.size, "argument count mismatch")

    loader match {
      case l: PreloadedClassLoader => l.loadPreloaded(name)
      case other                   => throw new UnsupportedOperationException()
    }
  }

  def loadModule(name: String, loader: ClassLoader): AnyRef =
    loader match {
      case l: PreloadedClassLoader => l.loadPreloaded(name)
      case other =>
        val clazz = other.loadClass(name + "$")
        clazz.getField("MODULE$").get(null)
    }
}
