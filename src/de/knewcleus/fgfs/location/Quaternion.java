/**
 * Copyright (C) 2008-2009 Ralf Gerlich 
 * 
 * This file is part of OpenRadar.
 * 
 * OpenRadar is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * OpenRadar is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * OpenRadar. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Diese Datei ist Teil von OpenRadar.
 * 
 * OpenRadar ist Freie Software: Sie können es unter den Bedingungen der GNU
 * General Public License, wie von der Free Software Foundation, Version 3 der
 * Lizenz oder (nach Ihrer Option) jeder späteren veröffentlichten Version,
 * weiterverbreiten und/oder modifizieren.
 * 
 * OpenRadar wird in der Hoffnung, dass es nützlich sein wird, aber OHNE JEDE
 * GEWÄHRLEISTUNG, bereitgestellt; sogar ohne die implizite Gewährleistung der
 * MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK. Siehe die GNU General
 * Public License für weitere Details.
 * 
 * Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
 * Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.
 */
package de.knewcleus.fgfs.location;

import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import de.knewcleus.fgfs.Units;

public class Quaternion {
	public static Quaternion zero=new Quaternion(0,0,0,0);
	public static Quaternion one=new Quaternion(1,0,0,0);
	protected final double x,y,z,w;
	
	public Quaternion(double w, double x, double y, double z) {
		this.w=w;
		this.x=x;
		this.y=y;
		this.z=z;
	}
	
	public Quaternion(Vector3D v) {
		this.w=0;
		this.x=v.getX();
		this.y=v.getY();
		this.z=v.getZ();
	}
	
	public double magnitude() {
		return Math.sqrt(w*w+x*x+y*y+z*z);
	}
	
	public double getAngle() {
		return acos(w)*2.0*Units.RAD;
	}
	
	public Vector3D getAxis() {
		final double angle=acos(w);
		final double sina=sin(angle);
		// FIXME: what about sina==0?
		return new Vector3D(x/sina,y/sina,z/sina);
	}
	
	public Vector3D getAngleAxis() {
		final double angle=acos(w);
		final double sina=sin(angle);
		final double norm=magnitude();
		// FIXME: what about sina==0?
		return new Vector3D(2.0*angle*x/(sina*norm),2.0*angle*y/(sina*norm),2.0*angle*z/(sina*norm));
	}
	
	public static Quaternion fromAngleAxis(Vector3D angleAxis) {
		final double angle=angleAxis.getLength()/2.0;
		final Vector3D axis=angleAxis.normalise();
		final double sina=sin(angle),cosa=cos(angle);
		
		return new Quaternion(cosa,axis.getX()*sina,axis.getY()*sina,axis.getZ()*sina);
	}
	
	public Quaternion inverse() {
		double m=magnitude();
		return new Quaternion(w/m,-x/m,-y/m,-z/m);
	}
	
	public Quaternion normalize() {
		double m=magnitude();
		return new Quaternion(w/m,x/m,y/m,z/m);
	}
	
	public Quaternion conjugate() {
		return new Quaternion(w,-x,-y,-z);
	}
	
	public Quaternion add(Quaternion q) {
		return new Quaternion(w+q.w,x+q.x,y+q.y,z+q.z);
	}
	
	public Quaternion multiply(Quaternion q) {
		return new Quaternion(w*q.w-x*q.x-y*q.y-z*q.z,
							  w*q.x+q.w*x+y*q.z-z*q.y,
							  w*q.y+q.w*y+z*q.x-x*q.z,
							  w*q.z+q.w*z+x*q.y-y*q.x);
	}
	
	public Vector3D transform(Vector3D v) {
		// TODO: hand-optimisation
		Quaternion qv=new Quaternion(v);
		Quaternion qvn=multiply(qv).multiply(conjugate());
		return new Vector3D(qvn.x,qvn.y,qvn.z);
	}

	public static Quaternion fromEulerAngles(double z, double y, double x) {
		// sequence is z,y,x
		final double cosz2=cos(z/Units.RAD/2.0),sinz2=sin(z/Units.RAD/2.0);
		final double cosy2=cos(y/Units.RAD/2.0),siny2=sin(y/Units.RAD/2.0);
		final double cosx2=cos(z/Units.RAD/2.0),sinx2=sin(x/Units.RAD/2.0);
		
		final double cosz2cosy2=cosz2*cosy2,sinz2siny2=sinz2*siny2;
		final double cosz2siny2=cosz2*siny2,sinz2cosy2=sinz2*cosy2;
		
		return new Quaternion(cosz2cosy2*cosx2+sinz2siny2*sinx2,
							  cosz2cosy2*sinx2-sinz2siny2*cosx2,
							  cosz2siny2*cosx2+sinz2cosy2*sinx2,
							  sinz2cosy2*cosx2-cosz2siny2*sinx2);
	}
	
	public static Quaternion fromYawPitchRoll(double yaw, double pitch, double roll) {
		return fromEulerAngles(yaw, pitch, roll);
	}
	
	public static Quaternion fromLatLon(double lat, double lon) {
		// sequence is z, y
		final double z2=lon/Units.RAD/2.0;
		final double y2=-Math.PI/4.0-lat/Units.RAD/2.0;
		final double cosz2=cos(z2),sinz2=sin(z2);
		final double cosy2=cos(y2),siny2=sin(y2);
		return new Quaternion(cosz2*cosy2,-sinz2*siny2,cosz2*siny2,sinz2*cosy2);
	}
}
