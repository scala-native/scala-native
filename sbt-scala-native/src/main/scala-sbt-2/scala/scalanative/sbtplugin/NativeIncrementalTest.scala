package scala.scalanative.sbtplugin

import sbt.*
import sbt.Def.Initialize
import sbt.Keys.*
import sbt.util.CacheImplicits.given
import sbt.util.Digest

import scala.scalanative.sbtplugin.ScalaNativePlugin.autoImport.nativeLink

/** sbt 2 incremental test digests for Scala Native projects. */
private[scalanative] object NativeIncrementalTest {

  def nativeDefinedTestDigestsTask: Initialize[Task[Map[String, Digest]]] =
    Def.cachedTask {
      val testNames = definedTests.value.map(_.name).toVector.distinct
      val opts = testOptionDigestsFromOptions(testOptions.value)
      val rds = (Test / resources).value
        .map(_.toPath)
        .sortBy(_.toString)
        .map(Digest.sha256Hash(_))
      val extra = extraTestDigests.value
      val binaryDigest =
        Digest.sha256Hash(nativeLink.value.contentHashStr().getBytes("UTF-8"))
      val envDigest = envVarsDigest((Test / envVars).value)

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
      case Tests.Setup(_, digest)    => Seq(digest)
      case Tests.Cleanup(_, digest)  => Seq(digest)
      case Tests.Argument(fm, args) =>
        Seq(
          Digest.sha256Hash(
            (fm.toSeq.map(_.toString) ++ args).mkString("\n").getBytes("UTF-8")
          )
        )
      case _ => Nil
    }

  private def envVarsDigest(envVars: Map[String, String]): Digest = {
    val salt = envVars.toSeq.sortBy(_._1).map { case (k, v) => s"$k=$v" }.mkString("\n")
    Digest.sha256Hash(salt.getBytes("UTF-8"))
  }

  val incrementalTestSettings: Seq[Setting[?]] = Seq(
    definedTestDigests := nativeDefinedTestDigestsTask
      .triggeredBy(nativeLink)
      .value
  )
}
