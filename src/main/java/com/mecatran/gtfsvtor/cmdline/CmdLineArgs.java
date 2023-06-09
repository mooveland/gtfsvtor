package com.mecatran.gtfsvtor.cmdline;

import java.io.IOException;
import java.util.Optional;

import com.beust.jcommander.Parameter;
import com.mecatran.gtfsvtor.lib.GtfsVtorOptions;
import com.mecatran.gtfsvtor.reporting.FormattingOptions;
import com.mecatran.gtfsvtor.reporting.FormattingOptions.SpeedUnit;

public class CmdLineArgs implements GtfsVtorOptions {

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

	@Parameter(names = { "-o", "--htmlOutput" }, description = ""
			+ "HTML validation report output file. "
			+ "Use --htmlOutput '' to disable HTML output generation.")
	private String htmlReportFile = "validation-results.html";

	@Parameter(names = {
			"--jsonOutput" }, description = "JSON validation report (summary only for now) output file")
	private String jsonReportFile = null;

	@Parameter(names = {
			"--jsonAppend" }, description = "Append JSON validation run to existing report, if any")
	private boolean appendMode = false;

	@Parameter(names = { "-l",
			"--limit" }, description = "Limit number of issues per category")
	private int maxIssuesPerCategoryLimit = 100;

	@Parameter(names = {
			"--numThreads" }, description = "Number of threads for running DAO validators in parallel")
	private int numThreads = 1;

	@Parameter(names = { "--maxStopTimesInterleaving" }, description = ""
			+ "Max number of interleaved trips in stop_times.txt "
			+ "(number of concurrent 'opened' trips) in PACKED stop time mode. "
			+ "Increase this option if you have some data not ordered by trip ID, "
			+ "but still want to use PACKED mode, to improve loading performances. "
			+ "Otherwise, use --stopTimesMode UNSORTED or AUTO option.")
	private int maxStopTimesInterleaving = 100;

	@Parameter(names = { "--maxShapePointsInterleaving" }, description = ""
			+ "Max number of interleaved shapes in shapes.txt "
			+ "(number of concurrent 'opened' shapes). "
			+ "Use/increase this option if you have lots of unordered shape points in shapes.txt, "
			+ "to improve loading performances.")
	private int maxShapePointsInterleaving = 100;

	@Parameter(names = { "--stopTimesMode" }, description = ""
			+ "Stop times DAO implementation to use. "
			+ "PACKED: Optimized for memory, but can be slower if stop_times.txt are not sorted by trip ID. "
			+ "UNSORTED: Work best for stop_times.txt unsorted by trip ID, but uses more memory. "
			+ "AUTO: Start in PACKED mode, then switch to UNSORTED mode if required. ")
	private StopTimesDaoMode stopTimesDaoMode = StopTimesDaoMode.AUTO;

	@Parameter(names = { "--shapePointsMode" }, description = ""
			+ "Shape points DAO implementation to use. "
			+ "PACKED: Optimized for memory, but can be slower if shapes.txt are not sorted by shape ID. "
			+ "SIMPLE: Work for all situations, but uses more memory. ")
	private ShapePointsDaoMode shapePointsDaoMode = ShapePointsDaoMode.AUTO;

	@Parameter(names = { "--speedUnit" }, description = ""
			+ "Speed unit to use in outputs. " //
			+ "MPS: Meters per second. " //
			+ "KPH: Kilometers per hour. " //
			+ "MPH: Miles per hour. ")
	private SpeedUnit speedUnit = SpeedUnit.MPS;

	@Parameter(description = "<GTFS file to validate>")
	private String gtfsFile;

	public boolean isHelp() {
		return help;
	}

	public boolean isListValidators() {
		return listValidators;
	}

	@Override
	public boolean isVerbose() {
		return verbose;
	}

	@Override
	public boolean isPrintIssues() {
		return printIssues;
	}

	@Override
	public Optional<String> getConfigFile() {
		return Optional.ofNullable(configFile);
	}

	@Override
	public Optional<NamedDataIO> getHtmlDataIO() throws IOException {
		return htmlReportFile == null || htmlReportFile.isEmpty()
				? Optional.empty()
				: Optional.of(new FileDataIO(htmlReportFile, false));
	}

	@Override
	public Optional<NamedDataIO> getJsonDataIO() throws IOException {
		return jsonReportFile == null || jsonReportFile.isEmpty()
				? Optional.empty()
				: Optional.of(new FileDataIO(jsonReportFile, appendMode));
	}

	@Override
	public int getMaxIssuesPerCategoryLimit() {
		return maxIssuesPerCategoryLimit;
	}

	@Override
	public int getNumThreads() {
		return numThreads;
	}

	@Override
	public int getMaxStopTimeInterleaving() {
		return maxStopTimesInterleaving;
	}

	@Override
	public int getMaxShapePointsInterleaving() {
		return maxShapePointsInterleaving;
	}

	@Override
	public StopTimesDaoMode getStopTimesDaoMode() {
		return stopTimesDaoMode;
	}

	@Override
	public ShapePointsDaoMode getShapePointsDaoMode() {
		return shapePointsDaoMode;
	}

	@Override
	public FormattingOptions getFormattingOptions() {
		return new FormattingOptions(speedUnit);
	}

	@Override
	public String getGtfsFile() {
		return gtfsFile;
	}
}
