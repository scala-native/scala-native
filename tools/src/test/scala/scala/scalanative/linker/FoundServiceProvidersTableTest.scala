package scala.scalanative
package linker

import scala.io.AnsiColor

import org.junit.Assert._
import org.junit.Test

class FoundServiceProvidersTableTest extends LinkerSpec {
  @Test def correctFormatting(): Unit = {
    val actual = new LinktimeIntrinsicCallsResolver.FoundServiceProviders(
      Map(
        "Service 1 very long name" -> Seq(
          LinktimeIntrinsicCallsResolver.FoundServiceProvider(
            "Service 1 very long implementation name",
            LinktimeIntrinsicCallsResolver.ServiceProviderStatus.Loaded
          )
        ),
        "Service 2" -> Seq(
          LinktimeIntrinsicCallsResolver.FoundServiceProvider(
            "---",
            LinktimeIntrinsicCallsResolver.ServiceProviderStatus.NoProviders
          )
        )
      )
    )
      .asTable(noColor = false)

    val expected = Seq(
      "|----------------------------------------------------------------------------------|",
      "| Service                  | Service Provider                        | Status      |",
      "|----------------------------------------------------------------------------------|",
      s"| Service 1 very long name | Service 1 very long implementation name | ${AnsiColor.GREEN}Loaded     ${AnsiColor.RESET} |",
      "|                          |                                         |             |",
      s"| Service 2                | ---                                     | ${AnsiColor.YELLOW}NoProviders${AnsiColor.RESET} |",
      "|----------------------------------------------------------------------------------|"
    )

    expected.zip(actual).foreach {
      case (expectedLine, actualLine) =>
        assertEquals(expectedLine, actualLine)
    }
  }
}
