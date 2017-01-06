#!/bin/bash
# script to launch test of FileOutPutStream from scala native

#test the output of a SN test to determine wether it was sucessful
#if so, will make the corresponding check on the JVM and increments 
#the test counter accordingly.
JVMCheck () {
	if [ $1 != 0 ]; then
		(($3++))
	else
		scala $CHECKBIN $2
		(($3=$3+$?))
	fi
}

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

#Check (JVM) FileOutputStream in non-append mode overwrites correctly a file (SN)
scala $CHECKBIN deleteFile
scala $CHECKBIN createFileWithAlreadyWrittenText
./$TESTOUT owerwriteConstructor
scala $CHECKBIN constructorFileCheck
((testres=testres+$?))
((testtotal++))

#Check (JVM) FileOutputStream in append mode correctly writes at the end of a file (SN)
scala $CHECKBIN deleteFile
scala $CHECKBIN createFileWithAlreadyWrittenText
./$TESTOUT appendConstructor
scala $CHECKBIN constructorFileAppendTrueCheck
((testres=$testres+$?))
((testtotal++))
scala $CHECKBIN deleteFile

#Writes to stdout (SN), then using bash pipe process, checks the output was correct (JVM)
./$TESTOUT writeToStdoutTest | scala $CHECKBIN checkStdin
((testres=$testres+$?))
((testtotal++))

#Create a dir (JVM), then instatiates a new FOS with this dir, and checks the corresponding exception gets launched (SN)
scala $CHECKBIN createNewDir
./$TESTOUT cannotWriteInADirectoryTest
ExitCodeTest $? testres
scala $CHECKBIN deleteDir
((testtotal++))


#Write a non existing file (SN) and checks it was written correctly (JVM)
./$TESTOUT owerwriteConstructor
scala $CHECKBIN constructorFileCheck
((testres=testres+$?))
((testtotal++))
scala $CHECKBIN deleteFile

#Create a file without allowance to write (BASH) and then check we can't write in it (SN)
touch testFile
chmod ugo-w testFile
./$TESTOUT cannotWriteInAProtectedFile
ExitCodeTest $? testres
((testtotal++))
rm -rf testFile


if ((testres != 0)); then 
	echo -e "\n[info] FileOutputStream : Number of tests failed : $testres out of $testtotal\n"
else
	echo -e "\n[info] FileOutputStream : All $testtotal tests were successful\n"
fi

