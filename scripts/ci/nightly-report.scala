//> using scala 3.9
//> using toolkit default
//> using file gh.scala

/** Reports results of a scheduled CI run as GitHub issues.
 *
 *  Failed jobs are re-run once first. Then each failing job gets one rolling
 *  issue (label `ci-nightly`), closed once the job passes again.
 *
 *  Environment: GH_TOKEN, GH_REPO, RUN_ID, RUN_ATTEMPT, HEAD_SHA, RUN_URL
 */

val Label = "ci-nightly"
val TitlePrefix = "Nightly CI failure: "
// Fails with any other job
val IgnoredJobs = Set("CI OK")
val FailedConclusions = Set("failure", "timed_out", "cancelled")

case class Job(name: String, conclusion: Option[String])

def jobs(repo: String, runId: String): Seq[Job] =
  runJobs(repo, runId).map(j => Job(j("name").str, j("conclusion").strOpt))

@main def run(): Unit =
  val repo = env("GH_REPO")
  val runId = env("RUN_ID")
  val attempt = env("RUN_ATTEMPT").toInt
  val sha = env("HEAD_SHA")
  val runUrl = env("RUN_URL")

  val executed = jobs(repo, runId)
    .filterNot(job => IgnoredJobs(job.name))
    .filter(_.conclusion.exists(_ != "skipped"))
  val failed = executed
    .filter(_.conclusion.exists(FailedConclusions))
    .map(_.name)
    .sorted
  if failed.isEmpty && executed.forall(_.name == "Plan") then
    println("Nightly was skipped (no new commits), nothing to report")
    return

  val passed =
    executed.filter(_.conclusion.contains("success")).map(_.name).toSet
  println(s"Attempt $attempt: ${executed.size} jobs, ${failed.size} failed")

  if failed.nonEmpty && attempt == 1 then
    println(s"Re-running failed jobs once before reporting:")
    failed.foreach(name => println(s"  $name"))
    gh(s"run rerun $runId --failed")
    return

  val label = s"$Label --force --color B60205"
  gh(s"label create $label", "description" -> "Failure in a scheduled CI run")
  val issues = s"issue list --label $Label --state open --limit 200"
  val openIssues: Map[String, Int] =
    ghJson(s"$issues --json number,title").arr
      .map(issue => issue("title").str -> issue("number").num.toInt)
      .collect {
        case (title, number) if title.startsWith(TitlePrefix) =>
          title.stripPrefix(TitlePrefix) -> number
      }
      .toMap

  for job <- failed do
    val title = TitlePrefix + job
    val body =
      s"Job `$job` failed in the [scheduled CI run]($runUrl) for $sha (also after a re-run)."
    openIssues.get(job) match
      case Some(number) =>
        gh(s"issue comment $number", "body" -> body)
        println(s"Commented on #$number: $job")
      case None =>
        gh(s"issue create --label $Label", "title" -> title, "body" -> body)
        println(s"Opened issue: $job")

  for (job, number) <- openIssues if passed(job) do
    val comment = s"Passing again in the [scheduled CI run]($runUrl) for $sha."
    gh(s"issue close $number", "comment" -> comment)
    println(s"Closed #$number: $job")
