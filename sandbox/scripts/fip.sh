#!/bin/bash
# script to launch test of FileInputStream from Scala Native (referred "SN" in the comments)

#test the output of a SN test to determine 
#if it was sucessful, and increments the test counter accordingly.
ExitCodeTest () {
	if [ $1 != 0 ]; then
		(($2++))
	fi
}

TESTOUT=sandbox/target/scala-2.11/sandbox-out
CHECKBIN=JVMchecks
testres=0
testtotal=0

# Create a file with texte written (JVM), and test the good number of byte is available (SN)
scala $CHECKBIN writeAFileWithText
./$TESTOUT readStringFromFileTest
ExitCodeTest $? testres
scala $CHECKBIN deleteFile
((testtotal++))

# Create a file with texte written (JVM), and test the good number of byte is available (SN)
scala $CHECKBIN writeAFileWithText
./$TESTOUT availableFromFileTest
ExitCodeTest $? testres
scala $CHECKBIN deleteFile
((testtotal++))

# Create a file with texte written (JVM), and test skipping on byte in reading (SN)
scala $CHECKBIN writeAFileWithText
./$TESTOUT skipOneByteTest
ExitCodeTest $? testres
scala $CHECKBIN deleteFile
((testtotal++))

#test an exception is correctly launched when trying to skip negative amount of byte (SN)
scala $CHECKBIN writeAFileWithText
./$TESTOUT skipExceptionsTest
ExitCodeTest $? testres
scala $CHECKBIN deleteFile
((testtotal++))

#Writes to stdout (JvM), then using bash pipe process, test we can read the output (SN)
scala $CHECKBIN writeToStdout | ./$TESTOUT stdinReadTest
ExitCodeTest $? testres
((testtotal++))

#Test number of bytes of stdin is zero by default (SN)
./$TESTOUT stdinAvailableNoInput
ExitCodeTest $? testres
((testtotal++))

#Test number of bytes of stdin is correct (SN) when writing to its stdin beforehand (BASH)
echo "Hello World !" | ./$TESTOUT stdinAvailableWithInput
ExitCodeTest $? testres
((testtotal++))

#Test skipping a byte on stdin works (SN) when writing to its stdin beforehand (BASH)
echo "Hello World !" | ./$TESTOUT stdinSkipTest
ExitCodeTest $? testres
((testtotal++))

#Create a file in with text written (JVM), remove its read allowance (BASH) and then check we can't read from it (SN)
scala $CHECKBIN writeAFileWithText
chmod ugo-r testFile
./$TESTOUT cannotReadFromAProtectedFile
ExitCodeTest $? testres
((testtotal++))
rm -rf testFile


if ((testres != 0)); then 
	echo -e "\n[info] FileInputStream : Number of tests failed : $testres out of $testtotal\n"
else
	echo -e "\n[info] FileInputStream : All $testtotal tests were successful\n"
fi

