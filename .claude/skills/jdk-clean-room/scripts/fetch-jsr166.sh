#!/usr/bin/env bash
# Checks out public-domain (CC0) JSR166 sources from Doug Lea's anonymous CVS.
# Usage: fetch-jsr166.sh <output-dir> [file ...]   (files relative to src/main/java/util/concurrent)
set -euo pipefail
out="${1:?output dir}"; shift
files=("${@:-ForkJoinPool.java ForkJoinTask.java ForkJoinWorkerThread.java SubmissionPublisher.java}")
export CVSROOT=':pserver:anonymous@gee.cs.oswego.edu/home/jsr166/jsr166'
touch "$HOME/.cvspass"
mkdir -p "$out" && cd "$out"
for f in ${files[@]}; do
  cvs -z3 -Q checkout "jsr166/src/main/java/util/concurrent/$f"
  cvs -Q log -N "jsr166/src/main/java/util/concurrent/$f" > "$f.cvslog"
done
echo "Checked out into $out/jsr166/src/main/java/util/concurrent (CVS logs alongside)"
