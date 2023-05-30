#!/bin/bash
cwd=$(pwd)
cd ../../projects/othertest_projects/SimpleTest1/
mvn compile
cd "$cwd"
exit 0