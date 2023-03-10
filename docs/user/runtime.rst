.. _runtime:

Runtime Settings
================

Scala Native comes with some ability to change the runtime characteristics.

Garbage Collector (GC) Settings
------------------------------------------

Scala Native supports the `Boehm-Demers-Weiser Garbage Collector <https://www.hboehm.info/gc/>`_.
The environment variables defined in Boehm are planned to be shared by all the Garbage
Collectors supported in Scala Native so they are consistent. The variables supported are listed
below for each GC.


All Garbage Collectors
----------------------

The following environment variables will be used for all GCs. They can go from 1 MB to
the system memory maximum or up to about 512 GB. The size is in bytes,
kilobytes(k or K), megabytes(m or M), or gigabytes(g or G). Examples: 1024k, 1M, or 1G etc.

* GC_INITIAL_HEAP_SIZE changes the minimum heap size.
* GC_MAXIMUM_HEAP_SIZE changes the maximum heap size.

The plan is to add more GC settings in the future using the Boehm setting names where applicable.

Boehm GC
--------

The Boehm GC uses the two variables shown above. The following is available for Boehm
and Commix.

* GC_NPROCS

The following document shows all the variables available for Boehm:
`README <https://github.com/ivmai/bdwgc/blob/master/docs/README.environment>`_.

None GC
-------

The None GC uses the two variables shown above.

Immix GC
--------

The Immix GC uses the two variables shown above as well as the following variable.

* SCALANATIVE_STATS_FILE (set to the file name)

Commix GC
---------

In addition to the variables described above for Immix, Commix has the following
variable shared with Boehm.

* GC_NPROCS (default is processor count - 1 up to 8 maximum)

Commix also adds a few more variables which do not match the Boehm settings yet.

* SCALANATIVE_TIME_RATIO (default is .05)
* SCALANATIVE_FREE_RATIO (default is .5)

Note: STATS_FILE_SETTING shared with Immix is only available if the compiler defines
-DENABLE_GC_STATS for Commix.

Examples
--------

If you are developing in the Scala Native sandbox, the following are examples
showing some error conditions using Immix, the default GC. Adjust the path to
your executable as needed:

.. code-block:: shell

    $ export GC_INITIAL_HEAP_SIZE=64k; export GC_MAXIMUM_HEAP_SIZE=512k; sandbox/.2.13/target/scala-2.13/sandbox
    GC_MAXIMUM_HEAP_SIZE too small to initialize heap.
    Minimum required: 1m

    $ export GC_INITIAL_HEAP_SIZE=2m; export GC_MAXIMUM_HEAP_SIZE=1m; sandbox/.2.13/target/scala-2.13/sandbox
    GC_MAXIMUM_HEAP_SIZE should be at least GC_INITIAL_HEAP_SIZE


