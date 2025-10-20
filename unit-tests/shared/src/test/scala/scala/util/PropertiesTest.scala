package scala.util

import org.junit.Test
import org.junit.Assert.*

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
