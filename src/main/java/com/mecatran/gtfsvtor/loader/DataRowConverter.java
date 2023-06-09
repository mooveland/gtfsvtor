package com.mecatran.gtfsvtor.loader;

import java.text.ParseException;
import java.time.ZoneId;
import java.util.Currency;
import java.util.Locale;
import java.util.TimeZone;

import com.mecatran.gtfsvtor.model.GtfsBikeAccess;
import com.mecatran.gtfsvtor.model.GtfsBlockId;
import com.mecatran.gtfsvtor.model.GtfsCalendarDateExceptionType;
import com.mecatran.gtfsvtor.model.GtfsColor;
import com.mecatran.gtfsvtor.model.GtfsDirectionality;
import com.mecatran.gtfsvtor.model.GtfsDropoffType;
import com.mecatran.gtfsvtor.model.GtfsExactTime;
import com.mecatran.gtfsvtor.model.GtfsFareDurationLimitType;
import com.mecatran.gtfsvtor.model.GtfsFareTransferType;
import com.mecatran.gtfsvtor.model.GtfsLogicalDate;
import com.mecatran.gtfsvtor.model.GtfsLogicalTime;
import com.mecatran.gtfsvtor.model.GtfsNumTransfers;
import com.mecatran.gtfsvtor.model.GtfsPathwayMode;
import com.mecatran.gtfsvtor.model.GtfsPaymentMethod;
import com.mecatran.gtfsvtor.model.GtfsPickupType;
import com.mecatran.gtfsvtor.model.GtfsShapePointSequence;
import com.mecatran.gtfsvtor.model.GtfsStopType;
import com.mecatran.gtfsvtor.model.GtfsTimepoint;
import com.mecatran.gtfsvtor.model.GtfsTransferType;
import com.mecatran.gtfsvtor.model.GtfsTranslationTable;
import com.mecatran.gtfsvtor.model.GtfsTripDirectionId;
import com.mecatran.gtfsvtor.model.GtfsTripStopSequence;
import com.mecatran.gtfsvtor.model.GtfsWheelchairAccess;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.InvalidEncodingError;
import com.mecatran.gtfsvtor.reporting.issues.InvalidFieldFormatError;
import com.mecatran.gtfsvtor.reporting.issues.MissingMandatoryValueError;

public class DataRowConverter {

	public enum Requiredness {
		OPTIONAL, MANDATORY
	}

	private DataRow row;
	private ReportSink reportSink;

	public DataRowConverter(DataRow row, ReportSink reportSink) {
		this.row = row;
		this.reportSink = reportSink;
	}

	public String getString(String field) {
		return getString(field, null, Requiredness.OPTIONAL);
	}

	public String getString(String field, Requiredness requiredness) {
		return getString(field, null, requiredness);
	}

	public String getString(String field, String defaultValue,
			Requiredness requiredness) {
		String ret = row.getString(field);
		if (ret != null) {
			// ret.contains("\uFFFD")
			if (ret.chars().anyMatch(c -> c == 0xFFFD || c == 0)) {
				reportSink.report(new InvalidEncodingError(row.getSourceRef(),
						field, ret), row.getSourceInfo());
			}
			// Return the string anyway
		}
		if (ret == null || ret.isEmpty()) {
			if (requiredness == Requiredness.MANDATORY) {
				reportSink.report(new MissingMandatoryValueError(
						row.getSourceRef(), field), row.getSourceInfo());
			}
			return defaultValue;
		}
		return ret;
	}

	public Integer getInteger(String field, Requiredness requiredness) {
		return getTypeFromString(Integer.class, field, requiredness, "integer",
				Integer::parseInt);
	}

	public Double getDouble(String field, Requiredness requiredness) {
		return this.getDouble(field, null, null, requiredness);
	}

