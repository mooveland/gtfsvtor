package com.mecatran.gtfsvtor.reporting.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

import com.googlecode.jatl.Html;
import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
import com.mecatran.gtfsvtor.reporting.ReportFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.ReviewReport;
import com.mecatran.gtfsvtor.reporting.impl.ClassifiedReviewReport.CategoryCounter;
import com.mecatran.gtfsvtor.reporting.impl.ClassifiedReviewReport.IssuesCategory;
import com.mecatran.gtfsvtor.reporting.impl.ClassifiedReviewReport.IssuesSubCategory;

public class HtmlReportFormatter implements ReportFormatter {

	private OutputStream outputStream;
	private Writer writer;
	private Html html;
	private int maxIssuesPerCategory;

	public HtmlReportFormatter(OutputStream outputStream,
			int maxIssuesPerCategory) {
		this.outputStream = outputStream;
		this.maxIssuesPerCategory = maxIssuesPerCategory;
	}

	@Override
	public void format(ReviewReport report) throws IOException {
		writer = new OutputStreamWriter(outputStream);
		html = new Html(writer);
		ClassifiedReviewReport clsReport = new ClassifiedReviewReport(report,
				maxIssuesPerCategory);
		formatHeader();
		formatSummary(clsReport);
		for (IssuesCategory category : clsReport.getCategories()) {
			formatCategory(report, category);
		}
		formatFooter();
		writer.close();
		outputStream.close();
	}

	private void formatCategory(ReviewReport report, IssuesCategory category)
			throws IOException {
		html.h2();
		html.text(category.getCategoryName());
		for (CategoryCounter cc : category.getSeverityCounters()) {
			html.span().classAttr("smaller");
			html.text(" - " + cc.getTotalCount());
			html.span().classAttr("badge " + cc.getSeverity().toString())
					.text(cc.getCategoryName()).end();
			html.end(); // span
		}
		html.end(); // h2;

		List<CategoryCounter> ccList = category.getCategoryCountersToDisplay();
		if (!ccList.isEmpty()) {
			html.ul();
			for (CategoryCounter cc : ccList) {
				html.li();
				html.text("" + cc.getTotalCount());
				html.span()
						.classAttr(
								"smaller badge " + cc.getSeverity().toString())
						.text(cc.getSeverity().toString()).end();
				html.text(cc.getCategoryName());
				if (cc.isTruncated()) {
					html.span().classAttr("comments")
							.text(" (of which only the first "
									+ cc.getDisplayedCount()
									+ " are displayed)")
							.end();
				}
				html.end(); // li
			}
			html.end(); // ul
		}
		for (IssuesSubCategory subCategory : category.getSubCategories()) {
			formatSubCategory(report, subCategory);
		}
	}

	private void formatSubCategory(ReviewReport report,
			IssuesSubCategory subCategory) throws IOException {
		html.div().classAttr("subcategory");
		if (!subCategory.getSourceRefs().isEmpty()) {
			formatSourceInfos(report, subCategory);
		}
		formatIssueList(subCategory.getIssues());
		html.end(); // div.subcategory
	}

	private void formatSourceInfos(ReviewReport report,
			IssuesSubCategory subCategory) throws IOException {
		String lastTableName = null;
		boolean inTable = false;
		for (int sourceRefIndex = 0; sourceRefIndex < subCategory
				.getSourceRefs().size(); sourceRefIndex++) {
			DataObjectSourceInfo sourceInfo = report.getSourceInfo(
					subCategory.getSourceRefs().get(sourceRefIndex));
			String tableName = sourceInfo.getTable().getTableName();
			if (!tableName.equals(lastTableName)) {
				// Output table header
				if (inTable)
					html.end(); // table
				inTable = true;
				html.table().classAttr("sourceinfo");
				if (lastTableName != null) {
					html.caption().text(tableName).end();
				}
				html.thead().tr();
				html.td().classAttr("linenr")
						.text(sourceInfo.getLineNumber() == 1 ? "L1" : "")
						.end();
				for (String header : sourceInfo.getTable().getHeaderColumns()) {
					html.td();
					if (sourceInfo.getLineNumber() == 1) {
						ReportIssueSeverity fieldSeverity = subCategory
								.getFieldSeverity(header);
						if (fieldSeverity != null)
							html.classAttr(fieldSeverity.toString());
					}
					html.text(header);
					html.end(); // td
				}
				html.end().end(); // thead/tr
			}
			lastTableName = tableName;
			if (sourceInfo.getLineNumber() != 1) {
				html.tr();
				html.td().classAttr("linenr")
						.text("L" + sourceInfo.getLineNumber()).end();
				List<String> headers = sourceInfo.getTable().getHeaderColumns();
				List<String> fields = sourceInfo.getFields();
				for (int i = 0; i < fields.size(); i++) {
					String fieldValue = fields.get(i);
					String header = i < headers.size() ? headers.get(i) : "-";
					html.td();
					ReportIssueSeverity fieldSeverity = subCategory
							.getFieldSeverity(sourceRefIndex, header);
					if (fieldSeverity != null)
						html.classAttr(fieldSeverity.toString());
					html.text(fieldValue);
					html.end(); // td
				}
				html.end(); // tr
			}
		}
		if (inTable)
			html.end(); // table
	}

