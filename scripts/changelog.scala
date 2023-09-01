// Based on Ammonite script created by Tomasz Godzik in scalameta/metals https://github.com/scalameta/metals/commits/main/bin/merged_prs.sc
//> using dep org.kohsuke:github-api:1.316
//> using toolkit latest

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
    githubToken: String
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
    "0.4.x",
    "--pretty=format:%H"
  )

  val token =
    Option(githubToken).filter(_.nonEmpty).orElse(defaultToken).getOrElse {
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
    searchSha <- Seq(
      "3dd595bc1c5315d42178cc4a3f5434563ab32ea8",
      "ecdf41394758a5d0c59f853048df01caa77c3b47",
      "7460645f44bf69fdb0dd708efba8113eec53733a",
      "4c4006c6534699db3697bc376b65d05f5dab4aab",
      "ec2402fa4d9dbd1e51c01f680c3179f74ec40dc2",
      "3339fe40eb648ae87c19567935d22215464bcef7",
      "f0b390e4dacdefa74c04a8f663f93f6402ae70a5",
      "0a6d1368227b41339fc8cc38f099eab0539c766a",
      "8f28b34d1c88aff0cf03171cbae11f993fbacda5",
      "36d32f015d5a024edf307cbd3e68e8ee90753a5d",
      "2a0e7ec7f36068aaf859d5e8c68bf5ec5415f8a4",
      "3273166af3540b05d02c7200df1a09ba95093d7d",
      "b54e0698dc426d7164dac4fb709e4c16ef05aa36",
      "56861600e3c66e5afc61f23641251b09439441c2",
      "c4e9c81d84971ec0a80b018a060ef6acdb0a1ccd",
      "5b5db373b6636421d1d3218b84926cb9e024d695",
      "b1bba76a8dc42852d378e1fff0b4da9517522430",
      "04a19fe8a2a50cee7bbb331854a9668c787ca1c4",
      "513df72133187fb5dcf35a26d114459618922216",
      "9f6b9b835bfd4e7c8d5be3e8f32abfb49ef1eb14",
      "f3b1a31edcecafd3b40f94af2360d8fff98a753a",
      "bc5b65f88d319ca8059c06928feed19826a796f7",
      "0e181d49c6125caf1307cc3de5e043067e14f112",
      "3544ba66ad33729e35459e57e7bec717c91b89e4",
      "9e4775d0d21b90e492b5dc9216a59e5d98af2162",
      "61f4230a0bb7fe6c0fc58496ffddf4fd319debf0",
      "ce94cf563a17e162d4349ffdb1152e09028d999c",
      "f97ac0d307f81154b5ae9b5d808f013eed2f4504",
      "eb2caf722be4e513b97aa8c2a7d54c97e37e460c",
      "a65fe110edbde7c9c26343b99cfcc01808f8c387",
      "637972d8656f2323adcffb7c5acac7cd54d963ac",
      "789b5f4b8dce383ba7371a3a71bd3e4df017e087",
      "3bcc03e962b202b21515a2875e33050e756ea77d",
      "990e16e12fbdadc235df17126d0791ce9545f6db",
      "014f0addcac859c72af81274cd44d571d12eb159",
      "d51279a6fbe100f1762e8a45f2e0bae356045f05",
      "594e24fc967e75aea9997468b7cd7531e221c101",
      "6a9e7c9c49842576dd1a760374f89a379ee96c37",
      "01b4fa1208946a6d2d77cca3399655cedd504bae",
      "4687be081943f0d60e38662ebcdb2b038b5d539c",
      "8c87a9aaad3994d68b6b5bde61be3a6bc62eefeb",
      "40ef9b4ba9bc8e3ecff3f955598f5f6a1ddd3876",
      "582022549183c062db3ff25e89796e39e8b5ec7d",
      "2179e7304f57e8469ad028fd2c449a4e8fd7c733",
      "2c0161dc23857a30cec394b3843fedd47d3ce06d",
      "54a7fb87d95c401cf228232bae8eac78f78875cc",
      "54a7fb87d95c401cf228232bae8eac78f78875cc"
    )
      // .split('\n')
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
    os.pwd / "docs" / "changelog" / s"$lastTag.md"
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
      |We're happy to announce the release of Scala Native $version, which
      |
      |
      |Scala standard library used by this release is based on the following versions:
      |<table>
      |<tbody>
      |  <tr>
      |    <td>Scala binary version</td>
      |    <td>Scala release</td>
      |  </tr>
      |  <tr>
      |    <td align="center">2.12</td>
      |    <td align="center"></td>
      |  </tr>
      |  <tr>
      |    <td align="center">2.13</td>
      |    <td align="center"></td>
      |  </tr>
      |  <tr>
      |    <td align="center">3</td>
      |    <td align="center"></td>
      |  </tr>
      |</tbody>
      |</table>
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
