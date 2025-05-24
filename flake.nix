/* A devenv based developer shell. Choices:
 *
 * - sets the JDK to JDK 21
 * - sets the environment variables for:
 *    - no experimental compiler
 *    - debug mode
 *    - optimizer on
 *    - immix GC
 *    - no LTO
 *
 * This is a nix flake that describes how to set up a developer environment for
 * scala-native. This uses packages from `nixpkgs`. The specific versions of those
 * packages is defined by `flake.lock`.
 *
 * See also: 
 */
{
  inputs = {
    nixpkgs.url = "github:cachix/devenv-nixpkgs/rolling";
    systems.url = "github:nix-systems/default";
    devenv.url = "github:cachix/devenv";
    devenv.inputs.nixpkgs.follows = "nixpkgs";
  };

  nixConfig = {
    extra-trusted-public-keys = "devenv.cachix.org-1:w1cLUi8dv3hnoSPGAuibQv+f9TZLr6cv/Hm9XgU50cw=";
    extra-substituters = "https://devenv.cachix.org";
  };

  outputs = { self, nixpkgs, devenv, systems, ... } @ inputs:
    let
      forEachSystem = nixpkgs.lib.genAttrs (import systems);
      # Use a fixed JDK version of our choice.
      useFixedJDK = (final: prev: {
        jdk = final.jdk21;
      });
    in
    {
      packages = forEachSystem (system: {
        devenv-up = self.devShells.${system}.default.config.procfileScript;
        devenv-test = self.devShells.${system}.default.config.test;
      });

      devShells = forEachSystem
        (system:
          let
            pkgs = nixpkgs.legacyPackages.${system};
          in
          {
            default = devenv.lib.mkShell {
              inherit inputs pkgs;
              modules = [
                {
                  stdenv = pkgs.llvmPackages.stdenv;

                  languages = {
                    scala.enable = true;
                    c.enable = true;
                    java.enable = true;
                  };

                  overlays = [
                    useFixedJDK
                  ];

                  env = {
                    ENABLE_EXPERIMENTAL_COMPILER = "false";
                    SCALANATIVE_MODE = "debug";
                    SCALANATIVE_GC = "immix";
                    SCALANATIVE_LTO = "none";
                    SCALANATIVE_OPTIMIZE = "true";
                  };

                  hardeningDisable = [ "fortify" ];

                  packages = with pkgs; [
                    boehmgc
                    libunwind
                    clang
                    zlib
                  ];

                  enterShell = ''
                    echo 'run sbt to get started'
                  '';
                }
              ];
            };
          });
    };
}
