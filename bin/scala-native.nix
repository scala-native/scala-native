let
  pkgs = import <nixpkgs> {};
  stdenv = pkgs.stdenv;
  sbtOracle = pkgs.sbt.override {
    jre = pkgs.oraclejdk8.jre;
  };
in rec {
  clangEnv = stdenv.mkDerivation rec {
    name = "clang-env";
    shellHook = ''
    alias cls=clear
    '';
    CLANG_PATH = pkgs.clang + "/bin/clang";
    CLANGPP_PATH = pkgs.clang + "/bin/clang++";
    buildInputs = with pkgs; [
      stdenv
      sbtOracle
      boehmgc
      clang
    ];
  };
} 
