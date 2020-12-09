package scala.scalanative.testinterface.adapter

// Ported from Scala.js

import scala.scalanative.testinterface.common._
import sbt.testing._

private[adapter] final class FrameworkAdapter(info: FrameworkInfo,
                                              testAdapter: TestAdapter)
    extends Framework {

  val name: String = info.displayName

  def fingerprints: Array[Fingerprint] = info.fingerprints.toArray

  def runner(args: Array[String],
             remoteArgs: Array[String],
             testClassLoader: ClassLoader): Runner = {
    RunnerAdapter(testAdapter, info.implName, args, remoteArgs)
  }

  override def toString(): String = s"FrameworkAdapter($name)"
}
