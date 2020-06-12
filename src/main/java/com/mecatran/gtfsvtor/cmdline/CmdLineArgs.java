package com.mecatran.gtfsvtor.cmdline;

import com.beust.jcommander.Parameter;

public class CmdLineArgs {

	@Parameter(names = { "-h",
			"--help" }, description = "Display this help and exit")
	private boolean help = false;

	@Parameter(names = {
			"--listValidators" }, description = "List validators and their parameters, and exit")
	private boolean listValidators = false;

	@Parameter(names = { "-v",
			"--verbose" }, description = "Enable verbose mode")
	private boolean verbose = false;

	@Parameter(names = { "-p",
			"--printIssues" }, description = "Print issues log to standard output")
	private boolean printIssues = false;

	@Parameter(names = { "-c",
			"--config" }, description = "Configuration file to load (properties file)")
	private String configFile = null;

	@Parameter(names = { "-o",
			"--output" }, description = "Validation report output file")
	private String outputReportFile = "validation-results.html";

	@Parameter(names = { "-l",
			"--limit" }, description = "Limit number of issues per category")
	private int maxIssuesPerCategoryLimit = 100;

	@Parameter(names = {
			"--numThreads" }, description = "Number of threads for running DAO validators in parallel")
	private int numThreads = 1;

	@Parameter(names = {
			"--maxStopTimesInterleaving" }, description = "Max number of interleaved trips in stop_times.txt "
					+ "" + "(number of concurrent 'opened' trips). "
					+ "Use/increase this option if you have lots of unordered trips in stop_times.txt, "
					+ "to improve loading performances.")
	private int maxStopTimesInterleaving = 100;

	@Parameter(description = "<GTFS file to validate>")
	private String gtfsFile;

	public boolean isHelp() {
		return help;
	}

	public boolean isListValidators() {
		return listValidators;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public boolean isPrintIssues() {
		return printIssues;
	}

	public String getConfigFile() {
		return configFile;
	}

	public String getOutputReportFile() {
		return outputReportFile;
	}

	public int getMaxIssuesPerCategoryLimit() {
		return maxIssuesPerCategoryLimit;
	}

	public int getNumThreads() {
		return numThreads;
	}

	public int getMaxStopTimeInterleaving() {
		return maxStopTimesInterleaving;
	}

	public String getGtfsFile() {
		return gtfsFile;
	}
}
