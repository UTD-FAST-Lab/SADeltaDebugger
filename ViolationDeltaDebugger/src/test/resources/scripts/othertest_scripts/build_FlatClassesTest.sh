#!/bin/bash
cwd=$(pwd)
echo $cwd
cd ../../projects/othertest_projects/FlatClassesTest/
mvn package
ret=$?
cd "$cwd"
exit $ret