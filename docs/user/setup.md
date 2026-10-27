# Environment setup

Following documentation uses [sbt](https://www.scala-sbt.org/) build tool.
Other build tools that support Scala Native are:
- [scala-cli](https://scala-cli.virtuslab.org/install/#scala-native)
- [Mill](https://mill-build.org/mill/scalalib/native-examples.html)
- [Gradle](https://gradle.org) with [Scala Native / Scala.js plugin](https://github.com/dubinsky/scalajs-gradle)


Scala Native has the following build dependencies:

-   Java 8 or newer
-   sbt 1.5.8 or newer
-   LLVM/Clang 6.0 or newer (LLVM/Clang 16 or newer is recommended; older versions are deprecated)

And following completely optional runtime library dependencies:

-   Boehm GC 7.6.0 (optional)
-   zlib 1.2.8 or newer (optional)

These are only required if you use the corresponding feature.

## Installing sbt

**macOS, Linux, and Windows**

Please refer to [this
link](https://www.scala-sbt.org/release/docs/Setup.html) for
instructions for your operating system.

**FreeBSD**

``` shell
pkg install sbt
```

**OpenBSD**

``` shell
pkg_add sbt
```

**NetBSD**

``` shell
pkg_add scala-sbt
```

## Installing clang and runtime dependencies

Scala Native requires Clang, which is part of the
[LLVM](https://llvm.org) toolchain. LLVM/Clang 16 is the default version
used in CI and the recommended baseline for local development. Older
versions remain supported down to the minimum version shown above, but
they are deprecated and may cause issues with continuations.

Scala Native uses the
[Immix](https://www.cs.utexas.edu/users/speedway/DaCapo/papers/immix-pldi-2008.pdf)
garbage collector by default. You can use the
[Boehm](https://github.com/ivmai/bdwgc) garbage collector instead. If
you chose to use that alternate garbage collector both the native
library and header files must be provided at build time.

If you use classes from the `java.util.zip` for compression
zlib needs to be installed.

*Note:* Some package managers provide the library header files in separate
`-dev` packages.

Here are install instructions for a number of operating systems Scala
Native has been used with:

**macOS**

``` shell
brew install llvm
brew install bdw-gc # optional
```

*Note 1:* Xcode should work as an alternative if preferred:
<https://apps.apple.com/us/app/xcode/id497799835>

*Note 2:* A version of zlib that is sufficiently recent comes with the
installation of macOS.

**Ubuntu**

``` shell
sudo apt install clang libstdc++-12-dev
sudo apt install libgc-dev # optional
```

**Arch Linux**

``` shell
sudo pacman -S llvm clang
sudo pacman -S gc # optional
```

*Note:* A version of zlib that is sufficiently recent comes with the
installation of Arch Linux.

**Fedora 33**

``` shell
sudo dnf install llvm clang
sudo dnf groupinstall "Development Tools"
sudo dnf install gc-devel zlib-devel # both optional
```

**FreeBSD 12.4 and later**

*Note 1:* Only AMD64 and ARM64 architectures are supported.

*Note 2:* Sufficiently recent versions of llvm and zlib come with the
installation of FreeBSD.

``` shell
pkg install boehm-gc # optional
```

*Note 3:* Using the boehm GC with multi-threaded binaries doesn't work
out-of-the-box yet.

**OpenBSD 7.5 and later**

*Note 1:* OpenBSD support is experimental and limited to only AMD64
architecture.

``` shell
pkg_add boehm-gc # optional
```

**NetBSD 9.3 and later**

*Note 1:* NetBSD support is experimental and limited to only AMD64
architecture.

``` shell
pkg_add clang
pkg_add boehm-gc # optional
```

**Nix/NixOS**

``` shell
wget https://raw.githubusercontent.com/scala-native/scala-native/main/scripts/scala-native.nix
nix-shell scala-native.nix -A clangEnv
```

**Termux (Android)**

*Note 1:* Supported on ARM64 devices running Android 9 (API 28) or later.

Install the required packages in Termux:

``` shell
pkg install clang openjdk-21 which zlib libandroid-posix-semaphore libandroid-wordexp
```

Add these settings to your `build.sbt`:

``` scala
nativeConfig ~= { config =>
  config
    .withTargetTriple("aarch64-unknown-linux-android28")
    .withLinkingOptions(_ ++ Seq("-landroid-posix-semaphore", "-landroid-wordexp"))
}
```

*Note 2:* Android's Bionic C library does not provide named POSIX semaphores or word expansion. `libandroid-posix-semaphore` and `libandroid-wordexp` provide these features.

*Note 3:* Bionic does not support POSIX thread cancellation or monetary formatting.

### Windows (x64)

Corporate environments and Windows policies can affect the method used
to set up your environment. The following procedure involves downloading
installers and running the installers using Powershell (Administrative)
to avoid some of these issues. If you have full access to your machine
then you can install using your favorite method.
[Chocolatey](https://chocolatey.org/) or [Scoop](https://scoop.sh/) can
be substituted as needed or desired and are also mentioned above in the
installing [sbt](./sbt.md) documentation.

1.  Download and install Visual Studio Community 2019

<https://visualstudio.microsoft.com/>

You may install it via the command line if needed.

``` powershell
> .\vs_community__<version>.exe
```

Select the *Workloads* tab and then *Desktop development with C++*
checkbox. The defaults are fine. The *C++ Clang tools for Windows* does
not work so use the next step for details on installing LLVM.

```{figure} vs-install.png
:alt: visual studio install

Visual Studio install dialog showing options.
```

2.  Download and install LLVM

<https://github.com/llvm/llvm-project/releases>

Select an *LLVM-16.x-win64.exe* installer or newer. Digital signatures are
provided.

You may also install LLVM via the command line, and if needed, install
it into your `C:\Users\<login>\AppData\Local` directory. The
installer will add *LLVM* and the associated directories and files.

``` powershell
> .\LLVM-<version>-win64.exe
```

3.  Add the binary location to your PATH

Using the install path above, you would add the following:

``` powershell
C:\Users\<login>\AppData\Local\LLVM\bin
```

### Windows on ARM64 (WoA)

Use native ARM64 versions of the JDK, LLVM and native dependencies:

1. Install an ARM64 JDK, such as
   [Microsoft Build of OpenJDK](https://learn.microsoft.com/java/openjdk/).
2. Install the Windows ARM64 LLVM package (`LLVM-<version>-woa64.exe`) from the
   [LLVM releases](https://github.com/llvm/llvm-project/releases).
   Add its `bin` directory to `PATH`, or set `LLVM_BIN` to that directory.
   Verify that `clang -dumpmachine` reports `aarch64-pc-windows-msvc`.
3. Install [sbt using its Windows installer](https://www.scala-sbt.org/download.html).
4. Install Visual Studio 2022 Build Tools with the *Desktop development with C++*
   workload, **MSVC v143 C++ ARM64 build tools**, and a Windows 11 SDK.
   Run the installer from a native ARM64 PowerShell session.
5. Build native dependencies with the `arm64-windows-static` vcpkg triplet:

   ```powershell
   $env:VCPKG_DEFAULT_HOST_TRIPLET = "arm64-windows"
   vcpkg install bdwgc zlib --triplet=arm64-windows-static
   ```

Set `SCALANATIVE_INCLUDE_DIRS` and `SCALANATIVE_LIB_DIRS` to the `include` and
`lib` directories under `<vcpkg>/installed/arm64-windows-static`.
If vcpkg produces `zs.lib`, copy it to `zlib.lib` in the same directory so
Scala Native can find zlib.

For repository builds and setup helpers, see
[Windows on ARM64 contributor setup](../contrib/build-setup.md#windows-on-arm64).

Continue to [sbt](./sbt.md).
