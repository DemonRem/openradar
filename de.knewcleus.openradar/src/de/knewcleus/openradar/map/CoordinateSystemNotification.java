package de.knewcleus.openradar.map;

import de.knewcleus.openradar.notify.INotification;
import de.knewcleus.openradar.notify.INotifier;

/**
 * A coordinate system notification is sent whenever the coordinate system of a viewer changes,
 * so that views can update their extents.
 * 
 * @author Ralf Gerlich
 *
 */
public class CoordinateSystemNotification implements INotification {
	protected final IViewAdapter source;

	public CoordinateSystemNotification(IViewAdapter source) {
		this.source = source;
	}

	@Override
	public INotifier getSource() {
		return source;
	}
}
