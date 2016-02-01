package de.knewcleus.openradar.gui.flightstrips.rules;

import java.util.ArrayList;

import org.jdom2.Element;

import de.knewcleus.openradar.gui.flightplan.FlightPlanData;
import de.knewcleus.openradar.gui.flightstrips.FlightStrip;
import de.knewcleus.openradar.gui.flightstrips.LogicManager;

public class DestinationRule extends AbstractRule {

	private final String otherAirport; // if empty: any other airport
	
	public DestinationRule(String otherAirport) {
		this.otherAirport = otherAirport;
	}
	
	public DestinationRule(Element element, LogicManager logic) {
		this.otherAirport = element.getAttributeValue("otherairport");
	}
	
	@Override
	public boolean isAppropriate(FlightStrip flightstrip) {
		FlightPlanData flightplan = flightstrip.getContact().getFlightPlan();
		return (flightplan != null) && (((otherAirport.length() <= 0) && !flightplan.contactWillLandHere()) || flightplan.getDestinationAirport().equalsIgnoreCase(otherAirport));
	}

	@Override
	public ArrayList<String> getRuleText() {
		ArrayList<String> result = new ArrayList<String>();
		result.add("contact's destination airport is " + ((otherAirport.length() > 0) ? otherAirport : "any other airport") + ".");
		return result;
	}

	// --- IDomElement ---
	
	@Override
	public void putAttributes(Element element) {
		super.putAttributes(element);
		element.setAttribute("otherairport", otherAirport);
	}

}