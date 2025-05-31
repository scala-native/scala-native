# Build Environment Setup

## Requirements

-   Java 8 or newer
-   LLVM/Clang 15 or newer
-   sbt

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


