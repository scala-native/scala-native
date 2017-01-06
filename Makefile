all: JVMchecks testCompile fileioTest fopTest fipTest clean

JVMchecks: 
	scalac sandbox/scripts/JVMchecks.scala

testCompile: 
	sbt sandbox/nativeLink

onlyTests: JVMchecks fileioTest fopTest fipTest

fopTest: 
	./sandbox/scripts/fop.sh

fileioTest:
	./sandbox/scripts/fileio.sh

fipTest:
	./sandbox/scripts/fip.sh


javalibCompile:
	sbt javalib/clean
	sbt javalib/publishLocal

allCompile:
	sbt clean
	sbt nscplugin/publishLocal nativelib/publishLocal publishLocal

clean: 
	rm -rf *.class