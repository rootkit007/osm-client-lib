package com.greatnowhere.osmclient;

import java.util.List;

import com.xapi.models.Node;
import com.xapi.models.NodeRef;
import com.xapi.models.Way;

import android.location.Location;
import android.util.Log;

public class GISUtils {

	private static final String TAG = GISUtils.class.getCanonicalName();
	
	public static final double EARTH_RADIUS = 6371000D;
	
	/**
	 * Returns bounding box centered on location
	 * 
	 * @param loc
	 * @param radius, in meters
	 * @return long lat long lat
	 */
	public static double[] getBoundingBoxCoords(Location loc, Double radius) {
		double dY = 360 * radius / EARTH_RADIUS;
		double dX = dY * Math.cos(Math.toRadians(loc.getLatitude()));
		return new double[] { loc.getLongitude() - dX, loc.getLatitude() - dY, loc.getLongitude() + dX, loc.getLatitude() + dY};
	}
	
	/**
	 * Returns TRUE if specified location is "on the way" identified by list of nodes
	 * @param loc location
	 * @param nodes
	 * @param wayRadius way radius (width) in meters
	 * @return
	 */
	public static boolean isOnTheWay(Location loc, List<NodeRef> nodes, double wayRadius) {

		// sanity check
		if ( nodes == null || nodes.size() <= 1 )
			return false;
		
		double crossTrackDistance = EARTH_RADIUS;
		// compute cross-track distance for every way segment
		for (int i=0; i<nodes.size()-1; i++ ) {
			Node startPoint = nodes.get(i).node;
			Node endPoint = nodes.get(i+1).node;
			
			if ( startPoint == null || endPoint == null ) {
				Log.w(TAG, "NodeRef contains NULL node, processing skipped");
				continue;
			}
			
			// compute distance and bearing of way segment
			float[] results = new float[2];
			Location.distanceBetween(startPoint.lat, startPoint.lon, endPoint.lat, endPoint.lon, results);

			// compute distance and bearing of way segment start point to our point
			float[] resultsLoc = new float[2];
			Location.distanceBetween(startPoint.lat, startPoint.lon, loc.getLatitude(), loc.getLongitude(), resultsLoc);

			// compute distance
			// see http://www.movable-type.co.uk/scripts/latlong.html
			// dxt = asin(sin(d13/R)⋅sin(θ13−θ12)) ⋅ R
			double dxt = Math.asin(Math.sin(resultsLoc[0]/EARTH_RADIUS) * Math.sin(resultsLoc[1]-results[1])) * EARTH_RADIUS;
			if ( Math.abs(dxt) < Math.abs(crossTrackDistance) )
				crossTrackDistance = Math.abs(dxt);

			// if cross track distance is already within our radius, dont waste time computing distance from others
			if ( crossTrackDistance <= wayRadius ) 
				return true;
		}
		
		return ( crossTrackDistance <= wayRadius );
		
	}
	
	public static boolean isOnTheWay(Location loc, Way way) {
		if ( way == null )
			return false;

		// triple the regular way width to account for accuracy loss
		boolean onTheWay = isOnTheWay(loc, way.nodes, 3 * OSMClient.WAY_RADIUS_METERS);
		Log.d(TAG, "Location " + loc.getLatitude() + " " + loc.getLongitude() + " way " + way.toString() + " result " + onTheWay);
		return onTheWay;
	}
	
	public static double getDistance(double startLatitude,double startLongitude, double endLatitude, double endLongitude) {
		float[] results = new float[2];
		Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, results);
		return results[0];
	}
	
	public static double getBearing(double startLatitude,double startLongitude, double endLatitude, double endLongitude) {
		float[] results = new float[2];
		Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, results);
		return results[1];
	}
	
	public static double getBearingDifference(double a1, double a2) {
	    return Math.min((a1-a2)<0?a1-a2+360:a1-a2, (a2-a1)<0?a2-a1+360:a2-a1);
	}

	public static double getUnsignedBearingDifference(double a1, double a2) {
		a1 = Math.abs(a1);
		a2 = Math.abs(a2);
	    if ( a1 > 180 ) a1 -= 180;
	    if ( a2 > 180 ) a2 -= 180;
	    return Math.abs(getBearingDifference(a1, a2));
	}

}
