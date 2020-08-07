package com.novocode.junit

// Ported from Scala.js

import sbt.testing._

/** Forwarder framework so no additional framework name is needed in sbt.
 *
 *  Note that a type alias is not enough, since sbt looks at the runtime type.
 */
final class JUnitFramework extends Framework {
  private val f = new scala.scalanative.junit.JUnitFramework

  val name: String = f.name

  def fingerprints(): Array[Fingerprint] = f.fingerprints()

  def runner(args: Array[String],
             remoteArgs: Array[String],
             testClassLoader: ClassLoader): Runner = {
    f.runner(args, remoteArgs, testClassLoader)
  }

  /** Scala Native specific: Creates a worker runner for a given run.
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
  def slaveRunner(args: Array[String],
                  remoteArgs: Array[String],
                  testClassLoader: ClassLoader,
                  send: String => Unit): Runner = {
    f.slaveRunner(args, remoteArgs, testClassLoader, send)
  }
}
