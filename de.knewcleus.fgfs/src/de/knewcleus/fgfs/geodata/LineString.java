package de.knewcleus.fgfs.geodata;

import java.util.List;

public class LineString extends GeometryContainer<Point> {
	public List<Point> getPoints() {
		return getContainedGeometry();
	}
}