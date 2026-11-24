package build

import sbt.Keys._
import sbt._

/** Replaces Maven Central with the mirror given by MAVEN_CENTRAL_MIRROR, used
 *  in CI where Maven Central rate limits requests. Coursier prefers artifacts
 *  of repositories with cached metadata, so Maven Central can't stay as a
 *  fallback. lm-coursier ignores the coursier.mirrors setting.
 */
object MavenMirror extends AutoPlugin {
  override def requires = plugins.JvmPlugin
  override def trigger = allRequirements

  override def projectSettings: Seq[Setting[?]] =
    sys.env.get("MAVEN_CENTRAL_MIRROR").filter(_.nonEmpty).toSeq.flatMap {
      url =>
        def useMirror(resolvers: Seq[Resolver]) =
          resolvers.filterNot {
            case repo: MavenRepository => repo.root.contains("repo1.maven.org")
            case _                     => false
          } :+ MavenRepository("maven-central-mirror", url)
        Seq(
          fullResolvers := Def.uncached(useMirror(fullResolvers.value)),
          // Used to fetch the compiler bridge
          scalaCompilerBridgeResolvers := Def.uncached(
            useMirror(scalaCompilerBridgeResolvers.value)
          )
        )
    }
}
