#!/bin/bash
cwd=$(pwd)
echo $cwd
cd ../../projects/othertest_projects/cgRecursionTest/
mvn package
ret=$?
cd "$cwd"
exit $ret