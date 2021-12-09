package scala.scalanative.runtime

object PlatformExt {
  private lazy val osArch =
    System.getProperty("os.arch").toLowerCase()
  
  lazy val isArm64 = osArch == "aarch64"
}
