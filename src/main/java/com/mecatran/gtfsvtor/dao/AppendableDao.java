package com.mecatran.gtfsvtor.dao;

import com.mecatran.gtfsvtor.loader.DataLoader.SourceContext;
import com.mecatran.gtfsvtor.model.GtfsAgency;
import com.mecatran.gtfsvtor.model.GtfsCalendar;
import com.mecatran.gtfsvtor.model.GtfsCalendarDate;
import com.mecatran.gtfsvtor.model.GtfsFareAttribute;
import com.mecatran.gtfsvtor.model.GtfsFareRule;
import com.mecatran.gtfsvtor.model.GtfsFeedInfo;
import com.mecatran.gtfsvtor.model.GtfsFrequency;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.model.GtfsShapePoint;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTransfer;
import com.mecatran.gtfsvtor.model.GtfsTrip;

public interface AppendableDao {

	public void setFeedInfo(GtfsFeedInfo feedInfo, SourceContext info);

	public void addAgency(GtfsAgency agency, SourceContext info);

	public void addRoute(GtfsRoute route, SourceContext info);

	public void addStop(GtfsStop stop, SourceContext info);

	public void addCalendar(GtfsCalendar calendar, SourceContext info);

	public void addCalendarDate(GtfsCalendarDate calendarDate,
			SourceContext info);

	public void addShapePoint(GtfsShapePoint shapePoint, SourceContext info);

	public void addTrip(GtfsTrip trip, SourceContext info);

	public void addStopTime(GtfsStopTime stopTime, SourceContext info);

	public void addFrequency(GtfsFrequency frequency, SourceContext info);

	public void addTransfer(GtfsTransfer transfer, SourceContext info);

	public void addFareAttribute(GtfsFareAttribute fareAttribute,
			SourceContext info);

	public void addFareRule(GtfsFareRule fareRule, SourceContext info);

	public void close();
}
