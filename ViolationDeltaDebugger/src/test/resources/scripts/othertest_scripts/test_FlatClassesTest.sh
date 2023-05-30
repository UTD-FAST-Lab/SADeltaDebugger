#!/bin/bash
cwd=$(pwd)
SCRIPT_PATH='../is_divide_by_0.sh'
cd ../../projects/othertest_projects/FlatClassesTest
test=$(java -jar target/FlatClassesTest-1.0-SNAPSHOT-jar-with-dependencies.jar 2>&1)
cd "$cwd"
exit $(bash $SCRIPT_PATH "$test")

