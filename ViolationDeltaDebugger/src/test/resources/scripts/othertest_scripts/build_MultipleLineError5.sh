#!/bin/bash
cwd=$(pwd)
cd ../../projects/othertest_projects/MultipleLineError5/
mvn package
ret=$?
cd $cwd
exit $ret