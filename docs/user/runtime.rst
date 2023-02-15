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
* GC_MAXIMUM_MAX_HEAP_SIZE changes the maximum heap size.

The plan is to add more GC settings in the future using the Boehm setting names where applicable.

Boehm GC
--------

The Boehm GC uses the two variables shown above. The following document shows all the variables
available for Boehm: `README <https://github.com/ivmai/bdwgc/blob/master/docs/README.environment>`_.

None GC
-------

The None GC uses the two variables shown above.

Immix GC
--------

The following variables have not been changed to match the standard variables in Immix yet.

* SCALANATIVE_MIN_HEAP_SIZE changes the minimum heap size.
* SCALANATIVE_MAX_HEAP_SIZE changes the maximum heap size.

Commix GC
---------

In addition to the variables described above for Immix, Commix
also adds a few more variables which do not match the Boehm settings yet.

* SCALANATIVE_GC_THREADS (default is processor count - 1)
* SCALANATIVE_TIME_RATIO (default is .05)
* SCALANATIVE_FREE_RATIO (default is .5)

Examples
--------

If you are developing in the Scala Native sandbox, the following are examples
showing some error conditions using Immix, the default GC. Adjust the path to
your executable as needed:

.. code-block:: shell

    $ export SCALANATIVE_MIN_SIZE=64k; export SCALANATIVE_MAX_SIZE=512k; sandbox/.2.13/target/scala-2.13/sandbox-out
    SCALANATIVE_MAX_HEAP_SIZE too small to initialize heap.
    Minimum required: 1m

    $ export SCALANATIVE_MIN_SIZE=2m; export SCALANATIVE_MAX_SIZE=1m; sandbox/.2.13/target/scala-2.13/sandbox-out
    SCALANATIVE_MAX_HEAP_SIZE should be at least SCALANATIVE_MIN_HEAP_SIZE


