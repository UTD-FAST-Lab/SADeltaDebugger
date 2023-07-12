#!/bin/bash
cwd=$(pwd)
echo $cwd
cd ../../projects/othertest_projects/cgDepthTest/
mvn package
ret=$?
cd "$cwd"
exit $ret