// Based on Ammonite script created by Tomasz Godzik in scalameta/metals https://github.com/scalameta/metals/commits/main/bin/merged_prs.sc
import $ivy.`org.kohsuke:github-api:1.114`

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConverters._
import scala.collection.mutable

import org.kohsuke.github.GitHubBuilder

import java.text.SimpleDateFormat
import java.util.Date

val defaultToken = sys.env.get("GITHUB_TOKEN")

@main
def main(
    firstTag: String,
    lastTag: String,
    githubToken: Option[String]
) = {
  val author = os.proc(List("git", "config", "user.name")).call().out.trim()
  val commits = os
    .proc(List("git", "rev-list", s"${firstTag}..${lastTag}"))
    .call()
    .out
    .trim()
    .linesIterator
    .size

  val contributors = os
    .proc(
      List("git", "shortlog", "-sn", "--no-merges", s"${firstTag}..${lastTag}")
    )
    .call()
    .out
    .trim()
    .linesIterator
    .toList

  val command = List(
    "git",
    "log",
    s"$firstTag..$lastTag",
    "--first-parent",
    "main",
    "--pretty=format:%H"
  )

  val token = githubToken.orElse(defaultToken).getOrElse {
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
      s"""|- ${pr.getTitle()}
          |  [\\#${pr.getNumber()}](${pr.getHtmlUrl()})
          |  ([$login](https://github.com/$login))""".stripMargin
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
    os.pwd / os.up / "docs" / "changelog" / s"$today-release-$lastTag.md"
  os.write(pathToReleaseNotes, releaseNotes)
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
  s"""|
      |# Release $lastTag ($today)
      |
      |We're happy to announce the release of Scala Native $lastTag, which
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
      |  <tr>
      |    <td>Closed issues</td>
      |    <td align="center"></td>
      |  </tr>
      |  <tr>
      |    <td>New features</td>
      |    <td align="center"></td>
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
      |${contributos.mkString("\n")}
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
