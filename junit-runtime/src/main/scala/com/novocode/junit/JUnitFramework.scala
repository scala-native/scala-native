package com.novocode.junit

// Ported from Scala.js

import sbt.testing.*

/** Forwarder framework so no additional framework name is needed in sbt.
 *
 *  Note that a type alias is not enough, since sbt looks at the runtime type.
 */
final class JUnitFramework extends Framework {
  private val f = new scala.scalanative.junit.JUnitFramework

  override def name(): String = f.name()

  def fingerprints(): Array[Fingerprint] = f.fingerprints()

  def runner(
      args: Array[String],
      remoteArgs: Array[String],
      testClassLoader: ClassLoader
  ): Runner = {
    f.runner(args, remoteArgs, testClassLoader)
  }

  def slaveRunner(
      args: Array[String],
      remoteArgs: Array[String],
      testClassLoader: ClassLoader,
      send: String => Unit
  ): Runner = {
    f.slaveRunner(args, remoteArgs, testClassLoader, send)
  }
}
