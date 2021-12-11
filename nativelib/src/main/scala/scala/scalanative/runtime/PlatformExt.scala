package scala.scalanative.runtime

object PlatformExt {
  private lazy val osArch =
    System.getProperty("os.arch").toLowerCase()

  // On M1 macs it's arm64, on windows we set it to aarch64
  // see ./nativelib/src/main/resources/scala-native/platform.c for details
  lazy val isArm64 = osArch == "arm64" || osArch == "aarch64"
}
