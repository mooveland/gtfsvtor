package com.mecatran.gtfsvtor.model;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class GtfsTransfer
		implements GtfsObject<List<String>>, GtfsObjectWithSourceRef {

	public static final String TABLE_NAME = "transfers.txt";

	private GtfsStop.Id fromStopId, toStopId;
	private GtfsRoute.Id fromRouteId, toRouteId;
	private GtfsTrip.Id fromTripId, toTripId;
	private GtfsTransferType transferType;
	private Integer minTransferTime;

	private long sourceLineNumber;

	public GtfsTransfer.Id getId() {
		return id(fromStopId, toStopId, fromRouteId, toRouteId, fromTripId,
				toTripId);
	}

	@Override
	public DataObjectSourceRef getSourceRef() {
		return new DataObjectSourceRef(TABLE_NAME, sourceLineNumber);
	}

	public GtfsStop.Id getFromStopId() {
		return fromStopId;
	}

	public GtfsStop.Id getToStopId() {
		return toStopId;
	}

	public GtfsRoute.Id getFromRouteId() {
		return fromRouteId;
	}

	public GtfsRoute.Id getToRouteId() {
		return toRouteId;
	}

	public GtfsTrip.Id getFromTripId() {
		return fromTripId;
	}

	public GtfsTrip.Id getToTripId() {
		return toTripId;
	}

	public Optional<GtfsTransferType> getType() {
		return Optional.ofNullable(transferType);
	}

	public GtfsTransferType getNonNullType() {
		return transferType == null ? GtfsTransferType.RECOMMENDED
				: transferType;
	}

	public Integer getMinTransferTime() {
		return minTransferTime;
	}

	@Override
	public String toString() {
		return "Transfer{from=" + fromStopId + ",to=" + toStopId + ",type="
				+ transferType + "}";
	}

	public static Id id(GtfsStop.Id fromStopId, GtfsStop.Id toStopId) {
		return new Id(fromStopId, toStopId, null, null, null, null);
	}

	public static Id id(GtfsStop.Id fromStopId, GtfsStop.Id toStopId,
			GtfsRoute.Id fromRouteId, GtfsRoute.Id toRouteId,
			GtfsTrip.Id fromTripId, GtfsTrip.Id toTripId) {
		if (fromStopId == null && toStopId == null && fromRouteId == null
				&& toRouteId == null && fromTripId == null && toTripId == null)
			return null;
		return new Id(fromStopId, toStopId, fromRouteId, toRouteId, fromTripId,
				toTripId);
	}

	public static class Id extends GtfsCompositeId<String, GtfsTransfer> {

		private Id(GtfsStop.Id fromStopId, GtfsStop.Id toStopId,
				GtfsRoute.Id fromRouteId, GtfsRoute.Id toRouteId,
				GtfsTrip.Id fromTripId, GtfsTrip.Id toTripId) {
			super(Arrays.asList(fromStopId, toStopId, fromRouteId, toRouteId,
					fromTripId, toTripId));
		}
	}

	public static class Builder {
		private GtfsTransfer transfer;

		public Builder() {
			transfer = new GtfsTransfer();
		}

		public Builder withSourceLineNumber(long lineNumber) {
			transfer.sourceLineNumber = lineNumber;
			return this;
		}

		public Builder withFromStopId(GtfsStop.Id fromStopId) {
			transfer.fromStopId = fromStopId;
			return this;
		}

		public Builder withToStopId(GtfsStop.Id toStopId) {
			transfer.toStopId = toStopId;
			return this;
		}

		public Builder withFromRouteId(GtfsRoute.Id fromRouteId) {
			transfer.fromRouteId = fromRouteId;
			return this;
		}

		public Builder withToRouteId(GtfsRoute.Id toRouteId) {
			transfer.toRouteId = toRouteId;
			return this;
		}

		public Builder withFromTripId(GtfsTrip.Id fromTripId) {
			transfer.fromTripId = fromTripId;
			return this;
		}

		public Builder withToTripId(GtfsTrip.Id toTripId) {
			transfer.toTripId = toTripId;
			return this;
		}

		public Builder withTransferType(GtfsTransferType transferType) {
			transfer.transferType = transferType;
			return this;
		}

		public Builder withMinTransferTime(Integer minTransferTime) {
			transfer.minTransferTime = minTransferTime;
			return this;
		}

		public GtfsTransfer build() {
			return transfer;
		}
	}
}
