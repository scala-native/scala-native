let
  pkgs = import <nixpkgs> {};
  stdenv = pkgs.stdenv;
in rec {
  clangEnv = stdenv.mkDerivation rec {
    name = "clang-env";
    shellHook = ''
    alias cls=clear
    '';
    LLVM_BIN = pkgs.clang + "/bin";
    buildInputs = with pkgs; [
      stdenv
      sbt
      openjdk
      boehmgc
      libunwind
      clang
      zlib
    ];
  };
} 
