
0. Prequisites

1. Setup environment variables
	JAVA_HOME="your java version", needs to be 1.8 probably
	DELTA_DEBUGGER_HOME="wherever you put the repo"

2. Build Debugger Prerequisites
	mvn install in resources/delta_debugger/ProjectLineCounter
	
3. Build Debugger
	mvn package in resources/delta_debugger/ViolationDeltaDebugger

4. Explanation of Options
	
	Positional Arguments
	0	Path to main/src of java code
	1	Path to the apk created by the build script
	2	Path to the build script (shell)
	3	Path to the recreation script (shell)

	Option 				Type		Explanation
	-l				Optional	This option turns on logging data for debugging
	-c				Reduction	This option enables the debugger to perform a class-based reduction
	-m 				Reduction	This option enables the debugger to perform a method-based reduction (not implemented)
	-hdd				Reduction	This option enables the debugger to perform a regular HDD reduction
	-nam				Optional	This option enables the optimization for non-removal of abstract/interface methods (doesn't work)
	-no_opt				Optional	This option disables optimizations for the HDD algorithm (currently this is only the source/sink non-removal)
	-p <string>			Required	This option provides a name for the run called a prefix, use it by doing -p prefix
	-t <int>			Optional	This option allows you to specify a time for the HDD part of the reduction
	-bt <int>			Optional	This option allows you to specify a time for the class-based reduction
	-root_projects <string> 	Optional	This option is not needed, specify the directory the project your are debugging is in for droidbench this will be where you placed the droidbench_android_projects repo
	-check_d			Optional	This option tells the debugger to run determinism checks

5. setup the symbolic links and required files for debugger
	goto deltadebugger_runs/ and setup correct symbolic links
	
	ln -s ../resources/delta_debugger/ViolationDeltaDebugger/target/ViolationDeltaDebugger-1.0-SNAPSHOT-jar-with-dependencies.jar
	ln -s (AndroidTAEnvironment)/resources/scripts/runaql.py
	ln -s (AndroidTAEnvironment)/resources/scripts/run_aql.sh
	ln -s (AndroidTAEnvironment)/AQL-System/target/build/flushMemory.sh
	ln -s (AndroidTAEnvironment)/AQL-System/target/build/AQL-System-1.1.0-SNAPSHOT.jar
	ln -s (AndroidTAEnvironment)/AQL-System/target/build/AQL_Lib-1.1.0-SNAPSHOT.jar
	mkdir data
	mkdir data/storage
	mkdir sootOutput
	mkdir answers
	touch log.txt

6. Run
	in the deltadebugger_runs/ directory
	./debugger_testruns/runOneTest.sh <path to droidbench_android_projects repo>
7. Did Run work?
	
	In 6. we ran the debugger on the droidbench project Button2 to reproduce a soundness violation.
	If it successfuly worked there should be a file called 
debugger/singleTestRun_soundness_Callbacks_Button2_8c1eb69cc117c307729ac97fd5f617a1_1ef9d1d10bc982ab49d3b8eaf4409c05_time.txt.
This is the log file for the debugger run and contains the information about how the reduction performed on the app,
if you have a line like:
 end_line_count: 28
then you have successfully setup your debugger!
