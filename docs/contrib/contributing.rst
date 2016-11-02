.. _contributing:

Contributing guidelines
=======================

Very important notice about the Javalib
---------------------------------------

Scala Native contains a reimplementation of part of the JDK in Scala.

***To contribute to this code, it is strictly forbidden to even look at the
source code of the Oracle JDK or OpenJDK!***

This is for license considerations: these JDKs are under a GPL-based license,
which is not compatible with our BSD 3-clause license.

Non Compatible license:

* Android libcore (GPL)
* OpenJDK (GPL)

Compatible license source code:

* `Scala.js`_ (Scala License <=> BSD 3-clause)
* `Apache Harmony project`_ (Apache 2) (Discontinued)

Coding style
------------

Scala Native is formatted via `scalafmt`_. Make sure that all of your
contributions are properly formatted before suggesting any changes.

General workflow
----------------

This the general workflow for contributing to Scala Native.

1.  Make sure you have signed the `Scala CLA`_. If not, sign it.

2.  You should always perform your work in its own Git branch.
    The branch should be given a descriptive name that explains its intent.

3.  When the feature or fix is completed you should open a `Pull Request`_
    on GitHub.

4.  The Pull Request should be reviewed by other maintainers (as many as
    feasible/practical), among which at least one core developer.
    Independent contributors can also participate in the review process,
    and are encouraged to do so.

5.  After the review, you should resolve issues brought up by the reviewers as
    needed (amending or adding commits to address reviewers' comments),
    iterating until the reviewers give their thumbs up, the "LGTM" (acronym for
    "Looks Good To Me").

6.  Once the code has passed review the Pull Request can be merged into
    the distribution.

Pull Request Requirements
-------------------------

In order for a Pull Request to be considered, it has to meet these requirements:

1.  Live up to the current code standard:

    - Be formatted with `./bin/scalafmt`_.
    - Not violate `DRY`_.
    - `Boy Scout Rule`_ should be applied.

2.  Be accompanied by appropriate tests.

3.  Be issued from a branch *other than master* (PRs coming from master will not
    be accepted.)

If not *all* of these requirements are met then the code should **not** be
merged into the distribution, and need not even be reviewed.

Documentation
-------------

All code contributed to the user-facing standard library (the `nativelib/`
directory) should come accompanied with documentation.
Pull requests containing undocumented code will not be accepted.

Code contributed to the internals (nscplugin, tools, etc.)
should come accompanied by internal documentation if the code is not
self-explanatory, e.g., important design decisions that other maintainers
should know about.

Creating Commits And Writing Commit Messages
--------------------------------------------

Follow these guidelines when creating public commits and writing commit messages.

Prepare meaningful commits
--------------------------

If your work spans multiple local commits (for example; if you do safe point
commits while working in a feature branch or work in a branch for long time
doing merges/rebases etc.) then please do not commit it all but rewrite the
history by squashing the commits into **one commit per useful unit of
change**, each accompanied by a detailed commit message.
For more info, see the article: `Git Workflow`_.
Additionally, every commit should be able to be used in isolation--that is,
each commit must build and pass all tests.

First line of the commit message
--------------------------------

The first line should be a descriptive sentence about what the commit is
doing, written using the imperative style, e.g., "Change this.", and should
not exceed 70 characters.
It should be possible to fully understand what the commit does by just
reading this single line.
It is **not ok** to only list the ticket number, type "minor fix" or similar.
If the commit has a corresponding ticket, include a reference to the ticket
number, with the format "Fix #xxx: Change that.", as the first line.
Sometimes, there is no better message than "Fix #xxx: Fix that issue.",
which is redundant.
In that case, and assuming that it aptly and concisely summarizes the commit
in a single line, the commit message should be "Fix #xxx: Title of the ticket.".

Body of the commit message
--------------------------

If the commit is a small fix, the first line can be enough.
Otherwise, following the single line description should be a blank line
followed by details of the commit, in the form of free text, or bulleted list.

.. _Scala.js: https://github.com/scala-js/scala-js/tree/master/javalib/src/main/scala/java
.. _Apache Harmony project: https://github.com/apache/harmony
.. _scalafmt: https://github.com/olafurpg/scalafmt
.. _Scala CLA: http://typesafe.com/contribute/cla/scala
.. _Pull Request: https://help.github.com/articles/using-pull-requests
.. _DRY: http://programmer.97things.oreilly.com/wiki/index.php/Don%27t_Repeat_Yourself
.. _Boy Scout Rule: http://programmer.97things.oreilly.com/wiki/index.php/The_Boy_Scout_Rule
.. _Git Workflow: http://sandofsky.com/blog/git-workflow.html
