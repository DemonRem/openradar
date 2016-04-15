package de.knewcleus.openradar.gui.flightstrips.conditions;

import org.jdom2.Element;

import de.knewcleus.openradar.gui.flightplan.FlightPlanData;
import de.knewcleus.openradar.gui.flightstrips.FlightStrip;

public class AtcOtherCondition extends AbstractStringCondition {

	// --- constructors ---
	
	public AtcOtherCondition(String OtherAtc, boolean isOtherAtc) {
		super(OtherAtc, isOtherAtc);
	}
	
	public AtcOtherCondition(Element element) {
		super(element);
	}
	
	// --- compare ---
	
	@Override
	protected String extractStringValue(FlightStrip flightstrip) {
		FlightPlanData flightplan = flightstrip.getContact().getFlightPlan();
		if (flightplan == null) return null;
		return flightplan.getOwner();
	}

	// --- IDomElement ---
	
	@Override
	protected String getStringAttribute() {
		return "other_atc";
	}

	@Override
	protected String getBooleanAttribute() {
		return "is_other_atc";
	}

	// --- IRuleTextProvider ---
	
	@Override
	public String getPrefixText() {
		return "contact is controlled by ";
	}
	
}