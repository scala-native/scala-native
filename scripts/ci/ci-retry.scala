//> using scala 3.9
//> using toolkit default
//> using file gh.scala

/** Re-runs a CI run when GitHub cancelled some of its jobs because no hosted
 *  runner picked them up, e.g. macOS arm64 runners at capacity.
 *
 *  Environment: GH_TOKEN, GH_REPO, RUN_ID, RUN_ATTEMPT, MAX_ATTEMPTS
 */

val NotAcquired = "was not acquired by Runner"

@main def run(): Unit =
  val repo = env("GH_REPO")
  val runId = env("RUN_ID")
  val attempt = env("RUN_ATTEMPT").toInt
  val maxAttempts = env("MAX_ATTEMPTS").toInt

  val cancelled =
    runJobs(repo, runId).filter(_("conclusion").strOpt.contains("cancelled"))

  val notAcquired = cancelled
    .filter { job =>
      val id = job("id").num.toLong
      ghJson(s"api repos/$repo/check-runs/$id/annotations").arr
        .exists(_("message").str.contains(NotAcquired))
    }
    .map(_("name").str)

  if notAcquired.isEmpty then println("No jobs cancelled for lack of runners")
  else if attempt >= maxAttempts then
    println(s"Attempt $attempt of $maxAttempts, not re-running:")
    notAcquired.foreach(name => println(s"  $name"))
  else
    println(s"Attempt $attempt, re-running failed jobs. Jobs without a runner:")
    notAcquired.foreach(name => println(s"  $name"))
    gh(s"run rerun $runId --failed")
