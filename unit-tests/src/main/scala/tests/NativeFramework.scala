package tests

import sbt.testing.{Fingerprint, Framework, Runner}
import scala.scalanative.testinterface.PreloadedClassLoader

class NativeFramework extends Framework {
  override def name(): String = "Native Test Framework"

  override def fingerprints(): Array[Fingerprint] =
    Array(NativeFingerprint)

  override def runner(args: Array[String],
                      remoteArgs: Array[String],
                      testClassLoader: ClassLoader): Runner =
    testClassLoader match {
      case pcl: PreloadedClassLoader =>
        new NativeRunner(args, remoteArgs, pcl)
      case _ =>
        throw new Exception("This test framework cannot be used on the JVM.")
    }
}
