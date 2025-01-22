{
  description = "scala-native";

  inputs.nixpkgs.url = "github:NixOS/nixpkgs/24.05";

  inputs.sbt.url = "github:zaninime/sbt-derivation/master";
  inputs.sbt.inputs.nixpkgs.follows = "nixpkgs";

  inputs.systems.url = "github:nix-systems/default";

  outputs = {
    self,
    nixpkgs,
    sbt,
    systems
  }:
    let
      eachSystem = nixpkgs.lib.genAttrs (import systems);
    in
    {
      devShells = eachSystem (system:
        let pkgs = nixpkgs.legacyPackages.${system}; in
        {
          default = (sbt.mkSbtDerivation.${system}).withOverrides({ stdenv = pkgs.llvmPackages_18.stdenv; }) {
            pname = "scala-native";
            version = (builtins.elemAt
              (
                builtins.match "^.*current: String = \"([^\"]+)\".*$" (
                  builtins.readFile "${self}/nir/src/main/scala/scala/scalanative/nir/Versions.scala"
                )
              )
              0
            );
            src = self;
            # set to "" and rebuild to regenerate this.
            depsSha256 = "sha256-xLudUMf0bi77vWBnLZvft2sALD9GSI7ezpxXObxL/rs=";
            nativeBuildInputs = [
              pkgs.async-profiler
              pkgs.linuxPackages.perf
            ];
            buildInputs = with pkgs; [
              boehmgc
              libunwind
              zlib
            ];
            env.NIX_CFLAGS_COMPILE = "-Wno-unused-command-line-argument";
            hardeningDisable = [ "fortify" ];
          };
        }
      );
    };
}
