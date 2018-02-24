package scala.scalanative.native

import scala.scalanative.native.stdio.{snprintf}
import scala.scalanative.native.stdlib.{getenv, putenv, setenv, unsetenv}
import scala.scalanative.native.stdlib.{free, malloc}
import scala.scalanative.native.string.{strcmp, strlen, strncpy}

object StdlibSuite extends tests.Suite {

  // Attempts at unique/reserved names. Suite will break if these
  // are in use by somebody else. Uniqueness is too hard a problem for now.
  //
  // When changing values for these variables, make sure that the new
  // values will still fit into putEnvBufLen!!

  val putEnvBufLen = 128

  // BE _EXQUSITELY_ careful with the lifetime of this variable!
  var putEnvBuf: Ptr[CChar] = malloc(putEnvBufLen)
  assertNotNull(putEnvBuf)

  val putEnvVarName    = c"SCALANATIVE_UNIT_TEST_STDLIB_PUTENV"
  val putEnvVarValue_1 = c"putEnvVarValue_1_value"
  val putEnvVarValue_2 = c"putEnvVarValue_2_value"
  val putEnvVarValue_3 = c"Ain't nobody here but us chickens" // L. Armstrong

  val setEnvVarName    = c"SCALANATIVE_UNIT_TEST_STDLIB_SETENV"
  val setEnvVarValue_1 = c"setEnvVarValue_1_value"
  val setEnvVarValue_2 = c"setEnvVarValue_2_value"

  val unsetEnvVarName = c"SCALANATIVE_UNIT_TEST_STDLIB_UNSETENV"

  /* This Suite tests functions/methods which are __defined_ as
   * having no requirement to be thread-safe. In addition, it changes
   * the value of environment variables. Other simultaneous threads may
   * pickup value bogus to them.
   *
   * Tests must be run sequentially, not in parallel, because there is
   * an explicit order to some of the tests. For example, one must
   * set an environment variable before unsetting it.
   *
   * So this Suite __must__ be run in an environment which is guaranteed
   * to be single-threaded for the duration of the Suite. There is no
   * good runtime for single-threadedness here. The guarantee must come
   * from the enveloping test framework.
   */

  // getenv section

  test("getenv name '=' which can never already be in environment") {

    val result = getenv(c"=")
    assertNull(result)
  }

  test("getenv name PATH already in environment") {
    // Every linux environment should have PATH.
    // There is __bound__ to be some OS which does not.
    // Cross-platform life is hard.

    val result = getenv(c"PATH")
    assertNotNull(result)
  }

  // putenv section

  test("putenv name not already in environment") {

    val result_1 = getenv(putEnvVarName)
    assertNull(result_1)

    val result_2 = snprintf(putEnvBuf,
                            putEnvBufLen,
                            c"%s=%s",
                            putEnvVarName,
                            putEnvVarValue_1)
    assert(result_2 > 0)

    val result_3 = putenv(putEnvBuf)
    assert(result_3 == 0)

    val result_4 = getenv(putEnvVarName)
    assertNotNull(result_4)

    val result_5 = strcmp(putEnvVarValue_1, result_4)
    assert(result_5 == 0)
  }

  test("putenv name already in environment, value changes") {

    val result_1 = getenv(putEnvVarName)
    assertNotNull(result_1)

    val result_2 =
      snprintf(putEnvBuf,
               putEnvBufLen,
               c"%s=%s",
               putEnvVarName,
               putEnvVarValue_2)
    assert(result_2 > 0)

    val result_3 = putenv(putEnvBuf)
    assert(result_3 == 0)

    val result_4 = getenv(putEnvVarName)
    assertNotNull(result_4)

    val result_5 = strcmp(putEnvVarValue_2, result_4)
    assert(result_5 == 0)
  }

  test("putenv change value of string given in previous call") {

    // "Everything not forbidden is compulsory". Murray Gell-Mann
    // People use putenv() in preference to setenv() to avoid potential
    // memory leak in latter. From there, it is but a short step to
    // modifying the string. Hence, this test.

    val result_1 = getenv(putEnvVarName)
    assertNotNull(result_1)

    val insertPoint     = putEnvBuf + strlen(putEnvVarName) + 1 // skip '='
    val remainingLength = putEnvBufLen - (insertPoint - putEnvBuf)

    strncpy(insertPoint, putEnvVarValue_3, remainingLength)
    putEnvBuf(putEnvBufLen - 1) = 0.toByte

    val result_2 = putenv(putEnvBuf)
    assert(result_2 == 0)

    val result_3 = getenv(putEnvVarName)
    assertNotNull(result_3)

    val result_4 = strcmp(putEnvVarValue_3, result_3)
    assert(result_4 == 0)
  }

  // setenv section

  test("setenv name not already in environment, no overwrite") {

    val result_1 = getenv(setEnvVarName)
    assertNull(result_1)

    val result_2 = setenv(setEnvVarName, setEnvVarValue_1, 0)
    assert(result_2 == 0)

    val result_3 = getenv(setEnvVarName)
    assertNotNull(result_3)

    val result_4 = strcmp(setEnvVarValue_1, result_3)
    assert(result_4 == 0)
  }

  test("setenv name already in environment, no overwrite") {

    val result_1 = getenv(setEnvVarName)
    assertNotNull(result_1)

    val result_2 = setenv(setEnvVarName, setEnvVarValue_2, 0)
    assert(result_2 == 0)

    val result_3 = getenv(setEnvVarName)
    assertNotNull(result_3)

    val result_4 = strcmp(setEnvVarValue_1, result_3)
    assert(result_4 == 0)
  }

  test("setenv name already in environment, overwrite") {

    val result_1 = getenv(setEnvVarName)
    assertNotNull(result_1)

    val result_2 = setenv(setEnvVarName, setEnvVarValue_2, 1)
    assert(result_2 == 0)

    val result_3 = getenv(setEnvVarName)
    assertNotNull(result_3)

    val result_4 = strcmp(setEnvVarValue_2, result_3)
    assert(result_4 == 0)
  }

  // End of suite, clean up environment.
  // unsetenv section.

  test(s"unsetenv name not already in environment") {

    val envVar = unsetEnvVarName

    val result_1 = getenv(envVar)
    assertNull(result_1)

    val result_2 = unsetenv(envVar)
    assert(result_2 == 0)
  }

  test(s"unsetenv putenv name already in environment") {

    val envVar = putEnvVarName

    val result_1 = getenv(envVar)
    assertNotNull(result_1)

    val result_2 = unsetenv(envVar)
    assert(result_2 == 0)

    free(putEnvBuf)
  }

  test("unsetenv setenv name already in environment") {

    val envVar = setEnvVarName

    val result_1 = getenv(envVar)
    assertNotNull(result_1)

    val result_2 = unsetenv(envVar)
    assert(result_2 == 0)
  }
}
