
0. Prequisites

1. Setup environment variables
	JAVA_HOME="your java version", needs to be 1.8 probably
	DELTA_DEBUGGER_HOME="wherever you put the repo"

2. Build Debugger Prerequisites
	mvn install in ../ProjectLineCounter
	
3. Build Debugger
	mvn package in ../ViolationDeltaDebugger

4. Explanation of Options
	
	Required Arguments
	-src <space seperated list of src directories> 
	-apk <path to apk file build script is making>
	-bs <path to build script>
	-vs <path to testing scrip>

	Reduction Arguments (what reduction you want done)
	-c		Class-based reduction
	-hdd 		Heirarichal delta debugging

	Option 				Type		Explanation
	-l				Optional	This option turns on logging data for debugging
	-nam				Optional	This option enables the optimization for non-removal of abstract/interface methods (doesn't work)
	-no_opt				Optional	This option disables optimizations for the HDD algorithm (currently this is only the source/sink non-removal)
	-p <string>			Optional	This option provides a name for the run called a prefix, use it by doing -p prefix
	-t <int>			Optional	This option allows you to specify a time for the HDD part of the reduction
	-bt <int>			Optional	This option allows you to specify a time for the class-based reduction
	-check_d			Optional	This option tells the debugger to run determinism checks

5. Test run
	Prereq for Debugger test is to have droidbench_android_projects repo and an env var DROIDBENCH_ANDROID_PROJECTS="place you put repo"
	Direct to ../debugger_testruns/
	./run_new_debugger_test
	Should have created a new Button2_test_proj/ and deleted all the code in it, as well as making a debugger/ directory
	with minimized_apks/ that contains a minimized apk for Button2 and Button2_time.txt with run details
