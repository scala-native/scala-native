package build

import sbt.Keys._
import sbt._

/** The User-Agent coursier sends, set in CI (see .github/actions/sbt-warmup) so
 *  that Maven Central does not rate limit it as generic traffic, see
 *  https://central.sonatype.org/faq/429-error/. Forked JVMs (tests, nested
 *  scripted sbt) do not inherit system properties, so forward them.
 */
object CoursierUserAgent extends AutoPlugin {
  override def requires = plugins.JvmPlugin
  override def trigger = allRequirements

  private val properties = Seq(
    // Read by coursier and by sbt 2's lm-coursier
    "coursier.http.agent",
    // Read by the coursier shaded into lm-coursier
    "lmcoursier.internal.shaded.coursier.http.agent"
  )

  def javaOptions: Seq[String] =
    for {
      prop <- properties
      value <- sys.props.get(prop)
    } yield s"-D$prop=$value"

  override def projectSettings: Seq[Setting[?]] =
    if (javaOptions.isEmpty) Nil
    else Seq(Test / Keys.javaOptions ++= javaOptions)
}
