#!/bin/bash
cwd=$(pwd)
cd ../../projects/othertest_projects/SimpleTest1/
mvn package
ret=$?
cd "$cwd"
exit $ret