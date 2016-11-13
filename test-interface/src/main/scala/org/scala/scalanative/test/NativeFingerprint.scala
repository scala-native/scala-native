package scala.scalanative
package test

import sbt.testing.SubclassFingerprint

case object NativeFingerprint extends SubclassFingerprint {

  override def isModule(): Boolean = false

  override def superclassName(): String = "scala.scalanative.test.Test"

  override def requireNoArgConstructor(): Boolean = false

  override def equals(o: Any): Boolean = {
    // We will likely compare instances loaded from different classloaders
    o.getClass.getName == this.getClass.getName
  }

}
