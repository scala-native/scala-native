package java.nio.file.attribute

import java.nio.file.FileSystems
import scala.util.Random

object UserPrincipalLookupServiceSuite extends tests.Suite {
  val isTravis = System.getenv("TRAVIS") == "true"

  val lookupService = FileSystems.getDefault.getUserPrincipalLookupService

  test("lookupPrincipalByName succeeds for `root` user") {
    assert(lookupService.lookupPrincipalByName("root").getName == "root")
  }

  test("lookupPrincipalByName throws exception for `gobbledygook` user") {
    if (isTravis) {
      assertThrows[UserPrincipalNotFoundException](
        lookupService.lookupPrincipalByName("gobbledygook"))
    }
  }

  test("lookupPrincipalByGroupName succeeds for `root` group") {
    assert(lookupService.lookupPrincipalByGroupName("root").getName == "root")
  }

  test("lookupPrincipalByGroupName throws exception for `gobbledygook` group") {
    if (isTravis) {
      assertThrows[UserPrincipalNotFoundException](
        lookupService.lookupPrincipalByGroupName("gobbledygook"))
    }
  }

  test("lookupPrincipalByName succeeds for numeric name") {
    val expected = Random.nextInt.toString

    assert(lookupService.lookupPrincipalByName(expected).getName == expected)
  }

  test("lookupPrincipalByGroupName succeeds for numeric name") {
    val expected = Random.nextInt.toString

    assert(
      lookupService.lookupPrincipalByGroupName(expected).getName == expected)
  }
}
