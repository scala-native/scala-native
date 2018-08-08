#!/usr/bin/env bash
if [[ -d $HOME/docker ]]; then ls $HOME/docker/*.tar.gz | xargs -I {file} sh -c "zcat {file} | docker load"; rm -rf $HOME/docker; fi
