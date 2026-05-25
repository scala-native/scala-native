# Build Environment Setup

## Requirements

-   Java 8 or newer
-   LLVM/Clang 16 or newer
-   sbt

## Windows

When building the **scala-native** repository itself on Windows (not only
consuming the compiler as a library), configure Git and symlinks as follows.

### Git line endings

Disable automatic CRLF conversion so `scalalib` patches apply (required on
all Windows platforms, including CI):

```powershell
git config --global core.autocrlf false
```

### Symlinked patch files

Several `scalalib` patch files are symlinks in git. Enable
[Developer Mode](https://learn.microsoft.com/windows/apps/get-started/enable-your-device-for-development)
(or clone in an elevated shell with `git config --global core.symlinks true`)
so `git checkout` creates real symlinks instead of small text stubs.

If patches still fail to apply after checkout, run `git reset --hard HEAD`
once `core.autocrlf` is `false`.

### Windows on ARM64

End-user toolchain notes (JDK, LLVM, sbt, vcpkg) are in
[Environment setup — Windows on ARM64](../user/setup.md#windows-on-arm64-woa).

For a full local install on WoA, run `scripts/setup-windows.ps1` from an
elevated **ARM64-native** PowerShell (mirrors
[`.github/workflows/run-tests-windows-arm.yml`](https://github.com/scala-native/scala-native/blob/main/.github/workflows/run-tests-windows-arm.yml);
runtime tests only on `windows-11-arm` runners, without Coursier).

If linking fails with missing `libcpmt.lib` / `legacy_stdio_definitions.lib`:

1. Run `scripts\check-msvc-arm64.ps1` (no admin; see script `.EXAMPLE`).
2. If not READY, run `scripts\install-vs-arm64-msvc.ps1` (elevated; relaunches
   as ARM64-native PowerShell and uses `setup.exe`, not x64-emulated install).
3. MSVC ARM64 libs may live under `C:\Program\VC\Tools\MSVC\...\lib\arm64\`
   after an ARM64-native install, not only under Build Tools in
   `Program Files (x86)`.

If `vcpkg install … arm64-windows-static` fails to build zlib, run
`scripts\build-zlib-arm64.ps1` (see script `.EXAMPLE`), then reload the
environment with `env-windows.ps1` as below.

### Environment variables and PATH

After tools are installed, load variables for the **current shell** (no admin
required). If dot-sourcing fails with *running scripts is disabled*, allow
scripts for this session only:

```powershell
cd <scala-native>
Set-ExecutionPolicy -Scope Process Bypass
. .\scripts\env-windows.ps1
```

To persist for your user account:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\env-windows.ps1 -Persist User
```

Then open a **new** terminal so PATH is picked up.

This sets `JAVA_HOME`, `LLVM_BIN`, `SCALANATIVE_INCLUDE_DIRS`,
`SCALANATIVE_LIB_DIRS`, `VCPKG_*`, and prepends PATH with JDK `bin`, LLVM,
sbt, Git, and vcpkg.

## Use nix / devenv.sh

The `flake.nix` and `flake.lock` files provide a build environment using nix
flakes. Use `nix develop` to enter a shell with the developer tools available.

This uses [devenv.sh](https://devenv.sh) to implement the shell environment.
See [Using `devenv` with Nix Flakes](https://devenv.sh/guides/using-with-flakes/)
for details on `devenv` usage and limitation with nix flakes.

See `.envrc` in this directory for automatically enabling the shell using
[`direnv`](https://direnv.net/docs/hook.html).


### On Using nix flakes

#### `nixpkgs`

`nixpkgs` is the [most up-to-date distribution with the most
packages](https://nixos.org/blog/announcements/2025/nixos-2505/). In particular `nixpkgs` offers
LLVM and garbage collector library builds for multiple platforms. The relative ease to reliably acquire build
tooling makes `nixpkgs` an attractive option for developers.

There is an additional property of `nixpkgs`: Content addressed dependencies. The flake lock defines the
required content hash of `nixpkgs`. Which implies specific builds of LLVM/clang and other dependencies.
Another developer can reliably reproduce the same environment. This also adds some mitigation against supply
chain attacks.

#### nix flakes

nix flakes is a way to describe dependencies on `nixpkgs` and other repositories. In addition, a nix flake
describes what a repository provides. For example, a compiler repository can describe the exact
dependencies required by the compiler build but also export a build of the compiler as a usable nix package.

For this repository the nix flake uses [devenv.sh](https://devenv.sh) to as a library to build this nix flake.
This provides a [limited `devenv` environment](https://devenv.sh/guides/using-with-flakes/).

nix flakes has contentious points but, arguably, is uniquely capable. For scala-native, there is additional
work required to, for example, enable packages to depend on a nix scala-native compiler build. So this use of
nix flakes here is more aspirational than immediately useful. The primary value is providing a reasonably nice
developer experience for an automatic and correct developer environment.

