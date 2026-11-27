package build

import sbt.Keys._
import sbt._

/** Resolves from the mirror given by MAVEN_CENTRAL_MIRROR before Maven Central,
 *  used in CI where Maven Central rate limits requests. Maven Central stays as
 *  the fallback for releases the mirror has not synced yet. lm-coursier ignores
 *  the coursier.mirrors setting.
 */
object MavenMirror extends AutoPlugin {
  override def requires = plugins.JvmPlugin
  override def trigger = allRequirements

  override def projectSettings: Seq[Setting[?]] =
    sys.env.get("MAVEN_CENTRAL_MIRROR").filter(_.nonEmpty).toSeq.flatMap {
      url =>
        def useMirror(resolvers: Seq[Resolver]) = {
          val (central, others) = resolvers.partition {
            case repo: MavenRepository => repo.root.contains("repo1.maven.org")
            case _                     => false
          }
          (others :+ MavenRepository("maven-central-mirror", url)) ++ central
        }
        Seq(
          fullResolvers := Def.uncached(useMirror(fullResolvers.value)),
          // Used to fetch the compiler bridge
          scalaCompilerBridgeResolvers := Def.uncached(
            useMirror(scalaCompilerBridgeResolvers.value)
          )
        )
    }
}
