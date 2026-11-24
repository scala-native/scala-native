// GitHub CLI helpers shared by the CI scripts

def env(name: String): String = sys.env.getOrElse(name, "")

/** Runs `gh <command> --<flag> <value>...`, the command is split on spaces */
def gh(command: String, flags: (String, String)*): String =
  val args =
    command.split(' ').toSeq ++ flags.flatMap((f, v) => Seq(s"--$f", v))
  os.proc("gh" +: args).call(stderr = os.Pipe).out.text()

def ghJson(command: String): ujson.Value =
  val out = gh(command)
  if out.trim.isEmpty then ujson.Null else ujson.read(out)

/** All elements of a paginated GitHub API endpoint */
def ghApiPages(
    endpoint: String,
    extract: ujson.Value => ujson.Value = identity
): Seq[ujson.Value] =
  val separator = if endpoint.contains('?') then '&' else '?'
  Iterator
    .from(1)
    .map(page =>
      extract(ghJson(s"api $endpoint${separator}per_page=100&page=$page"))
    )
    .map(_.arr.toSeq)
    .takeWhile(_.nonEmpty)
    .flatten
    .toSeq

/** Latest attempt of each job of a workflow run */
def runJobs(repo: String, runId: String): Seq[ujson.Value] =
  ghApiPages(s"repos/$repo/actions/runs/$runId/jobs?filter=latest", _("jobs"))
