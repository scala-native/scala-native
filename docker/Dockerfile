FROM nixos/nix
 
RUN nix-channel --update

RUN nix-env -i git

WORKDIR /tmp

RUN git clone https://github.com/scala-native/scala-native.git native
  
WORKDIR /tmp/native

RUN  nix-shell scripts/scala-native.nix -A clangEnv --run "echo 'clangEnv installed'"

RUN  nix-shell scripts/scala-native.nix -A clangEnv --run "cd .. && sbt scalaVersion"

RUN  nix-shell scripts/scala-native.nix -A clangEnv --run "sbt rebuild"

CMD ["/bin/bash"]
