let
  pkgs = import <nixpkgs> {};
in rec {
  clangEnv = stdenv.mkDerivation rec {
    name = "clang-env";
    buildInputs = with pkgs; [
      stdenv
      boehmgc
    ];
  };
}