package scala.scalanative.sbtplugin

import sbt.*
import sbt.Def.Initialize
import sbt.Keys.*
import sbt.util.CacheImplicits.given
import sbt.util.Digest

import scala.scalanative.sbtplugin.ScalaNativePlugin.autoImport.nativeLink

/** sbt 2 incremental test digests for Scala Native projects. */
private[scalanative] object NativeIncrementalTest {

  private lazy val definedTestNamesInput = taskKey[Vector[String]](
    "Defined test names used for incremental test digests"
  )

  private lazy val testOptionsDigest = taskKey[Seq[Digest]](
    "Content digest of Test/testOptions"
  )

  private lazy val testResourcesDigest = taskKey[Seq[Digest]](
    "Content digests of Test/resources"
  )

  private lazy val testEnvVarsDigest = taskKey[Digest](
    "Content digest of Test/envVars"
  )

  private lazy val extraTestDigestsInput = taskKey[Seq[Digest]](
    "Plugin-provided extra digests for incremental testing"
  )

  def nativeDefinedTestDigestsTask: Initialize[Task[Map[String, Digest]]] =
    Def.cachedTask {
      val testNames = definedTestNamesInput.value
      val opts = testOptionsDigest.value
      val rds = testResourcesDigest.value
      val extra = extraTestDigestsInput.value
      val binaryDigest =
        Digest.sha256Hash(nativeLink.value.contentHashStr().getBytes("UTF-8"))
      val envDigest = testEnvVarsDigest.value

      val sharedDigests: Seq[Digest] =
        Seq(binaryDigest, envDigest) ++ opts ++ rds ++ extra

      Map(testNames.map { name =>
        val nameDigest = Digest.sha256Hash(name.getBytes("UTF-8"))
        name -> Digest.sha256Hash((nameDigest +: sharedDigests)*)
      }*)
    }

  private def testOptionDigestsFromOptions(
      options: Seq[TestOption]
  ): Seq[Digest] =
    options.flatMap {
      case Tests.Setup(_, digest)   => Seq(digest)
      case Tests.Cleanup(_, digest) => Seq(digest)
      case Tests.Argument(fm, args) =>
        Seq(
          Digest.sha256Hash(
            (fm.toSeq.map(_.toString) ++ args)
              .mkString("\n")
              .getBytes("UTF-8")
          )
        )
      case _ => Nil
    }

  private def envVarsDigest(envVars: Map[String, String]): Digest = {
    val salt = envVars.toSeq
      .sortBy(_._1)
      .map { case (k, v) => s"$k=$v" }
      .mkString("\n")
    Digest.sha256Hash(salt.getBytes("UTF-8"))
  }

  val incrementalTestSettings: Seq[Setting[?]] = Seq(
    definedTestNamesInput := Def.uncached {
      definedTests.value.map(_.name).toVector.distinct
    },
    testOptionsDigest := Def.uncached {
      testOptionDigestsFromOptions(testOptions.value)
    },
    testResourcesDigest := Def.uncached {
      resources.value
        .map(_.toPath)
        .sortBy(_.toString)
        .map(Digest.sha256Hash(_))
    },
    testEnvVarsDigest := Def.uncached {
      envVarsDigest(envVars.value)
    },
    extraTestDigestsInput := Def.uncached {
      extraTestDigests.value
    },
    definedTestDigests := nativeDefinedTestDigestsTask
      .triggeredBy(nativeLink)
      .value
  )
}
