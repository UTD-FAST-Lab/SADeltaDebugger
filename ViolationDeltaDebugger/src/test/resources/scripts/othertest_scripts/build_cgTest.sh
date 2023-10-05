#!/bin/bash
cwd=$(pwd)
echo $cwd
cd ../../projects/othertest_projects/cgTest/
mvn package
ret=$?
cd "$cwd"
exit $ret