.. _contributing:

Contributing guidelines
=======================

Very important notice about Javalib
-----------------------------------

Scala Native contains a re-implementation of part of the JDK.

Although the `GPL and Scala License are compatible`_ and the `GPL and
Scala CLA are compatible`_, EPFL wish to distribute scala native
under a permissive license.

When you sign the `Scala CLA`_ you are confirming that your
contributions are your own creation. This is especially important, as
it denies you the ability to copy any source code, e.g. Android,
OpenJDK, Apache Harmony, GNU Classpath or Scala.js. To be clear, you
are personally liable if you provide false information regarding the
authorship of your contribution.

However, we are prepared to accept contributions that include code
copied from `Scala.js`_ or `Apache Harmony project`_ on a case-by-case
basis. In such cases, you must fulfil your obligations and include the
relevant copyright / license information.


Coding style
------------

Scala Native is formatted via `./bin/scalafmt` and `./bin/clangfmt`.
Make sure that all of your contributions are properly formatted before
suggesting any changes.

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

Git workflow
------------

Scala Native repositories maintain a linear merge-free history on the master
branch. All of the incoming pull requests are merged using squash and merge
policy (i.e. one merged pull request corresponds to one squashed commit to the
master branch.)

You do not need to squash commits manually. It's typical to add new commits
to the PR branch to accommodate changes that were suggested by the reviewers.
Squashing things manually and/or rewriting history on the PR branch is all-right
as long as it's clear that concerns raised by reviewers have been addressed.

Maintaining a long-standing work-in-progress (WIP) branch requires one to rebase
on top of latest master using ``git rebase --onto`` from time to time.
It's strongly recommended not to perform any merges on your branches that you
are planning to use as a PR branch.

Pull Request Requirements
-------------------------

In order for a Pull Request to be considered, it has to meet these requirements:

1.  Live up to the current code standard:

    - Be formatted with `./bin/scalafmt` and `./bin/clangfmt`.
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
.. _Scala CLA: http://typesafe.com/contribute/cla/scala
.. _Pull Request: https://help.github.com/articles/using-pull-requests
.. _DRY: http://programmer.97things.oreilly.com/wiki/index.php/Don%27t_Repeat_Yourself
.. _Boy Scout Rule: http://programmer.97things.oreilly.com/wiki/index.php/The_Boy_Scout_Rule
.. _Git Workflow: http://sandofsky.com/blog/git-workflow.html
.. _GPL and Scala License are compatible: https://www.gnu.org/licenses/license-list.html#ModifiedBSD
.. _GPL and Scala CLA are compatible: https://www.gnu.org/licenses/license-list.html#apache2
