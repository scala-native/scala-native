.. _maven:

Building projects with Maven
============================

Maven build for ``scala-native`` is supported by
`scalor-maven-plugin <https://github.com/random-maven/scalor-maven-plugin>`_.

Production version of the plugin is published on
`maven-central <http://search.maven.org/#search%7Cga%7C1%7Cscalor-maven-plugin>`_
and development plugin version is published on
`bintray <https://bintray.com/random-maven/maven/scalor-maven-plugin_2.12>`_.

Basic plugin setup
------------------

Define ``scala`` and ``scala-native`` version information in ``pom.xml``:

.. code-block:: xml
    <properties>
        <!-- Scala stack. -->
        <version.scala.epoch>2.11</version.scala.epoch>
        <version.scala.release>2.11.12</version.scala.release>
        <!-- Scala.native stack. -->
        <version.scanat.epoch>0.3</version.scanat.epoch>
        <version.scanat.release>0.3.7</version.scanat.release>
        <version.scanat.library>native${version.scanat.epoch}_${version.scala.epoch}</version.scanat.library>
    </properties>

Declare required ``scala`` and ``scala-native`` dependencies in ``pom.xml``:

.. code-block:: xml
    <dependencies>
        <dependency>
            <groupId>org.scala-lang</groupId>
            <artifactId>scala-library</artifactId>
            <version>${version.scala.release}</version>
        </dependency>
        <dependency>
            <groupId>org.scala-native</groupId>
            <artifactId>scalalib_${version.scanat.library}</artifactId>
            <version>${version.scanat.release}</version>
        </dependency>
    </dependencies>

Provide source code in default plugin locations:

.. code-block:: xml
    src/main/cdata      # resources for embedding into binary
    src/main/clang      # C/CPP sources for compilation and llvm linking
    src/main/scala      # Scala sources for nir compilation and llvm linking

Plugin settings and goals
-------------------------

Maven plugin settings are conceptually similar to :ref:`sbt`, but adapted for Maven context.

Maven build involves these steps:
* ``register`` scala sources with the project
* ``compile`` scala sources to produce ``nir`` classes 
* ``native-link`` of ``nir`` classes to produce binary ``runtime``
* ``native-pack`` of binary ``runtime`` into ``jar`` for distribution

Configure relevant plugin goals and Scala compiler-plugins in ``pom.xml``:

.. code-block:: xml
    <build>
        <plugins>
            <plugin>
                <groupId>com.carrotgarden.maven</groupId>
                <artifactId>scalor-maven-plugin_2.12</artifactId>
                <version>${version.scalor.plugin}</version>
                <configuration>
                    <definePluginList>
                        <!-- Generate Scala.native *.nir classes. -->
                        <dependency>
                            <groupId>org.scala-native</groupId>
                            <artifactId>nscplugin_${version.scala.release}</artifactId>
                            <version>${version.scanat.release}</version>
                        </dependency>
                    </definePluginList>
                </configuration>
                <executions>
                    <execution>
                        <!-- Produce artifact for scope="main". -->
                        <goals>
                            <goal>register-main</goal>
                            <goal>compile-main</goal>
                            <goal>scala-native-link-main</goal>
                            <goal>scala-native-pack-main</goal>
                        </goals>             
                    </execution>
                </executions>
          </plugin>
       </plugins>
    </build>

Documentation of goals relevant for ``scala-native`` build:

Goals to produce artifact in scope="main" :
* `register-main <https://random-maven.github.io/scalor-maven-plugin/2.12/register-main-mojo.html>`_
* `compile-main <https://random-maven.github.io/scalor-maven-plugin/2.12/compile-main-mojo.html>`_
* `scala-native-link-main <https://random-maven.github.io/scalor-maven-plugin/2.12/scala-native-link-main-mojo.html>`_
* `scala-native-pack-main <https://random-maven.github.io/scalor-maven-plugin/2.12/scala-native-pack-main-mojo.html>`_

Goals to produce artifact in scope="test" :
* `register-test <https://random-maven.github.io/scalor-maven-plugin/2.12/register-test-mojo.html>`_
* `compile-test <https://random-maven.github.io/scalor-maven-plugin/2.12/compile-test-mojo.html>`_
* `scala-native-link-test <https://random-maven.github.io/scalor-maven-plugin/2.12/scala-native-link-test-mojo.html>`_
* `scala-native-pack-test <https://random-maven.github.io/scalor-maven-plugin/2.12/scala-native-pack-test-mojo.html>`_

Minimal Maven project
---------------------

Start by creating an experimental project:

Plugin integration test projects
* `test-native <https://github.com/random-maven/scalor-maven-plugin/tree/master/src/it/test-native>`_

Maven plugin support
--------------------

This Maven plugin 
is not part of `scala-native <https://github.com/scala-native/scala-native>`_ project, 
please post issues with the `plugin project <https://github.com/random-maven/scalor-maven-plugin/issues>`_

Continue to :ref:`lang`.
