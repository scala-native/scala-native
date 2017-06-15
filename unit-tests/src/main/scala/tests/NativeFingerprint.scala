package tests

import sbt.testing.SubclassFingerprint

object NativeFingerprint extends SubclassFingerprint {
  override def isModule: Boolean = true

  override def requireNoArgConstructor(): Boolean = false

  override def superclassName(): String = "tests.Suite"
}
