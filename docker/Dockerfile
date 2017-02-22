FROM nixos/nix
 
RUN nix-channel --update

RUN nix-env -i git

WORKDIR /tmp

RUN git clone https://github.com/scala-native/scala-native.git native

RUN git clone https://github.com/scala-native/sbt-crossproject.git
  
WORKDIR /tmp/native

RUN  nix-shell bin/scala-native.nix -A clangEnv --run "echo 'clangEnv installed'"

RUN  nix-shell bin/scala-native.nix -A clangEnv --run "cd .. && sbt scalaVersion"

RUN  nix-shell bin/scala-native.nix -A clangEnv --run "cd .. && cd sbt-crossproject && sbt publishLocal"

RUN  nix-shell bin/scala-native.nix -A clangEnv --run "sbt rebuild"

CMD ["/bin/bash"]
