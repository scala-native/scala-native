// Based on Ammonite script created by Tomasz Godzik in scalameta/metals https://github.com/scalameta/metals/commits/main/bin/merged_prs.sc
//> using dep org.kohsuke:github-api:1.330
//> using toolkit latest

import java.text.SimpleDateFormat
import java.util.Date

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

import org.kohsuke.github.GitHubBuilder

val defaultToken = sys.env.get("GITHUB_TOKEN")

@main
def main(
    firstTag: String,
    lastTag: String
) = {
  val author = os.proc(List("git", "config", "user.name")).call().out.trim()
  val commits = os
    .proc(List("git", "rev-list", s"${firstTag}..."))
    .call()
    .out
    .trim()
    .linesIterator
    .size

  val contributors = os
    .proc(
      List("git", "shortlog", "-sn", "--no-merges", s"${firstTag}..")
    )
    .call()
    .out
    .trim()
    .linesIterator
    .toList

  val command = List(
    "git",
    "log",
    s"$firstTag..",
    "--first-parent",
    "main",
    "--pretty=format:%H"
  )

  val token =
    defaultToken.getOrElse {
      throw new Exception("No github API token was specified")
    }

  val output = os.proc(command).call().out.trim()

  val gh = new GitHubBuilder()
    .withOAuthToken(token)
    .build()

  val foundPRs = mutable.Set.empty[Int]
  val mergedPRs = ListBuffer[String]()
  for {
    // group in order to optimize API
    searchSha <- output
      .split('\n')
      .grouped(5)
      .map(_.mkString("SHA ", " SHA ", ""))
    allMatching = gh
      .searchIssues()
      .q(s"repo:scala-native/scala-native type:pr $searchSha")
      .list()
    pr <- allMatching.toList().asScala.sortBy(_.getClosedAt()).reverse
    prNumber = pr.getNumber()
    if !foundPRs(prNumber)
  } {
    foundPRs += prNumber
    val login = pr.getUser().getLogin()
    val formattedPR =
      s"- ${pr.getTitle()} [#${pr.getNumber()}](${pr.getHtmlUrl()}) ([$login](https://github.com/$login))"
    mergedPRs += formattedPR
  }

  val releaseNotes =
    template(
      author,
      firstTag,
      lastTag,
      mergedPRs.reverse.toList,
      commits,
      contributors
    )

  val pathToReleaseNotes =
    os.pwd / "docs" / "changelog" / "0.5.x" / s"$lastTag.md"
  os.write.over(pathToReleaseNotes, releaseNotes)
}

def today: String = {
  val formatter = new SimpleDateFormat("yyyy-MM-dd");
  formatter.format(new Date());
}

def template(
    author: String,
    firstTag: String,
    lastTag: String,
    mergedPrs: List[String],
    commits: Int,
    contributos: List[String]
) = {
  val version = lastTag.stripPrefix("v")
  s"""|
      |# $version ($today)
      |
      |We're happy to announce the release of Scala Native $version, which ???
      |
      |
      |## Supported Scala versions
      |
      | Scala Binary Version | Supported Scala Versions |
      | -------------------- | ------------------------ |
      | 2.12 | 2.12.14 ... 2.12.20 |
      | 2.13 | 2.13.8 ... 2.13.17 |
      | 3    | 3.1.2 ... 3.1.3<br>3.2.0 ... 3.2.2<br>3.3.0 ... 3.3.7 LTS<br>3.4.0 ... 3.4.3<br>3.5.0 ... 3.5.2<br>3.6.2 ... 3.6.4<br>3.7.0 ... 3.7.3 |
      |
      |> Upon release of new Scala version (stable, or Scala 3 RC) version dependent artifacts would be published without a new release.
      |
      |<table>
      |<tbody>
      |  <tr>
      |    <td>Commits since last release</td>
      |    <td align="center">$commits</td>
      |  </tr>
      |  <tr>
      |    <td>Merged PRs</td>
      |    <td align="center">${mergedPrs.size}</td>
      |  </tr>
      |    <tr>
      |    <td>Contributors</td>
      |    <td align="center">${contributos.size}</td>
      |  </tr>
      |</tbody>
      |</table>
      |
      |## Contributors
      |
      |Big thanks to everybody who contributed to this release or reported an issue!
      |
      |```
      |$$ git shortlog -sn --no-merges $firstTag..$lastTag
      | ${contributos.mkString("\n")}
      |```
      |
      |## Merged PRs
      |
      |## [$lastTag](https://github.com/scala-native/scala-native/tree/$lastTag) (${today})
      |
      |[Full Changelog](https://github.com/scala-native/scala-native/compare/$firstTag...$lastTag)
      |
      |**Merged pull requests:**
      |
      |${mergedPrs.mkString("\n")}
      |""".stripMargin
}
