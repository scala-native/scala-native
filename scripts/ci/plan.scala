//> using scala 3.9
//> using toolkit default
//> using file gh.scala

/** Selects the CI jobs to run, used by the plan job of
 *  .github/workflows/ci.yml.
 *
 *  Each `Workflow` runs off, base (jobs without a condition) or extended (also
 *  `if: inputs.extended` jobs). Writes `tier`, `publish` and the level of each
 *  workflow to $GITHUB_OUTPUT.
 *
 *  Environment: EVENT_NAME, REF, REF_TYPE, GH_REPO, SHA, GH_TOKEN,
 *  GITHUB_OUTPUT; for pull requests PR_NUMBER, PR_DRAFT, PR_BODY, PR_LABELS
 *  (JSON list); DISPATCH_TIER for workflow_dispatch.
 */
import java.nio.file.{FileSystems, Paths}

import scala.util.matching.Regex

/** Path pattern matched against repository-relative paths */
sealed trait Pattern:
  def matches(path: String): Boolean

/** `*` matches within a path segment, `**` across segments */
final case class Glob(glob: String) extends Pattern:
  private val matcher = FileSystems.getDefault.getPathMatcher(s"glob:$glob")
  def matches(path: String): Boolean = matcher.matches(Paths.get(path))

final case class Re(regex: Regex) extends Pattern:
  def matches(path: String): Boolean = regex.findFirstIn(path).isDefined

def globs(patterns: String*): Seq[Pattern] = patterns.map(Glob(_))

extension (patterns: Seq[Pattern])
  def matchAny(path: String): Boolean = patterns.exists(_.matches(path))

case class PullRequest(files: Seq[String], labels: Set[String], body: String)

/** When a workflow runs on pull requests */
enum Trigger:
  case Always, Never
  case Changed(paths: Seq[Pattern])
  // label or marker in the pull request description
  case Requested(label: String, marker: String)

  def matches(pr: PullRequest): Boolean = this match
    case Always                   => true
    case Never                    => false
    case Changed(paths)           => pr.files.exists(paths.matchAny)
    case Requested(label, marker) =>
      pr.labels(label) || pr.body.contains(marker)

val toolchainPaths = globs(
  "tools/**",
  "nir/**",
  "util/**",
  "nscplugin/**",
  "junit-plugin/**",
  "test-runner/**",
  "test-interface*/**",
  "testing-compiler*/**",
  "scalalib/**",
  "scala3lib/**",
  "project/**"
)
val gcPaths = globs("nativelib/src/main/resources/scala-native/gc/**")
val nativeRuntimePaths = globs("nativelib/src/main/resources/**")
val posixPaths = globs(
  "posixlib/**",
  "clib/**",
  "nativelib/src/main/resources/scala-native/platform/posix/**"
)
val applePaths =
  Seq(Re("(?i)darwin|apple|macos|(^|[/_.-])ios([/_.-]|$)".r))
// Platforms tested on pull requests only for toolchain, GC or POSIX changes
val extraPlatformPaths = toolchainPaths ++ gcPaths ++ posixPaths

/** Workflows called from ci.yml and their pull request triggers. Nightly runs
 *  all of them extended.
 */
enum Workflow(
    val output: String,
    val base: Trigger,
    val extended: Trigger,
    // tested before publishing a release
    val release: Boolean = false
):
  case Toolchain
      extends Workflow(
        "toolchain",
        base = Trigger.Always,
        extended = Trigger.Changed(toolchainPaths)
      )
  case Linux
      extends Workflow(
        "linux",
        base = Trigger.Always,
        extended = Trigger.Changed(gcPaths),
        release = true
      )
  // Linux always, every OS for native runtime changes
  case GC
      extends Workflow(
        "gc",
        base = Trigger.Always,
        extended = Trigger.Changed(nativeRuntimePaths)
      )
  case MacOS
      extends Workflow(
        "macos",
        base = Trigger.Always,
        extended = Trigger.Never,
        release = true
      )
  case Windows
      extends Workflow(
        "windows",
        base = Trigger.Always,
        extended = Trigger.Never,
        release = true
      )
  case Apple
      extends Workflow(
        "apple",
        base = Trigger.Changed(extraPlatformPaths),
        extended = Trigger.Changed(applePaths)
      )
  case LinuxMultiarch
      extends Workflow(
        "linux-multiarch",
        base = Trigger.Changed(extraPlatformPaths),
        extended = Trigger.Never
      )
  case BSD
      extends Workflow(
        "bsd",
        base = Trigger.Changed(extraPlatformPaths),
        extended = Trigger.Changed(posixPaths)
      )
  case Termux
      extends Workflow(
        "termux",
        base = Trigger.Changed(extraPlatformPaths),
        extended = Trigger.Never
      )
  case SbtPlugin
      extends Workflow(
        "sbt-plugin",
        base = Trigger.Changed(
          globs(
            "sbt-scala-native/**",
            "scripted-tests/**",
            "project/**",
            "tools/**",
            "test-runner/**"
          )
        ),
        extended = Trigger.Never
      )
  case Partests
      extends Workflow(
        "partests",
        base = Trigger.Changed(
          globs("scala-partest*/**", "scripts/partest-check-files.scala")
        ),
        extended = Trigger.Requested("ci:partest", "[test-partest]")
      )

