package com.mecatran.gtfsvtor.lib;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mecatran.gtfsvtor.dao.AppendableDao;
import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.dao.inmemory.InMemoryDao;
import com.mecatran.gtfsvtor.lib.GtfsVtorOptions.NamedDataIO;
import com.mecatran.gtfsvtor.loader.NamedInputStreamSource;
import com.mecatran.gtfsvtor.loader.NamedTabularDataSource;
import com.mecatran.gtfsvtor.loader.impl.CsvDataSource;
import com.mecatran.gtfsvtor.loader.impl.DefaultDataLoaderContext;
import com.mecatran.gtfsvtor.loader.impl.GtfsDataLoader;
import com.mecatran.gtfsvtor.loader.impl.SourceInfoDataReloader;
import com.mecatran.gtfsvtor.loader.schema.DefaultGtfsTableSchema;
import com.mecatran.gtfsvtor.reporting.ReportFormatter;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.ReviewReport;
import com.mecatran.gtfsvtor.reporting.html.HtmlReportFormatter;
import com.mecatran.gtfsvtor.reporting.impl.InMemoryReportLog;
import com.mecatran.gtfsvtor.reporting.json.JsonReportFormatter;
import com.mecatran.gtfsvtor.validation.DaoValidator;
import com.mecatran.gtfsvtor.validation.DefaultDaoValidator;
import com.mecatran.gtfsvtor.validation.DefaultStreamingValidator;
import com.mecatran.gtfsvtor.validation.DefaultTripTimesValidator;
import com.mecatran.gtfsvtor.validation.impl.DefaultDaoValidatorContext;
import com.mecatran.gtfsvtor.validation.impl.DefaultValidatorConfig;

public class GtfsVtor {

	private GtfsVtorOptions options;
	private ReviewReport reviewReport;
	private ReportSink reportSink;
	private AppendableDao woDao;
	private IndexedReadOnlyDao roDao;

	public GtfsVtor(GtfsVtorOptions options) {
		this.options = options;
	}

	public void validate() throws IOException {

		// TODO Properly configure all this

		// Load configuration
		DefaultValidatorConfig config = new DefaultValidatorConfig();
		if (options.getConfigFile().isPresent()) {
			File propFile = new File(options.getConfigFile().get());
			if (propFile.exists() && propFile.canRead()) {
				if (options.isVerbose()) {
					System.out.println(
							"Loading config from " + propFile.getName());
				}
				config.loadProperties(propFile);
			} else {
				System.err.println("Cannot load " + propFile.getName());
			}
		}
		// TODO Add remaining cmd line args to config

		// Create report log
		InMemoryReportLog imReport = new InMemoryReportLog()
				.withMaxIssuesPerCategory(
						options.getMaxIssuesPerCategoryLimit())
				.withPrintIssues(options.isPrintIssues())
				.withFormattingOptions(options.getFormattingOptions());
		this.reportSink = imReport;
		this.reviewReport = imReport;

		// Create source
		NamedInputStreamSource inputStreamSource = NamedInputStreamSource
				.autoGuess(options.getGtfsFile(), reportSink);
		if (inputStreamSource != null) {
			NamedTabularDataSource dataSource = new CsvDataSource(
					inputStreamSource);

			// Register dataSource as source info factory
			imReport.withSourceInfoFactory(
					new SourceInfoDataReloader(dataSource)
							.withVerbose(options.isVerbose()));

			// Create the DAO
			InMemoryDao imDao = new InMemoryDao(options.getStopTimesDaoMode(),
					options.getMaxStopTimeInterleaving(),
					options.getShapePointsDaoMode(),
					options.getMaxShapePointsInterleaving())
							.withVerbose(options.isVerbose());
			this.woDao = imDao;
			this.roDao = imDao;

			// Load data, stream-validate along the way
			DefaultStreamingValidator defStreamingValidator = new DefaultStreamingValidator(
					config);
			DefaultGtfsTableSchema tableSchema = new DefaultGtfsTableSchema();
			GtfsDataLoader loader = new GtfsDataLoader(dataSource, tableSchema);

			long start = System.currentTimeMillis();
			loader.load(new DefaultDataLoaderContext(woDao, roDao, reportSink,
					defStreamingValidator));
			long end = System.currentTimeMillis();
			System.gc();
			if (options.isVerbose()) {
				Runtime runtime = Runtime.getRuntime();
				System.out.println("Loaded '" + options.getGtfsFile() + "' in "
						+ (end - start) + "ms. Used memory: ~"
						+ (runtime.totalMemory() - runtime.freeMemory())
								/ (1024 * 1024)
						+ "Mb");
			}

			// Dao and trip time validate
			DaoValidator.Context context = new DefaultDaoValidatorContext(imDao,
					imReport, config);
			DefaultDaoValidator daoValidator = new DefaultDaoValidator(config)
					.withVerbose(options.isVerbose())
					.withNumThreads(options.getNumThreads());
			daoValidator.validate(context);
			DefaultTripTimesValidator tripTimesValidator = new DefaultTripTimesValidator(
					config).withVerbose(options.isVerbose());
			tripTimesValidator.scanValidate(context);
		}

		// Generate report
		for (ReportFormatter reportFormatter : buildReportFormatters()) {
			reportFormatter.format(reviewReport);
		}
	}

	private List<ReportFormatter> buildReportFormatters() throws IOException {
		List<ReportFormatter> formatters = new ArrayList<>();

		// HTML format
		Optional<NamedDataIO> htmlDataIO = options.getHtmlDataIO();
		if (htmlDataIO.isPresent()) {
			ReportFormatter htmlFormatter = new HtmlReportFormatter(
					htmlDataIO.get(), options.getFormattingOptions());
			formatters.add(htmlFormatter);
		}

		// JSON format
		Optional<NamedDataIO> jsonDataIO = options.getJsonDataIO();
		if (jsonDataIO.isPresent()) {
			ReportFormatter jsonFormatter = new JsonReportFormatter(
					jsonDataIO.get()).withInputFileName(options.getGtfsFile());
			formatters.add(jsonFormatter);
		}

		return formatters;
	}

	public ReviewReport getReviewReport() {
		return reviewReport;
	}

	public IndexedReadOnlyDao getDao() {
		return roDao;
	}
}
