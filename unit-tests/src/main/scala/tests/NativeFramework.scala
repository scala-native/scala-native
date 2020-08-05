package tests

import sbt.testing.{Fingerprint, Framework, Runner}

class NativeFramework extends Framework {
  override def name(): String = "Native Test Framework"

  override def fingerprints(): Array[Fingerprint] =
    Array(NativeFingerprint)

  override def runner(args: Array[String],
                      remoteArgs: Array[String],
                      testClassLoader: ClassLoader): Runner = {
    new NativeRunner(args, remoteArgs)
  }

  /** Scala.js specific: Creates a slave runner for a given run.
   *
   * The slave may send a message to the master runner by calling `send`.
   */
  override def slaveRunner(args: Array[String],
                           remoteArgs: Array[String],
                           testClassLoader: ClassLoader,
                           send: String => Unit): Runner =
    new NativeRunner(args, remoteArgs)
}
