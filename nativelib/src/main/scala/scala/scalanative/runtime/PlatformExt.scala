package scala.scalanative.runtime

import java.util.Locale

object PlatformExt {
  private lazy val osArch =
    System.getProperty("os.arch").toLowerCase(Locale.ROOT)
  
  lazy val isArm64 = osArch == "aarch64"
}
