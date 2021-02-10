// Ported from Scala.js commit: 060c3397 dated: 2021-02-09

package scala.tools.partest.scalanative

import sbt.testing.{AnnotatedFingerprint, Fingerprint}

object Framework {
  // as partest is not driven by test classes discovered by sbt, need to add this marker fingerprint to definedTests
  val fingerprint = new AnnotatedFingerprint {
    def isModule = true; def annotationName = "partest"
  }

  // TODO how can we export `fingerprint` so that a user can just add this to their build.sbt
  // definedTests in Test += new sbt.TestDefinition("partest", fingerprint, true, Array())
}

class Framework extends sbt.testing.Framework {
  def fingerprints: Array[Fingerprint] =
    Array[Fingerprint](Framework.fingerprint)
  def name: String = "partest"

  def runner(args: Array[String],
             remoteArgs: Array[String],
             testClassLoader: ClassLoader): sbt.testing.Runner =
    Runner(args, remoteArgs, testClassLoader)
}
