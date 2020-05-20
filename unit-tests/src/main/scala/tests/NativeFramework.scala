package tests

import sbt.testing.{Fingerprint, Framework, Runner}

class NativeFramework extends Framework {
  override def name(): String = "Native Test Framework"

  override def fingerprints(): Array[Fingerprint] =
    Array(NativeFingerprint)

  override def runner(args: Array[String],
                      remoteArgs: Array[String],
                      testClassLoader: ClassLoader): Runner =
    new NativeRunner(args, remoteArgs)
}
