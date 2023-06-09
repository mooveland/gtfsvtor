package com.mecatran.gtfsvtor.model;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GtfsRoute implements GtfsObject<String>, GtfsObjectWithSourceRef {

	public static final String TABLE_NAME = "routes.txt";

	private GtfsRoute.Id id;
	private GtfsAgency.Id agencyId;
	private GtfsRouteType type;
	private String shortName;
	private String longName;
	private String description;
	private String url;
	private GtfsColor color;
	private GtfsColor textColor;
	private Integer sortOrder;
	private GtfsNetwork.Id networkId;

	private long sourceLineNumber;

	public GtfsRoute.Id getId() {
		return id;
	}

	@Override
	public DataObjectSourceRef getSourceRef() {
		return new DataObjectSourceRef(TABLE_NAME, sourceLineNumber);
	}

	public GtfsAgency.Id getAgencyId() {
		return agencyId;
	}

	public GtfsRouteType getType() {
		return type;
	}

	public String getShortName() {
		return shortName;
	}

	public String getLongName() {
		return longName;
	}

	public String getDescription() {
		return description;
	}

	public String getUrl() {
		return url;
	}

	public Optional<GtfsColor> getColor() {
		return Optional.ofNullable(color);
	}

	public GtfsColor getNonNullColor() {
		return color == null ? GtfsColor.WHITE : color;
	}

	public Optional<GtfsColor> getTextColor() {
		return Optional.ofNullable(textColor);
	}

	public GtfsColor getNonNullTextColor() {
		return textColor == null ? GtfsColor.BLACK : textColor;
	}

	public Integer getSortOrder() {
		return sortOrder;
	}

	public Optional<GtfsNetwork.Id> getNetworkId() {
		return Optional.ofNullable(networkId);
	}

	@Override
	public String toString() {
		return "Route{id=" + id + ",type=" + type + ",shortName='" + shortName
				+ "',longName='" + longName + "'}";
	}

	public static Id id(String id) {
		return id == null || id.isEmpty() ? null : Id.build(id);
	}

	public static class Id extends GtfsAbstractId<String, GtfsRoute> {

		private Id(String id) {
			super(id);
		}

		private static ConcurrentMap<String, Id> CACHE = new ConcurrentHashMap<>();

		private static Id build(String id) {
			return CACHE.computeIfAbsent(id, Id::new);
		}

		@Override
		public boolean equals(Object obj) {
			return super.doEquals(obj, GtfsRoute.Id.class);
		}
	}

	public static class Builder {
		private GtfsRoute route;

		public Builder(String id) {
			route = new GtfsRoute();
			route.id = id(id);
		}

		public Builder withSourceLineNumber(long lineNumber) {
			route.sourceLineNumber = lineNumber;
			return this;
		}

		public Builder withAgencyId(GtfsAgency.Id agencyId) {
			route.agencyId = agencyId;
			return this;
		}

		public Builder withType(GtfsRouteType type) {
			route.type = type;
			return this;
		}

		public Builder withShortName(String shortName) {
			route.shortName = shortName;
			return this;
		}

		public Builder withLongName(String longName) {
			route.longName = longName;
			return this;
		}

		public Builder withDescription(String description) {
			route.description = description;
			return this;
		}

		public Builder withUrl(String url) {
			route.url = url;
			return this;
		}

		public Builder withColor(GtfsColor color) {
			route.color = color;
			return this;
		}

		public Builder withTextColor(GtfsColor textColor) {
			route.textColor = textColor;
			return this;
		}

		public Builder withSortOrder(Integer sortOrder) {
			route.sortOrder = sortOrder;
			return this;
		}

		public Builder withNetworkId(GtfsNetwork.Id networkId) {
			route.networkId = networkId;
			return this;
		}

		public GtfsRoute build() {
			return route;
		}
	}
}
