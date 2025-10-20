package scala.scalanative.build

import org.junit.Test
import org.junit.Assert.*

class TargetTripleTest {

  val cases = List(
    "aarch64-unknown-linux-gnu" ->
      TargetTriple("aarch64", "unknown", "linux", "gnu"),
    "arm64-apple-darwin22.4.0" ->
      TargetTriple("aarch64", "apple", "darwin", "unknown"),
    "x86_64-apple-darwin13.4.0" ->
      TargetTriple("x86_64", "apple", "darwin", "unknown"),
    "x86_64-apple-darwin20.6.0" ->
      TargetTriple("x86_64", "apple", "darwin", "unknown"),
    "x86_64-apple-darwin21.6.0" ->
      TargetTriple("x86_64", "apple", "darwin", "unknown"),
    "x86_64-apple-darwin22.4.0" ->
      TargetTriple("x86_64", "apple", "darwin", "unknown"),
    "x86_64-pc-linux-gnu" ->
      TargetTriple("x86_64", "pc", "linux", "gnu"),
    "x86_64-pc-windows-msvc" ->
      TargetTriple("x86_64", "pc", "windows", "msvc"),
    "i686-pc-windows-msvc" ->
      TargetTriple("i386", "pc", "windows", "msvc"),
    "x86_64-portbld-freebsd13.1" ->
      TargetTriple("x86_64", "unknown", "freebsd", "unknown"),
    "i386-linux-gnu" ->
      TargetTriple("i386", "unknown", "linux", "gnu")
  )

  // samples based on parsed to type
  val cases32Bit = List(
    "aarch64_32",
    "amdil",
    "arc",
    "arm",
    "armeb",
    "csky",
    "dxil",
    "hexagon",
    "hsail",
    "kalimba",
    "lanai",
    "le32",
    "loongarch32",
    "m68k",
    "mips",
    "mipsel",
    "nvptx",
    "ppc",
    "ppcle",
    "r600",
    "renderscript32",
    "riscv32",
    "shave",
    "sparc",
    "sparcel",
    "spir",
    "spirv32",
    "tce",
    "tcele",
    "thumb",
    "thumbeb",
    "wasm32",
    "i386", // parsed to x86
    "xcore",
    "xtensa"
  )

  // samples based on parsed to type
  val cases64Bit = List(
    "aarch64",
    "aarch64_be",
    "amdgcn",
    "amdil64",
    "bpfeb",
    "bpfel",
    "hsail64",
    "le64",
    "loongarch64",
    "mips64",
    "mips64el",
    "nvptx64",
    "ppc64",
    "ppc64le",
    "renderscript64",
    "riscv64",
    "sparcv9",
    "spir64",
    "spirv64",
    "systemz",
    "ve",
    "wasm64",
    "x86_64"
  )

  @Test
  def testParser(): Unit = cases.foreach {
    case (triple, expected) =>
      assertEquals(triple, expected, TargetTriple.parse(triple))
  }

  @Test
  def isArch32Bit(): Unit = cases32Bit.foreach {
    case arch =>
      assertEquals(arch, true, TargetTriple.isArch32Bit(arch))
  }

  @Test
  def isArch64Bit(): Unit = cases64Bit.foreach {
    case arch =>
      assertEquals(arch, true, TargetTriple.isArch64Bit(arch))
  }
}
