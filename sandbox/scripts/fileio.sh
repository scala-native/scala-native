#!/bin/bash
# script to launch test of File.scala from Scala Native (referred "SN" in the comments)

#test the output of a SN test to determine 
#if it was sucessful, and increments the test counter accordingly.
ExitCodeTest () {
	if [ $1 != 0 ]; then
		(($2++))
	fi
}

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

# script var.
TESTOUT=sandbox/target/scala-2.11/sandbox-out
CHECKBIN=JVMchecks
testres=0
testtotal=0

#File path integrity test (SN)
./$TESTOUT filePathTest
ExitCodeTest $? testres
((testtotal++))

#Non existence of a file test (SN)
scala $CHECKBIN deleteFile
./$TESTOUT fileNotCreatedDoesNotExists
ExitCodeTest $? testres
((testtotal++))

#create a file (JVM), deletes it (SN) and then test it don't exist anymore (JVM)
scala $CHECKBIN createNewFile
./$TESTOUT fileCanBeDeleted
JVMCheck $? fileDontExistsCheck testres
((testotal++))

#Create a file (JVM) and test its existence (SN)
scala $CHECKBIN createNewFile
./$TESTOUT existsTest
JVMCheck $? deleteFile testres
((testtotal++))

#Creating file (SN) and check it exists (JVM)
./$TESTOUT fileCanBeCreated
JVMCheck $? fileExistsCheck testres
scala $CHECKBIN deleteFile
((testtotal++))

#Create a file, the create again to check it returns false (SN), and check it exists 
./$TESTOUT canNotCreateTwoTimeTheSameFile
JVMCheck $? fileExistsCheck testres
scala $CHECKBIN deleteFile
((testtotal++))

#Create a directory (JVM) and check it is indeed a directory (SN)
scala $CHECKBIN createNewDir
./$TESTOUT isDirTest
JVMCheck $? deleteDir testres
((testtotal++))

#Create a directory (SN) and check it is indeed a directory (JVM)
./$TESTOUT createDirectory
JVMCheck $? isDir testres
scala $CHECKBIN deleteDir
((testtotal++))

#Test isFile returns true (SN) by creating a file (JVM)
scala $CHECKBIN createNewFile
./$TESTOUT isFileTest
ExitCodeTest $? testres
scala $CHECKBIN deleteFile
((testtotal++))

#Test isFile returns false (SN) by creating a dir (JVM)
scala $CHECKBIN createNewDir
./$TESTOUT isNotFileTest
ExitCodeTest $? testres
scala $CHECKBIN deleteDir
((testtotal++))

#Test isFile returns false (SN) with File of empty path
./$TESTOUT isFileTestWithEmptyPath
ExitCodeTest $? testres
((testtotal++))

#Test isDirectory returns true (SN) by creating a dir (JVM)
scala $CHECKBIN createNewDir
./$TESTOUT isDirTest
ExitCodeTest $? testres
scala $CHECKBIN deleteDir
((testtotal++))

#Test isDirectory returns false (SN) by creating a file (JVM)
scala $CHECKBIN createNewFile
./$TESTOUT isNotDirTest
ExitCodeTest $? testres
scala $CHECKBIN deleteFile
((testtotal++))

#Test isDirectory returns false (SN) with File of empty path
./$TESTOUT isDirTestWithEmptyPath
ExitCodeTest $? testres
((testtotal++))

#Create three dirs in child-parent relation (SN) and check their existence (JVM)
./$TESTOUT createDirs
JVMCheck $? mkdirsCheck testres
((testtotal++))

#Create a file (JVM) then rename it (SN) and checks it has been correctly renamed (JVM)
scala $CHECKBIN createNewFile
./$TESTOUT renameToNative
JVMCheck $? renameToCheck testres
((testtotal++))

#Rename a file without creating it beforehand (SN), and checks it fails (returns false)
./$TESTOUT renameToRAM
ExitCodeTest $? testres
((testtotal++))

#Set a file in ReadOnly mode (SN), and check it is in this state (JVM)
scala $CHECKBIN deleteFile
./$TESTOUT setReadOnly
JVMCheck $? isReadOnlyCheck testres
((testtotal++))

#Create a file (JVM) then check if the file can be written (SN)
scala $CHECKBIN createNewFile
./$TESTOUT canWriteTest
ExitCodeTest $? testres
scala $CHECKBIN deleteFile
((testotal++))

#Set a file in ReadOnly mode (JVM), and check we can't write it (SN)
scala $CHECKBIN setReadOnly
./$TESTOUT canNotWriteTest
ExitCodeTest $? testres
scala $CHECKBIN deleteFile
((testotal++))

