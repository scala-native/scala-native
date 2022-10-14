.. _runtime:

Runtime Settings
================

Scala Native comes with some ability to change the runtime
characteristics.

Garbage Collection (GC) Heap Size Settings
------------------------------------------

* Change the minimum heap size by setting the SCALANATIVE_MIN_HEAP_SIZE environment variable.
* Change the maximum heap size by setting the SCALANATIVE_MAX_HEAP_SIZE environment variable.
* They can go as low as 1 MB and as high as your system memory up to about 512 GB.
* The size is in bytes, kilobytes(k or K), megabytes(m or M), or gigabytes(g or G).

Commix Heap Size Settings
-------------------------

In addition to the environment variable setting described above, Commix
also adds a few more.

* SCALANATIVE_GC_THREADS (default is processor count - 1)
* SCALANATIVE_TIME_RATIO (default is .05)
* SCALANATIVE_FREE_RATIO (default is .5)

If you are developing in the Scala Native sandbox, the following are examples
showing some error conditions. Adjust the path to your executable as needed:

.. code-block:: shell

    $ export SCALANATIVE_MIN_SIZE=64k; export SCALANATIVE_MAX_SIZE=512k; sandbox/.2.13/target/scala-2.13/sandbox-out
    SCALANATIVE_MAX_HEAP_SIZE too small to initialize heap.
    Minimum required: 1m

    $ export SCALANATIVE_MIN_SIZE=2m; export SCALANATIVE_MAX_SIZE=1m; sandbox/.2.13/target/scala-2.13/sandbox-out
    SCALANATIVE_MAX_HEAP_SIZE should be at least SCALANATIVE_MIN_HEAP_SIZE
