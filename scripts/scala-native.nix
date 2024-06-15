{ pkgs ? import <nixpkgs> { } }:
pkgs.mkShell {
  name = "clang-env";
  hardeningDisable = [ "fortify" ];
  buildInputs = with pkgs; [ boehmgc clang libunwind openjdk sbt ];
}
