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

  /** Scala Native specific: Creates a worker for a given run.
   *  Ported from Scala.js
   *  The worker may send a message to the controller runner by calling `send`.
   *
   *  Important: this method name cannot be changed.
   *  It must keep the name `slaveRunner` for compatibility with Scala.js.
   *  This is a public to-be-overridden API that testing frameworks must implement.
   *  Currently testing frameworks can have a unique implementation of Framework for JVM, JS and Native.
   *  If we change that method name, they will need a separate source file for Native.
   *
   *  Similarly, Scala.js cannot change the name of that method in its API either,
   *  since that would be a binary breaking that would break all published testing frameworks, which is not acceptable.
   *
   *  Introducing another method and deprecating this one as a forwarder is not an option,
   *  because testing frameworks must implement that method, not call it.)
   */
  override def slaveRunner(args: Array[String],
                           remoteArgs: Array[String],
                           testClassLoader: ClassLoader,
                           send: String => Unit): Runner = {
    new NativeRunner(args, remoteArgs)
  }
}
