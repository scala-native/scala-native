javalib-ext-dummies/src/main/scala/java/00_SN_READ_ME.txt¹

The Scala Native 'unit-tests-ext' project exists to internally test supported
classes which themselves use unsupported features. See the 00_SN_READ_ME.txt
in that project for additional details.

This directory tree holds selected minimal implementations of Java API
packages, classes, and methods to allow tests in 'unit-tests-ext' to link
and execute. They are not supported outside the Scala Native project
itself. The express intent is to keep the footprint small and, as
always, correct within the defined context.


1. 'javalib-ext-dummies' is the historical name of the project as it
   was ported from Scala.js. Times change, a kinder reading is
   'javalib-ext-mocks' as this is unlikely to give offensive to people
   with disabilities.
