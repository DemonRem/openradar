package de.knewcleus.fgfs.navdata.impl;

import java.awt.geom.Point2D;

import de.knewcleus.fgfs.Units;
import de.knewcleus.fgfs.navdata.model.IFrequency;
import de.knewcleus.fgfs.navdata.model.INDB;

public class NDB implements INDB {
	protected final Point2D geographicPosition;
	protected final float elevation;
	protected final String identification;
	protected final String name;
	protected final IFrequency frequency;
	protected final float range;

	public NDB(Point2D geographicPosition, float elevation,
			String identification, String name, IFrequency frequency,
			float range) {
		this.geographicPosition = geographicPosition;
		this.elevation = elevation;
		this.identification = identification;
		this.name = name;
		this.frequency = frequency;
		this.range = range;
	}

	@Override
	public Point2D getGeographicPosition() {
		return geographicPosition;
	}
	
	@Override
	public float getElevation() {
		return elevation;
	}
	
	@Override
	public String getIdentification() {
		return identification;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public IFrequency getFrequency() {
		return frequency;
	}
	
	@Override
	public float getRange() {
		return range;
	}
	
	@Override
	public String toString() {
		return String.format("NDB %3s %+10.6f %+11.6f elev %4fft freq %s range %3fNM name %s",
				identification,
				geographicPosition.getY(),
				geographicPosition.getX(),
				elevation / Units.FT,
				frequency.toString(),
				range / Units.NM,
				name);
	}
}