	private void formatIssueList(List<ReportIssue> issues) throws IOException {
		html.table().classAttr("issuelist");
		for (ReportIssue issue : issues) {
			ReportIssueSeverity severity = issue.getSeverity();
			html.tr();
			html.td().classAttr("severity " + severity.toString())
					.text(severity.toString()).end();
			List<String> fieldNames = issue.getSourceRefs().stream()
					.flatMap(si -> si.getFieldNames().stream()).sorted()
					.distinct().collect(Collectors.toList());
			html.td().classAttr("fieldname").text(String.join(", ", fieldNames))
					.end();
			html.td().classAttr("issue").raw(HtmlIssueFormatter.format(issue))
					.end();
			html.end();// tr
		}
		html.end(); // table
	}

	private void formatHeader() throws IOException {
		writer.write("<!DOCTYPE html>\n");
		html.html().lang("en").head();
		html.meta().charset("UTF-8").end();
		html.start("title").text("GTFS validation report").end();
		html.style().type("text/css");
		StringWriter cssWriter = new StringWriter();
		cssWriter.append("\n");
		IOUtils.copy(this.getClass().getResourceAsStream("report.css"),
				cssWriter, StandardCharsets.UTF_8);
		StringWriter logoWriter = new StringWriter();
		IOUtils.copy(this.getClass().getResourceAsStream("gtfsvtor_logo.svg"),
				logoWriter, StandardCharsets.UTF_8);
		logoWriter.close();
		cssWriter.append(
				"\n.logo {\n\tbackground-image: url('data:image/svg+xml;base64,");
		String logoBase64 = Base64.getEncoder().encodeToString(
				logoWriter.toString().getBytes(StandardCharsets.UTF_8));
		cssWriter.append(logoBase64);
		cssWriter.append("');\n}\n");
		cssWriter.close();
		html.raw(cssWriter.toString());
		html.end(); // style
	}

	private void formatSummary(ClassifiedReviewReport clsReport)
			throws IOException {
		html.h1().classAttr("logo image").text("GTFS validation report");
		for (CategoryCounter cc : clsReport.getSeverityCounters()) {
			html.span().classAttr("xsmaller");
			html.text(" - " + cc.getTotalCount() + " ");
			html.span().classAttr("badge " + cc.getSeverity().toString())
					.text(cc.getCategoryName()).end();
			html.end();
		}
		html.end(); // h1

		html.ul();
		for (CategoryCounter cc : clsReport.getCategoryCounters()) {
			html.li();
			html.text(cc.getTotalCount() + " ");
			html.span()
					.classAttr("smaller badge " + cc.getSeverity().toString())
					.text(cc.getSeverity().toString()).end();
			html.text(cc.getCategoryName());
			if (cc.isTruncated()) {
				html.span().classAttr("comments").text(" (of which "
						+ cc.getDisplayedCount() + " are displayed)").end();
			}
			html.end(); // li
		}
		html.end(); // ul
	}

	private void formatFooter() throws IOException {
		html.hr();
		Date now = new Date();
		Calendar cal = GregorianCalendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		html.p().classAttr("comments").text(String.format(
				"Validation done at %s by GTFSVTOR - Copyright (c) %d Mecatran",
				now, year)).end();
		html.end(); // html
	}

}
