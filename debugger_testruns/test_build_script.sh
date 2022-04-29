#!/bin/bash

var=$(./Button2_test_proj/gradlew assembleDebug -p Button2_test_proj)
testvar='BUILD SUCCESSFUL'
if [[ "$var" == *"$testvar"* ]]; then
	echo "True"
fi
