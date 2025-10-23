
// Results: "-v" at least prints out test name when started. 

ThisBuild / testOptions +=
           Tests.Argument(TestFrameworks.JUnit, "-a", "-s", "-v")

