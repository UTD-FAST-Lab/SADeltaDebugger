rm -r -f Button2_test_proj/
cp -r $DROIDBENCH_ANDROID_PROJECTS/Button2/ Button2_test_proj/
./Button2_test_proj/gradlew assembleDebug -p Button2_test_proj/
java -jar ../ViolationDeltaDebugger/target/ViolationDeltaDebugger-1.0-SNAPSHOT-jar-with-dependencies.jar -src Button2_test_proj/app/src -apk Button2_test_proj/app/build/outputs/apk/debug/app-debug.apk -bs ./test_build_script.sh -vs ./test_violation_script.sh -hdd -c