enum Level:
  case Off, Base, Extended

  def output: String = toString.toLowerCase

enum Tier:
  case Skip, Docs, Draft, PR, Nightly, Release

  def output: String = toString.toLowerCase

/** Changes to CI or the build run every workflow at its base level
 */
val fullPrPaths = globs(
  ".github/**",
  "ci-docker/**",
  "scripts/ci/**",
  "build.sbt",
  "project/*.sbt",
  "project/build.properties",
  "project/Build.scala",
  "project/Settings.scala",
  "project/Commands.scala",
  "project/MultiScalaProject.scala"
)

val docsPaths = globs("docs/**", "*.md", "**/*.md")

def changedFiles(repo: String, pr: String): Seq[String] =
  ghApiPages(s"repos/$repo/pulls/$pr/files").flatMap(f =>
    f("filename").str +: f.obj.get("previous_filename").map(_.str).toSeq
  )

def lastNightlySha(): Option[String] =
  val query = "--workflow ci.yml --event schedule --status completed --limit 1"
  try
    ghJson(s"run list $query --json headSha").arr.headOption
      .map(_("headSha").str)
  catch
    case e: os.SubprocessException =>
      // e.g. no scheduled run yet
      println(s"Cannot find the last nightly run: ${e.result.err.text().trim}")
      None

case class Plan(tier: Tier, levels: Map[Workflow, Level]):
  /** Snapshots from nightly runs on main, releases from tags */
  def publish: String = tier match
    case Tier.Release                                    => "release"
    case Tier.Nightly if env("REF") == "refs/heads/main" => "snapshot"
    case _                                               => "off"

object Plan:
  def all(tier: Tier, level: Level) =
    Plan(tier, Workflow.values.map(_ -> level).toMap)
  def nightly = all(Tier.Nightly, Level.Extended)
  def release = Plan(
    Tier.Release,
    Workflow.values
      .map(w => w -> (if w.release then Level.Base else Level.Off))
      .toMap
  )

  def pullRequest(pr: PullRequest): Plan =
    val fullPr = pr.files.isEmpty || pr.files.exists(fullPrPaths.matchAny)
    if fullPr then
      println("CI or build definition changed: running every workflow")
    Plan(
      Tier.PR,
      Workflow.values.map { w =>
        val level =
          if w.extended.matches(pr) then Level.Extended
          else if fullPr || w.base.matches(pr) then Level.Base
          else Level.Off
        w -> level
      }.toMap
    )

def plan(): Plan =
  env("EVENT_NAME") match
    case "schedule" =>
      val previous = lastNightlySha()
      if previous.contains(env("SHA")) then
        println(
          s"Nothing committed since the last nightly (${previous.get}), skipping"
        )
        Plan.all(Tier.Skip, Level.Off)
      else Plan.nightly

    case "push" if env("REF_TYPE") == "tag" => Plan.release

    case "workflow_dispatch" =>
      if env("DISPATCH_TIER") == Tier.PR.output then
        Plan.all(Tier.PR, Level.Base)
      else Plan.nightly

    case "pull_request" =>
      val body = env("PR_BODY")
      val labels = ujson
        .read(Option(env("PR_LABELS")).filter(_.nonEmpty).getOrElse("[]"))
        .arr
        .map(_.str)
        .toSet
      val pr = PullRequest(
        files = changedFiles(env("GH_REPO"), env("PR_NUMBER")),
        labels = labels,
        body = body
      )
      println(s"${pr.files.size} changed files")
      if Trigger.Requested("ci:full", "[test-extended]").matches(pr) then
        Plan.nightly
      else if env("PR_DRAFT") == "true" then Plan.all(Tier.Draft, Level.Off)
      else if pr.files.nonEmpty && pr.files.forall(docsPaths.matchAny) then
        Plan.all(Tier.Docs, Level.Off)
      else Plan.pullRequest(pr)

    case other =>
      sys.error(s"Unsupported event: $other")

@main def run(): Unit =
  val result = plan()
  val lines = s"tier=${result.tier.output}" +: s"publish=${result.publish}" +:
    Workflow.values.toSeq.map(w => s"${w.output}=${result.levels(w).output}")
  lines.foreach(println)
  os.write.append(os.Path(env("GITHUB_OUTPUT")), lines.mkString("", "\n", "\n"))
