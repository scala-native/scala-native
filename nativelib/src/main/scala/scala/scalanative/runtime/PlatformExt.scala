package scala.scalanative.runtime

object PlatformExt {
  private lazy val osArch =
    System.getProperty("os.arch").toLowerCase()

  /* BE __EXTREMELY__ CAREFUL changing the next declaration.
   *
   * The declaration of 'isArm64', as implemented, is a misnomer.
   * It does not accurately report the lowest level 'bare metal' hardware.
   * That is, it reports "false" when the hardware is arm64 but the
   * program is running under (Rosetta 2) translation.
   *
   * Normally one would correct the definition. However, there are
   * significant parts of Scala Native which require the current definition
   * (and fail with an inclusive & accurate one). CVarArgsList is one of
   * these.
   *
   * Translated programs have os.arch == "X86_64" but may still be
   * sensitive to hardware arm64 issues.
   */

  // On M1 macs it's arm64, on windows we set it to aarch64
  // see ./nativelib/src/main/resources/scala-native/platform.c for details

  lazy val isArm64 = osArch == "arm64" || osArch == "aarch64"
}
