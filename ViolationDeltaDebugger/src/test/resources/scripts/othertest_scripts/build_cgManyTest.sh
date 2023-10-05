#!/bin/bash
cwd=$(pwd)
echo $cwd
cd ../../projects/othertest_projects/cgManyTest/
mvn package
ret=$?
cd "$cwd"
exit $ret