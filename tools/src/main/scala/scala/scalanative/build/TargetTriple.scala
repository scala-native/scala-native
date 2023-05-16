// ported from LLVM 887d6ab dated 2023-04-16

//===--- Triple.cpp - Target triple helper class --------------------------===//
//
// Part of the LLVM Project, under the Apache License v2.0 with LLVM Exceptions.
// See https://llvm.org/LICENSE.txt for license information.
// SPDX-License-Identifier: Apache-2.0 WITH LLVM-exception
//
//===----------------------------------------------------------------------===//

package scala.scalanative
package build

import java.nio.ByteOrder

private[scalanative] final case class TargetTriple(
    arch: String,
    vendor: String,
    os: String,
    env: String
) {
  override def toString = s"$arch-$vendor-$os-$env"
}

private[scalanative] object TargetTriple {

  def parse(triple: String): TargetTriple = {
    val components = triple.split("-", 4).toList
    val unknown = "unknown"
    TargetTriple(
      components.lift(0).map(Arch.parse).getOrElse(unknown),
      components.lift(1).map(Vendor.parse).getOrElse(unknown),
      components.lift(2).map(OS.parse).getOrElse(unknown),
      components.lift(3).map(Env.parse).getOrElse(unknown)
    )
  }

  object Arch {
    def parse(str: String): String = str match {
      case "i386" | "i486" | "i586" | "i686"          => x86
      case "i786" | "i886" | "i986"                   => x86
      case "amd64" | "x86_64" | "x86_64h"             => x86_64
      case "powerpc" | "powerpcspe" | "ppc" | "ppc32" => ppc
      case "powerpcle" | "ppcle" | "ppc32le"          => ppcle
      case "powerpc64" | "ppu" | "ppc64"              => ppc64
      case "powerpc64le" | "ppc64le"                  => ppc64le
      case "xscale"                                   => arm
      case "xscaleeb"                                 => armeb
      case "aarch64"                                  => aarch64
      case "aarch64_be"                               => aarch64_be
      case "aarch64_32"                               => aarch64_32
      case "arc"                                      => arc
      case "arm64"                                    => aarch64
      case "arm64_32"                                 => aarch64_32
      case "arm64e"                                   => aarch64
      case "arm64ec"                                  => aarch64
      case "arm"                                      => arm
      case "armeb"                                    => armeb
      case "thumb"                                    => thumb
      case "thumbeb"                                  => thumbeb
      case "avr"                                      => avr
      case "m68k"                                     => m68k
      case "msp430"                                   => msp430
      case "mips" | "mipseb" | "mipsallegrex" | "mipsisa32r6" | "mipsr6" => mips
      case "mipsel" | "mipsallegrexel" | "mipsisa32r6el" | "mipsr6el" =>
        mipsel
      case "mips64" | "mips64eb" | "mipsn32" | "mipsisa64r6" | "mips64r6" |
          "mipsn32r6" =>
        mips64
      case "mips64el" | "mipsn32el" | "mipsisa64r6el" | "mips64r6el" |
          "mipsn32r6el" =>
        mips64el
      case "r600"                => r600
      case "amdgcn"              => amdgcn
      case "riscv32"             => riscv32
      case "riscv64"             => riscv64
      case "hexagon"             => hexagon
      case "s390x" | "systemz"   => systemz
      case "sparc"               => sparc
      case "sparcel"             => sparcel
      case "sparcv9" | "sparc64" => sparcv9
      case "tce"                 => tce
      case "tcele"               => tcele
      case "xcore"               => xcore
      case "nvptx"               => nvptx
      case "nvptx64"             => nvptx64
      case "le32"                => le32
      case "le64"                => le64
      case "amdil"               => amdil
      case "amdil64"             => amdil64
      case "hsail"               => hsail
      case "hsail64"             => hsail64
      case "spir"                => spir
      case "spir64"              => spir64
      case "spirv32" | "spirv32v1.0" | "spirv32v1.1" | "spirv32v1.2" |
          "spirv32v1.3" | "spirv32v1.4" | "spirv32v1.5" =>
        spirv32
      case "spirv64" | "spirv64v1.0" | "spirv64v1.1" | "spirv64v1.2" |
          "spirv64v1.3" | "spirv64v1.4" | "spirv64v1.5" =>
        spirv64
      case "lanai"          => lanai
      case "renderscript32" => renderscript32
      case "renderscript64" => renderscript64
      case "shave"          => shave
      case "ve"             => ve
      case "wasm32"         => wasm32
      case "wasm64"         => wasm64
      case "csky"           => csky
      case "loongarch32"    => loongarch32
      case "loongarch64"    => loongarch64
      case "dxil"           => dxil
      case "xtensa"         => xtensa
      case other            =>
        // Some architectures require special parsing logic just to compute the
        // ArchType result.

        if (other.startsWith("kalimba"))
          kalimba
        else if (other.startsWith("arm") || other.startsWith("thumb") ||
            other.startsWith("aarch64"))
          parseArm(other)
        else if (other.startsWith("bpf"))
          parseBpf(other)
        else
          unknown
    }

    private def parseArm(str: String): String = {

      val isa =
        if (str.startsWith("aarch64") || str.startsWith("arm64")) aarch64
        else if (str.startsWith("thumb")) thumb
        else if (str.startsWith("arm")) arm
        else unknown

      val endian =
        if (str.startsWith("armeb") || str.startsWith("thumbeb") ||
            str.startsWith("aarch64_be"))
          Some(ByteOrder.BIG_ENDIAN)
        else if (str.startsWith("arm") || str.startsWith("thumb")) {
          if (str.endsWith("eb"))
            Some(ByteOrder.BIG_ENDIAN)
          else
            Some(ByteOrder.LITTLE_ENDIAN)
        } else if (str.startsWith("aarch64") || str.startsWith("aarch64_32"))
          Some(ByteOrder.LITTLE_ENDIAN)
        else None

      endian match {
        case Some(ByteOrder.LITTLE_ENDIAN) =>
          isa match {
            case `arm`     => arm
            case `thumb`   => thumb
            case `aarch64` => aarch64
            case _         => unknown
          }
        case Some(ByteOrder.BIG_ENDIAN) =>
          isa match {
            case `arm`     => armeb
            case `thumb`   => thumbeb
            case `aarch64` => aarch64_be
            case _         => unknown
          }
        case _ => unknown
      }
    }

    private def parseBpf(str: String): String = str match {
      case "bpf" =>
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN)
          bpfel
        else bpfeb
      case "bpf_be" | "bpfeb" => bpfeb
      case "bpf_le" | "bpfel" => bpfel
      case _                  => unknown
    }

    final val unknown = "unknown"
    final val aarch64 = "aarch64"
    final val aarch64_32 = "aarch64_32"
    final val aarch64_be = "aarch64_be"
    final val amdgcn = "amdgcn"
    final val amdil64 = "amdil64"
    final val amdil = "amdil"
    final val arc = "arc"
    final val arm = "arm"
    final val armeb = "armeb"
    final val avr = "avr"
    final val bpfeb = "bpfeb"
    final val bpfel = "bpfel"
    final val csky = "csky"
    final val dxil = "dxil"
    final val hexagon = "hexagon"
    final val hsail64 = "hsail64"
    final val hsail = "hsail"
    final val kalimba = "kalimba"
    final val lanai = "lanai"
    final val le32 = "le32"
    final val le64 = "le64"
    final val loongarch32 = "loongarch32"
    final val loongarch64 = "loongarch64"
    final val m68k = "m68k"
    final val mips64 = "mips64"
    final val mips64el = "mips64el"
    final val mips = "mips"
    final val mipsel = "mipsel"
    final val msp430 = "msp430"
    final val nvptx64 = "nvptx64"
    final val nvptx = "nvptx"
    final val ppc64 = "powerpc64"
    final val ppc64le = "powerpc64le"
    final val ppc = "powerpc"
    final val ppcle = "powerpcle"
    final val r600 = "r600"
    final val renderscript32 = "renderscript32"
    final val renderscript64 = "renderscript64"
    final val riscv32 = "riscv32"
    final val riscv64 = "riscv64"
    final val shave = "shave"
    final val sparc = "sparc"
    final val sparcel = "sparcel"
    final val sparcv9 = "sparcv9"
    final val spir64 = "spir64"
    final val spir = "spir"
    final val spirv32 = "spirv32"
    final val spirv64 = "spirv64"
    final val systemz = "s390x"
    final val tce = "tce"
    final val tcele = "tcele"
    final val thumb = "thumb"
    final val thumbeb = "thumbeb"
    final val ve = "ve"
    final val wasm32 = "wasm32"
    final val wasm64 = "wasm64"
    final val x86 = "i386"
    final val x86_64 = "x86_64"
    final val xcore = "xcore"
    final val xtensa = "xtensa"
  }

  object Vendor {
    def parse(str: String): String = str match {
      case "apple"  => Apple
      case "pc"     => PC
      case "scei"   => SCEI
      case "sie"    => SCEI
      case "fsl"    => Freescale
      case "ibm"    => IBM
      case "img"    => ImaginationTechnologies
      case "mti"    => MipsTechnologies
      case "nvidia" => NVIDIA
      case "csr"    => CSR
      case "myriad" => Myriad
      case "amd"    => AMD
      case "mesa"   => Mesa
      case "suse"   => SUSE
      case "oe"     => OpenEmbedded
      case _        => Unknown
    }

    final val Unknown = "unknown"
    final val AMD = "amd"
    final val Apple = "apple"
    final val CSR = "csr"
    final val Freescale = "fsl"
    final val IBM = "ibm"
    final val ImaginationTechnologies = "img"
    final val Mesa = "mesa"
    final val MipsTechnologies = "mti"
    final val Myriad = "myriad"
    final val NVIDIA = "nvidia"
    final val OpenEmbedded = "oe"
    final val PC = "pc"
    final val SCEI = "scei"
    final val SUSE = "suse"
  }

  object OS {
    def parse(str: String): String = str match {
      case os if os.startsWith("ananas")      => Ananas
      case os if os.startsWith("cloudabi")    => CloudABI
      case os if os.startsWith("darwin")      => Darwin
      case os if os.startsWith("dragonfly")   => DragonFly
      case os if os.startsWith("freebsd")     => FreeBSD
      case os if os.startsWith("fuchsia")     => Fuchsia
      case os if os.startsWith("ios")         => IOS
      case os if os.startsWith("kfreebsd")    => KFreeBSD
      case os if os.startsWith("linux")       => Linux
      case os if os.startsWith("lv2")         => Lv2
      case os if os.startsWith("macos")       => MacOSX
      case os if os.startsWith("netbsd")      => NetBSD
      case os if os.startsWith("openbsd")     => OpenBSD
      case os if os.startsWith("solaris")     => Solaris
      case os if os.startsWith("win32")       => Win32
      case os if os.startsWith("windows")     => Win32
      case os if os.startsWith("zos")         => ZOS
      case os if os.startsWith("haiku")       => Haiku
      case os if os.startsWith("minix")       => Minix
      case os if os.startsWith("rtems")       => RTEMS
      case os if os.startsWith("nacl")        => NaCl
      case os if os.startsWith("aix")         => AIX
      case os if os.startsWith("cuda")        => CUDA
      case os if os.startsWith("nvcl")        => NVCL
      case os if os.startsWith("amdhsa")      => AMDHSA
      case os if os.startsWith("ps4")         => PS4
      case os if os.startsWith("ps5")         => PS5
      case os if os.startsWith("elfiamcu")    => ELFIAMCU
      case os if os.startsWith("tvos")        => TvOS
      case os if os.startsWith("watchos")     => WatchOS
      case os if os.startsWith("driverkit")   => DriverKit
      case os if os.startsWith("mesa3d")      => Mesa3D
      case os if os.startsWith("contiki")     => Contiki
      case os if os.startsWith("amdpal")      => AMDPAL
      case os if os.startsWith("hermit")      => HermitCore
      case os if os.startsWith("hurd")        => Hurd
      case os if os.startsWith("wasi")        => WASI
      case os if os.startsWith("emscripten")  => Emscripten
      case os if os.startsWith("shadermodel") => ShaderModel
      case os if os.startsWith("liteos")      => LiteOS
      case _                                  => Unknown
    }

    final val Unknown = "unknown"
    final val AIX = "aix"
    final val AMDHSA = "amdhsa"
    final val AMDPAL = "amdpal"
    final val Ananas = "ananas"
    final val CUDA = "cuda"
    final val CloudABI = "cloudabi"
    final val Contiki = "contiki"
    final val Darwin = "darwin"
    final val DragonFly = "dragonfly"
    final val DriverKit = "driverkit"
    final val ELFIAMCU = "elfiamcu"
    final val Emscripten = "emscripten"
    final val FreeBSD = "freebsd"
    final val Fuchsia = "fuchsia"
    final val Haiku = "haiku"
    final val HermitCore = "hermit"
    final val Hurd = "hurd"
    final val IOS = "ios"
    final val KFreeBSD = "kfreebsd"
    final val Linux = "linux"
    final val Lv2 = "lv2"
    final val MacOSX = "macosx"
    final val Mesa3D = "mesa3d"
    final val Minix = "minix"
    final val NVCL = "nvcl"
    final val NaCl = "nacl"
    final val NetBSD = "netbsd"
    final val OpenBSD = "openbsd"
    final val PS4 = "ps4"
    final val PS5 = "ps5"
    final val RTEMS = "rtems"
    final val Solaris = "solaris"
    final val TvOS = "tvos"
    final val WASI = "wasi"
    final val WatchOS = "watchos"
    final val Win32 = "windows"
    final val ZOS = "zos"
    final val ShaderModel = "shadermodel"
    final val LiteOS = "liteos"
  }

  object Env {
    def parse(str: String): String = str match {
      case env if env.startsWith("eabihf")        => EABIHF
      case env if env.startsWith("eabi")          => EABI
      case env if env.startsWith("gnuabin32")     => GNUABIN32
      case env if env.startsWith("gnuabi64")      => GNUABI64
      case env if env.startsWith("gnueabihf")     => GNUEABIHF
      case env if env.startsWith("gnueabi")       => GNUEABI
      case env if env.startsWith("gnuf32")        => GNUF32
      case env if env.startsWith("gnuf64")        => GNUF64
      case env if env.startsWith("gnusf")         => GNUSF
      case env if env.startsWith("gnux32")        => GNUX32
      case env if env.startsWith("gnu_ilp32")     => GNUILP32
      case env if env.startsWith("code16")        => CODE16
      case env if env.startsWith("gnu")           => GNU
      case env if env.startsWith("android")       => Android
      case env if env.startsWith("musleabihf")    => MuslEABIHF
      case env if env.startsWith("musleabi")      => MuslEABI
      case env if env.startsWith("muslx32")       => MuslX32
      case env if env.startsWith("musl")          => Musl
      case env if env.startsWith("msvc")          => MSVC
      case env if env.startsWith("itanium")       => Itanium
      case env if env.startsWith("cygnus")        => Cygnus
      case env if env.startsWith("coreclr")       => CoreCLR
      case env if env.startsWith("simulator")     => Simulator
      case env if env.startsWith("macabi")        => MacABI
      case env if env.startsWith("pixel")         => Pixel
      case env if env.startsWith("vertex")        => Vertex
      case env if env.startsWith("geometry")      => Geometry
      case env if env.startsWith("hull")          => Hull
      case env if env.startsWith("domain")        => Domain
      case env if env.startsWith("compute")       => Compute
      case env if env.startsWith("library")       => Library
      case env if env.startsWith("raygeneration") => RayGeneration
      case env if env.startsWith("intersection")  => Intersection
      case env if env.startsWith("anyhit")        => AnyHit
      case env if env.startsWith("closesthit")    => ClosestHit
      case env if env.startsWith("miss")          => Miss
      case env if env.startsWith("callable")      => Callable
      case env if env.startsWith("mesh")          => Mesh
      case env if env.startsWith("amplification") => Amplification
      case env if env.startsWith("ohos")          => OpenHOS
      case _                                      => Unknown
    }

    final val Unknown = "unknown"
    final val Android = "android"
    final val CODE16 = "code16"
    final val CoreCLR = "coreclr"
    final val Cygnus = "cygnus"
    final val EABI = "eabi"
    final val EABIHF = "eabihf"
    final val GNU = "gnu"
    final val GNUABI64 = "gnuabi64"
    final val GNUABIN32 = "gnuabin32"
    final val GNUEABI = "gnueabi"
    final val GNUEABIHF = "gnueabihf"
    final val GNUF32 = "gnuf32"
    final val GNUF64 = "gnuf64"
    final val GNUSF = "gnusf"
    final val GNUX32 = "gnux32"
    final val GNUILP32 = "gnu_ilp32"
    final val Itanium = "itanium"
    final val MSVC = "msvc"
    final val MacABI = "macabi"
    final val Musl = "musl"
    final val MuslEABI = "musleabi"
    final val MuslEABIHF = "musleabihf"
    final val MuslX32 = "muslx32"
    final val Simulator = "simulator"
    final val Pixel = "pixel"
    final val Vertex = "vertex"
    final val Geometry = "geometry"
    final val Hull = "hull"
    final val Domain = "domain"
    final val Compute = "compute"
    final val Library = "library"
    final val RayGeneration = "raygeneration"
    final val Intersection = "intersection"
    final val AnyHit = "anyhit"
    final val ClosestHit = "closesthit"
    final val Miss = "miss"
    final val Callable = "callable"
    final val Mesh = "mesh"
    final val Amplification = "amplification"
    final val OpenHOS = "ohos"
  }

}