	public Double getDouble(String field, Double defaultValue,
			Double defaultValueIfInvalid, Requiredness requiredness) {
		String value = getString(field);
		if (value == null || value.isEmpty()) {
			if (requiredness == Requiredness.MANDATORY) {
				reportSink.report(new MissingMandatoryValueError(
						row.getSourceRef(), field), row.getSourceInfo());
			}
			return defaultValue;
		}
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			reportSink.report(
					fieldFormatError(field, value, "floating-point (double)"),
					row.getSourceInfo());
			return defaultValueIfInvalid;
		}
	}

	public Boolean getBoolean(String field) {
		return this.getBoolean(field, Requiredness.OPTIONAL);
	}

	public Boolean getBoolean(String field, Requiredness requiredness) {
		return getTypeFromString(Boolean.class, field, requiredness,
				"boolean (0 or 1)", str -> {
					if (str.equals("0"))
						return false;
					else if (str.equals("1"))
						return true;
					else
						throw new ParseException(str, 0);
				});
	}

	public TimeZone getTimeZone(String field) {
		return getTimeZone(field, Requiredness.OPTIONAL);
	}

	public TimeZone getTimeZone(String field, Requiredness requiredness) {
		return getTypeFromString(TimeZone.class, field, requiredness,
				"IANA timezone", tz -> TimeZone.getTimeZone(ZoneId.of(tz)));
	}

	public Locale getLocale(String field, Requiredness requiredness) {
		return getTypeFromString(Locale.class, field, requiredness,
				"ISO 639-1 language code", Locale::forLanguageTag);
	}

	public GtfsLogicalDate getLogicalDate(String field,
			Requiredness requiredness) {
		return getTypeFromString(GtfsLogicalDate.class, field, requiredness,
				"date (YYYYMMDD)", GtfsLogicalDate::parseFromYYYYMMDD);
	}

	public GtfsLogicalTime getLogicalTime(String field,
			Requiredness requiredness) {
		return getTypeFromString(GtfsLogicalTime.class, field, requiredness,
				"time (HH:MM:SS)", GtfsLogicalTime::parseFromHH_MM_SS);
	}

	public GtfsCalendarDateExceptionType getCalendarDateExceptionType(
			String field) {
		return getTypeFromInteger(GtfsCalendarDateExceptionType.class, field,
				Requiredness.MANDATORY, "exception type (1 or 2)",
				GtfsCalendarDateExceptionType::fromValue);
	}

	public GtfsStopType getStopType(String field) {
		return getTypeFromInteger(GtfsStopType.class, field,
				Requiredness.OPTIONAL, "stop type (0, 1, 2, 3 or 4)",
				GtfsStopType::fromValue);
	}

	public GtfsBlockId getBlockId(String field) {
		return GtfsBlockId.fromValue(getString(field));
	}

	public GtfsTripDirectionId getDirectionId(String field) {
		return getTypeFromInteger(GtfsTripDirectionId.class, field,
				Requiredness.OPTIONAL, "direction ID (0 or 1)",
				GtfsTripDirectionId::fromValue);
	}

	public GtfsTripStopSequence getTripStopSequence(String field) {
		return getTypeFromInteger(GtfsTripStopSequence.class, field,
				Requiredness.OPTIONAL, "stop sequence (integer)",
				GtfsTripStopSequence::fromSequence);
	}

	public GtfsShapePointSequence getShapePointSequence(String field) {
		return getTypeFromInteger(GtfsShapePointSequence.class, field,
				Requiredness.OPTIONAL, "point sequence (integer)",
				GtfsShapePointSequence::fromSequence);
	}

	public GtfsPickupType getPickupType(String field) {
		return getTypeFromInteger(GtfsPickupType.class, field,
				Requiredness.OPTIONAL, "pickup type (0, 1, 2 or 3)",
				GtfsPickupType::fromValue);
	}

	public GtfsDropoffType getDropoffType(String field) {
		return getTypeFromInteger(GtfsDropoffType.class, field,
				Requiredness.OPTIONAL, "drop-off type (0, 1, 2 or 3)",
				GtfsDropoffType::fromValue);
	}

	public GtfsTimepoint getTimepoint(String field) {
		return getTypeFromInteger(GtfsTimepoint.class, field,
				Requiredness.OPTIONAL, "timepoint (0 or 1)",
				GtfsTimepoint::fromValue);
	}

	public GtfsColor getColor(String field) {
		return getTypeFromString(GtfsColor.class, field, Requiredness.OPTIONAL,
				"hexadecimal RGB color triplet (6 characters)",
				GtfsColor::parseHexTriplet);
	}

	public GtfsWheelchairAccess getWheelchairAccess(String field) {
		return getTypeFromInteger(GtfsWheelchairAccess.class, field,
				Requiredness.OPTIONAL, "wheelchair access (0, 1, 2)",
				GtfsWheelchairAccess::fromValue);
	}

	public GtfsBikeAccess getBikeAccess(String field) {
		return getTypeFromInteger(GtfsBikeAccess.class, field,
				Requiredness.OPTIONAL, "bike access (0, 1, 2)",
				GtfsBikeAccess::fromValue);
	}

	public GtfsExactTime getExactTimes(String field) {
		return getTypeFromInteger(GtfsExactTime.class, field,
				Requiredness.OPTIONAL, "exact times (0, 1)",
				GtfsExactTime::fromValue);
	}

	public GtfsPaymentMethod getPaymentMethod(String field) {
		return getTypeFromInteger(GtfsPaymentMethod.class, field,
				Requiredness.MANDATORY, "payment method (0, 1)",
				GtfsPaymentMethod::fromValue);
	}

	public GtfsNumTransfers getNumTransfers(String field) {
		/*
		 * This field is specified as "mandatory", but with an empty (ie
		 * missing) value "allowed" (meaning unlimited). So this is not really
		 * mandatory after all, it is optional with a default value if missing.
		 * Here we return null if empty (unlimited): this is up to the holding
		 * class to return a default value.
		 */
		return getTypeFromInteger(GtfsNumTransfers.class, field,
				Requiredness.OPTIONAL, "num transfers (0, 1, 2, empty)",
				GtfsNumTransfers::fromValue);
	}

	public GtfsTransferType getTransferType(String field) {
		return getTypeFromInteger(GtfsTransferType.class, field,
				Requiredness.MANDATORY, "transfer type (0, 1, 2, 3)",
				GtfsTransferType::fromValue);
	}

	public Currency getCurrency(String field) {
		return getTypeFromString(Currency.class, field, Requiredness.MANDATORY,
				"ISO 4217 currency", Currency::getInstance);
	}

	public GtfsPathwayMode getPathwayMode(String field) {
		return getTypeFromInteger(GtfsPathwayMode.class, field,
				Requiredness.MANDATORY, "pathway mode (1-7)",
				GtfsPathwayMode::fromValue);
	}

	public GtfsDirectionality getDirectionality(String field) {
		return getTypeFromInteger(GtfsDirectionality.class, field,
				Requiredness.MANDATORY, "directionality (0, 1)",
				GtfsDirectionality::fromValue);
	}

	public GtfsTranslationTable getTranslationTable(String field) {
		return getTypeFromString(GtfsTranslationTable.class, field,
				Requiredness.MANDATORY, "Table name (w/o .txt)",
				GtfsTranslationTable::fromValue);
	}

	public GtfsFareDurationLimitType getFareDurationLimit(String field) {
		return getTypeFromInteger(GtfsFareDurationLimitType.class, field,
				Requiredness.OPTIONAL, "duration limit type (0, 1, 2, 3)",
				GtfsFareDurationLimitType::fromValue);
	}

	public GtfsFareTransferType getFareTransferType(String field) {
		return getTypeFromInteger(GtfsFareTransferType.class, field,
				Requiredness.MANDATORY, "fare transfer type (0, 1, 2)",
				GtfsFareTransferType::fromValue);
	}

	@FunctionalInterface
	public interface DataConverter<T, R> {
		R convert(T t) throws Exception;
	}

	private <T> T getTypeFromInteger(Class<T> clazz, String field,
			Requiredness requiredness, String expectedFormat,
			DataConverter<Integer, T> func) {
		return getTypeFromString(clazz, field, requiredness, expectedFormat,
				str -> func.convert(Integer.parseInt(str)));
	}

	private <T> T getTypeFromString(Class<T> clazz, String field,
			Requiredness requiredness, String expectedFormat,
			DataConverter<String, T> func) {
		String str = getString(field);
		if (str == null || str.isEmpty()) {
			if (requiredness == Requiredness.MANDATORY) {
				reportSink.report(new MissingMandatoryValueError(
						row.getSourceRef(), field), row.getSourceInfo());
			}
			return null;
		}
		try {
			return func.convert(str);
		} catch (Exception e) {
			reportSink.report(fieldFormatError(field, str, expectedFormat),
					row.getSourceInfo());
			return null;
		}
	}

	private InvalidFieldFormatError fieldFormatError(String field, String value,
			String expectedFormat) {
		return fieldFormatError(field, value, expectedFormat, null);
	}

	private InvalidFieldFormatError fieldFormatError(String field, String value,
			String expectedFormat, String additionalInfo) {
		return new InvalidFieldFormatError(row.getSourceRef(), field, value,
				expectedFormat, additionalInfo);
	}
}
