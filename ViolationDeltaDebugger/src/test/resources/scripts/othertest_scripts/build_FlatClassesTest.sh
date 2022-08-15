#!/bin/bash
cwd=$(pwd)
cd ../../projects/othertest_projects/FlatClassesTest/
mvn package
ret=$?
cd $cwd
exit $ret