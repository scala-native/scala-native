package scala.scalanative
package test

import sbt.testing.{Fingerprint, SubclassFingerprint}

final class FakeFingerprint(val original: Fingerprint)
    extends SubclassFingerprint {

  override def isModule(): Boolean = NativeFingerprint.isModule()

  override def superclassName(): String = NativeFingerprint.superclassName()

  override def requireNoArgConstructor(): Boolean =
    NativeFingerprint.requireNoArgConstructor()
}
