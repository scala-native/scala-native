package scala.util

import org.junit.Assert._
import org.junit.Test

class PropertiesTest {
  @Test def propertiesReleaseVersion(): Unit = {
    assertTrue(Properties.releaseVersion != null)
  }

  @Test def propertiesVersionNumberString(): Unit = {
    assertTrue(Properties.versionNumberString != null)
  }

  @Test def PropertiesCopyrightString(): Unit = {
    assertTrue(Properties.copyrightString != null)
  }
}
