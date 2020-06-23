package com.mecatran.gtfsvtor.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.mecatran.gtfsvtor.dao.ShapePointsDao;
import com.mecatran.gtfsvtor.model.GtfsShape;
import com.mecatran.gtfsvtor.model.GtfsShapePoint;

public class PackingUnsortedShapePointsDao implements ShapePointsDao {

	public static class DefaultContext
			implements PackedUnsortedShapePoints.Context {
	}

	private Map<GtfsShape.Id, PackedUnsortedShapePoints> shapePoints = new HashMap<>();
	private DefaultContext context = new DefaultContext();
	private int nShapePoints = 0;
	private boolean verbose = false;
	private boolean closed = false;

	public PackingUnsortedShapePointsDao() {
	}

	public PackingUnsortedShapePointsDao withVerbose(boolean verbose) {
		this.verbose = verbose;
		return this;
	}

	@Override
	public void addShapePoint(GtfsShapePoint shapePoint) {
		if (closed)
			throw new RuntimeException(
					"Cannot re-open a closed PackingUnsortedShapePointsDao. Implement this if needed.");
		nShapePoints++;
		PackedUnsortedShapePoints sp = shapePoints.computeIfAbsent(
				shapePoint.getShapeId(),
				sid -> new PackedUnsortedShapePoints());
		sp.addShapePoint(context, shapePoint);
	}

	@Override
	public int getShapePointsCount() {
		return nShapePoints;
	}

	@Override
	public Stream<GtfsShape.Id> getShapeIds() {
		closeIfNeeded();
		return shapePoints.keySet().stream();
	}

	@Override
	public boolean hasShape(GtfsShape.Id shapeId) {
		closeIfNeeded();
		return shapePoints.containsKey(shapeId);
	}

	@Override
	public Optional<List<GtfsShapePoint>> getPointsOfShape(
			GtfsShape.Id shapeId) {
		closeIfNeeded();
		PackedUnsortedShapePoints pusp = shapePoints.get(shapeId);
		Optional<List<GtfsShapePoint>> shapePoints = pusp == null
				? Optional.empty()
				: Optional.of(pusp.getShapePoints(shapeId, context));
		return shapePoints;
	}

	private void closeIfNeeded() {
		if (closed)
			return;
		shapePoints.values().forEach(st -> st.sort(context));
		if (verbose) {
			long nShapes = shapePoints.size();
			long shapeBytes = nShapes * (8 * 4); // 1 int, 3 pointer
			long dataBytes = shapePoints.entrySet().stream()
					.mapToInt(e -> e.getValue().getDataSize()).sum();
			long totalBytes = shapeBytes + dataBytes;
			System.out.println(String.format(Locale.US,
					"Packed %d points, %d shapes (%dk) in (%dk)", nShapePoints,
					nShapes, shapeBytes / 1024, dataBytes / 1024));
			System.out.println(String.format(Locale.US,
					"Total %dk. Avg bytes: %.2f per shape point, %.2f per shape",
					totalBytes / 1024, totalBytes * 1.0 / nShapePoints,
					totalBytes * 1.0 / nShapes));
		}
		closed = true;
	}
}
