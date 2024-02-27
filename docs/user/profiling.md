# Profiling

In this section you can find some tips on how to profile your Scala
Native binary in Linux.

## Measuring execution time and memory

-   With the `time` command you can measure execution time:

``` shell
$ time ./target/scala-2.13/scala-native-out 
real  0m0,718s
user  0m0,419s
sys   0m0,299s
```

-   With the `/usr/bin/time --verbose` command you can also see memory
    consumption:

``` 
$ /usr/bin/time --verbose ./target/scala-2.13/scala-native-out 
Command being timed: "./target/scala-2.13/scala-native-out"
User time (seconds): 0.49
System time (seconds): 0.23
Percent of CPU this job got: 99%
Elapsed (wall clock) time (h:mm:ss or m:ss): 0:00.72
Average shared text size (kbytes): 0
Average unshared data size (kbytes): 0
Average stack size (kbytes): 0
Average total size (kbytes): 0
Maximum resident set size (kbytes): 1317184
Average resident set size (kbytes): 0
Major (requiring I/O) page faults: 0
Minor (reclaiming a frame) page faults: 328341
Voluntary context switches: 1
Involuntary context switches: 70
Swaps: 0
File system inputs: 0
File system outputs: 0
Socket messages sent: 0
Socket messages received: 0
Signals delivered: 0
Page size (bytes): 4096
Exit status: 0
```

## Creating Flamegraphs

A [flamegraph](http://www.brendangregg.com/flamegraphs.html) is a
visualization of the most frequent code-paths of a program. You can use
flamegraphs to see where your program spends most of its CPU time.
Follow these steps:

-   You need to install the `perf` command if you haven\'t got it
    already:

``` shell
$ sudo apt update && sudo apt install linux-tools-generic
```

-   Then clone the flamegraph repository into e.g. `~/git/hub/`

``` shell
$ cd ~ && mkdir -p git/hub && cd git/hub/ 
$ git clone git@github.com:brendangregg/FlameGraph.git
```

-   Then navigate to your Scala Native project and, after building your
    binary, you can create a flamegraph like so:

``` shell
$ sudo perf record -F 1000 -a -g ./target/scala-2.13/scala-native-out
$ sudo perf script > out.perf
$ ~/git/hub/FlameGraph/stackcollapse-perf.pl out.perf > out.folded
$ ~/git/hub/FlameGraph/flamegraph.pl out.folded > kernel.svg
```

-   Open the file `kernel.svg` in your browser and you can zoom in the
    interactive SVG-file by clicking on the colored boxes as explained
    [here](https://github.com/brendangregg/FlameGraph/blob/master/README.md).
    A box represents a stack frame. The broader a box is the more CPU
    cycles have been spent. The higher the box is, the deeper in the
    call-chain it is.
-   The perf option `-F 1000` means that the sampling frequency is set
    to 1000 Hz. You can experiment with changing this option to get the
    right accuracy; start with e.g. `-F 99` and see what you get. You
    can then increase the sampling frequency to see if more details adds
    interesting information.

Continue to [runtime](./runtime.md).
