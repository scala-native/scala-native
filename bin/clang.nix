let
  pkgs = import <nixpkgs> {};
  stdenv = pkgs.stdenv;
in rec {
  clangEnv = stdenv.mkDerivation rec {
    name = "clang-env";
    buildInputs = [
      stdenv
      pkgs.boehmgc
    ];
  };
} 