package com.mecatran.gtfsvtor.cmdline;

import com.beust.jcommander.Parameter;

public class CmdLineArgs {

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
	private String outputReportFile = "validation-report.html";

	@Parameter(description = "GTFS file to validate")
	private String gtfsFile = ".";

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

	public String getGtfsFile() {
		return gtfsFile;
	}

}