#create a file (JVM) and test if we can read it (SN)
scala $CHECKBIN createNewFile
./$TESTOUT canReadTest
ExitCodeTest $? testres
scala $CHECKBIN deleteFile
((testtotal++))

#Set a file non readable (JVM), and check we can't read it (SN)
scala $CHECKBIN setReadableFalse
./$TESTOUT canNotReadTest
ExitCodeTest $? testres
scala $CHECKBIN deleteFile
((testtotal++))

#Create empty file (JVM) and test if it has length 0 (SN)
scala $CHECKBIN createNewFile
./$TESTOUT emptyFileHasLengthZero
ExitCodeTest $? testres
scala $CHECKBIN deleteFile
((testtotal++))

#Create a file with some text (JVM) and test if it has the correct length (SN)
scala $CHECKBIN writeAFileWithText
./$TESTOUT writtenFileHasCorrectLength
ExitCodeTest $? testres
scala $CHECKBIN deleteFile
((testtotal++))

#Create a file nomally (JVM) and test that it is not hidden (SN)
scala $CHECKBIN createNewFile
./$TESTOUT isNotHiddenTest
ExitCodeTest $? testres
scala $CHECKBIN deleteFile
((testtotal++))

#Create a file nomally (JVM) and test that it is hidden (SN)
scala $CHECKBIN createHiddenFile
./$TESTOUT isHiddenTest
ExitCodeTest $? testres
scala $CHECKBIN deleteHiddenFile
((testtotal++))

#Test if a path is absolute or not (SN)
./$TESTOUT isAbsoluteTest
ExitCodeTest $?
((testtotal++))

#Get the absolute path of the file (SN) writes it by pipe process to the check program (JVM), 
#then check the absolute path from the two platform are identical (JVM)
#this test has dependencies with FileOutputStream, if its implementation is boggus, this test may fail.
./$TESTOUT getAbsolutePathTest | scala $CHECKBIN getAbsolutePathCheck
((testres=testres+$?))
((testtotal++))

#Get an absolute version of the  test file (SN) writes its path by pipe process to the check program (JVM), 
#then check the absolute path from the two platform are identical (JVM)
#this test has dependencies with FileOutputStream, if its implementation is boggus, this test may fail.
./$TESTOUT getAbsoluteFileTest | scala $CHECKBIN getAbsolutePathCheck
((testres=testres+$?))
((testtotal++))

#Create a file with non canonical path, get its canonical path and compare it to the original path (SN)
./$TESTOUT getCanonicalPathTest
ExitCodeTest $? testres
((testtotal++))

#Create a file with parent dir, and test if getParent return the dir path (SN)
./$TESTOUT getParentTest
ExitCodeTest $? testres
((testtotal++))

#Create a file with parent dir, and test if getParentFile return the a File with correct path (SN)
./$TESTOUT getParentFileTest
ExitCodeTest $? testres
((testtotal++))

#Test the good root is returned by listRoots (SN)
./$TESTOUT getRoots
ExitCodeTest $? testres
((testtotal++))

#Create a file and set a last modified time (JVM), then test if lastModified returns this time (SN)
scala $CHECKBIN setLastModifiedTime
./$TESTOUT lastModifiedTest
ExitCodeTest $? testres
scala $CHECKBIN deleteFile
((testtotal++))


#Create a file (JVM) then sets a last modified time (SN) and check test if it has effectively sets this time (JVM)
scala $CHECKBIN createNewFile
./$TESTOUT setLastModifiedTest
JVMCheck $? lastModifiedCheck testres
scala $CHECKBIN deleteFile
((testtotal++))

#Test two file of the same name have the same hashCode, but not the inverse (SN)
./$TESTOUT hashCodeTest
ExitCodeTest $? testres
((testtotal++))

#Test two file of the same path have the same hashCode, but not the inverse (SN)
./$TESTOUT equalsTest
ExitCodeTest $? testres
((testtotal++))

#Test getName returns the name of the top file or directory. (SN)
./$TESTOUT getNameTest
ExitCodeTest $? testres
((testtotal++))

#Test compareTo with all combinations possible within 3 files (SN)
./$TESTOUT compareToTest
ExitCodeTest $? testres
((testtotal++))


# test counter.
if ((testres != 0)); then 
	echo -e "\n[info] File : Number of tests failed : $testres out of $testtotal\n"
else
	echo -e "\n[info] File : All $testtotal tests were successful\n"
fi

