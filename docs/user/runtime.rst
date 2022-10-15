.. _runtime:

Runtime Settings
================

Scala Native comes with some ability to change the runtime
characteristics.

Garbage Collection (GC) Heap Size Settings
------------------------------------------

Scala Native supports the `Boehm-Demers-Weiser Garbage Collector <https://www.hboehm.info/gc/>`_.
The environment variables defined in Boehm are planned to be used for all the supported Garbage
Collectors so the variables are shared and consistent.

All Garbage Collectors
----------------------

The following environment variables are used for all the GCs. They can go as low as 1 MB or
maybe less for Boehm and as high as your system memory up to about 512 GB. The size is in bytes,
kilobytes(k or K), megabytes(m or M), or gigabytes(g or G). Example: 1M or 1G

* Change the minimum heap size by setting the GC_INITIAL_HEAP_SIZE environment variable.
* Change the maximum heap size by setting the GC_MAXIMUM_MAX_HEAP_SIZE environment variable.

None GC
---------------

The None GC uses the two settings above with the other Garbage Collectors.

Immix GC
--------

The following variables have not been changed to match the standard variables in Immix yet.

* Change the minimum heap size by setting the SCALANATIVE_MIN_HEAP_SIZE environment variable.
* Change the maximum heap size by setting the SCALANATIVE_MAX_HEAP_SIZE environment variable.

Commix Heap Size Settings
-------------------------

In addition to the environment variable setting described above for Immix, Commix
also adds a few more which do not match the Boehm settings yet.

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

The plan is to add more GC settings in the future using the Boehm GC when able. To see a list of
variables refer to following `README <https://github.com/ivmai/bdwgc/blob/master/doc/README.environment>`_.
