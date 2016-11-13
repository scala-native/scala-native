package scala.scalanative
package test

import sbt.testing.{Fingerprint, SubclassFingerprint}

class Framework extends sbt.testing.Framework {

  override def name(): String = "Native Testing Framework"

  override def fingerprints(): Array[Fingerprint] = Array(NativeFingerprint)

  override def runner(args: Array[String],
                      remoteArgs: Array[String],
                      testClassLoader: ClassLoader): sbt.testing.Runner = {
    new Runner(args, remoteArgs)
  }
}
