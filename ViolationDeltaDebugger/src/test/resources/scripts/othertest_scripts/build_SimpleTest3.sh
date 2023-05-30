#!/bin/bash
cwd=$(pwd)
cd ../../projects/othertest_projects/SimpleTest3/
mvn package
ret=$?
cd "$cwd"
exit $ret