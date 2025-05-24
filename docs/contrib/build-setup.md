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
