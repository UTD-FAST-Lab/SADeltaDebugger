#!/bin/bash
cwd=$(pwd)
cd ../../projects/othertest_projects/SimpleTest2/
mvn package
ret=$?
cd "$cwd"
exit $ret