package scala.scalanative.build

import java.nio.file.Paths

import org.junit.Assert._
import org.junit.Test

class ConfigTest {

  @Test def relativeBaseDirIsMadeAbsolute(): Unit = {
    val config = Config.empty.withBaseDir(Paths.get("native"))
    val expected = Paths.get("native").toAbsolutePath()
    assertEquals(expected, config.baseDir)
    assertTrue(config.workDir.isAbsolute())
    assertEquals(expected.resolve("native"), config.workDir)
  }

  @Test def defaultBaseDirIsAbsolute(): Unit = {
    assertTrue(Config.empty.baseDir.isAbsolute())
    assertTrue(Config.empty.workDir.isAbsolute())
  }
}
