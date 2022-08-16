package cs.utd.soles.setup;


import picocli.CommandLine;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ArgsHandler {

    @CommandLine.Option(names = "--runPrefix", description="The prefix to append to run artifacts.")
    public String runPrefix;

    @CommandLine.Option(names="--sources", description = "Directory/ies where source code is.", required = true)
    public List<File> sources;

    @CommandLine.Option(names="--target", description="The compiled target program to run on.", required = true)
    public File target;

    @CommandLine.Option(names="--vs", description="The violation script. " +
            "Expects a return code of 0 if the violation was reproduced, and takes the compiled program (e.g., JAR" +
            " or APK) as a parameter.", required = true)
    public File vs;

    @CommandLine.Option(names="--bs", description="The build script. The delta debugger expects this script to " +
            "return 0 if the build was successful, and to output the compiled program in the same place as the" +
            " --target parameter.", required = true)
    public File bs;

    @CommandLine.Option(names="--log", description="Enable logging.")
    public boolean log;

    @CommandLine.Option(names="--class-reduction", description="Enable class-based reduction, " +
            "consistent with Kalhauge and Palsberg's approach.")
    public boolean classReduction;

    @CommandLine.Option(names="--method-reduction", description = "Enable method based reduction [EXPERIMENTAL]")
    public boolean methodReduction;

    @CommandLine.Option(names="--no_abstract", description="Do we still use this option?")
    public boolean noAbstract;

    @CommandLine.Option(names="--help", usageHelp = true)
    public boolean help;

    @CommandLine.Option(names="--hdd", description="Enable hierarchical delta debugging.")
    public boolean hdd;

    @CommandLine.Option(names="--no_opt", description="Disable optimization (Dakota, what is this?)")
    public
    boolean noOpt;

    @CommandLine.Option(names="--timeout", description="The timeout for the whole delta debugging process in minutes.")
    public
    Optional<Integer> timeoutMinutes;


    @CommandLine.Option(names="--check-determinism", description = "[DEPRECATED] Check determinism of the " +
            "analysis result.")
    public
    boolean checkDeterminism;

    @CommandLine.Option(names="--logs", description = "Where to write log.", required = true)
    public File logFile;

}
