package com.xapi.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Transient;
import org.simpleframework.xml.core.Commit;

import android.util.Log;

import com.greatnowhere.osmclient.GISUtils;

@Root(name="osm",strict=false)
public class Response {
	
	private static final String TAG = Response.class.getCanonicalName();

	@Attribute(name="version",required=false)
	public String version;
	
	@Attribute(name="generator",required=false)
	public String generator;
	
	@Element(name="note",required=false)
	public String note;
	
	@Element(name="meta",required=false)
	public Meta meta;
	
	@ElementList(inline=true,entry="node",required=false)
	public List<Node> nodes;
	
	@Transient
	public Map<Long, Node> nodeMap;
	
	@ElementList(entry="way",inline=true,required=false)
	public List<Way> ways;
	
	@Commit
	public void build() {
		// build a hashmap of nodes
		nodeMap = new HashMap<Long, Node>();
		if ( nodes != null ) {
			for (Node n : nodes) {
				nodeMap.put(n.id, n);
			}
		}
		if ( ways != null ) {
			for (Way w : ways ) {
				buildWayNodeRefs(w);
			}
		}
	}
	
	private void buildWayNodeRefs(Way w) {
		if ( w != null && w.nodes != null ) {
			for (NodeRef nd : w.nodes ) {
				nd.node = nodeMap.get(nd.ref);
			}
		}
	}
	
	public Way getMainWay() {
		if ( ways != null && ways.size() > 0 ) {
			return ways.get(0);		
		}
		return null;
	}
	
	/**
	 * Returns way that is aligned most closely with current bearing, lat, long
	 * @param bearing
	 * @param latitude
	 * @param longitude
	 * @return
	 */
	public Way getMainWay(float bearing, double latitude, double longitude) {
		try {
			if ( ways != null && ways.size() > 0 ) {
				if ( ways.size() == 1 )
					return ways.get(0);
				Way bestWay = ways.get(0);
				double bestBearing = 90;
				for ( Way w : ways ) {
					Log.d(TAG,"examining way " + w);
					// sanity check
					if ( w.nodes == null || w.nodes.size() == 0 )
						continue;
					
					// compute way's bearing at the point closest to current location
					// find closest node
					double maxDist = Double.MAX_VALUE;
					Node closestNode = w.nodes.get(0).node;
					int closestNodeIdx = 0;
					for ( int i=0; i<w.nodes.size(); i++ ) {
						NodeRef nr = w.nodes.get(i);
						double nodeDist = GISUtils.getDistance(latitude, longitude, nr.node.lat, nr.node.lon);
						if ( nodeDist < maxDist ) {
							closestNode = nr.node;
							closestNodeIdx = i;
							maxDist = nodeDist;
						}
					}
					Log.d(TAG,"closest node " + closestNode.id);
					// find bearings from the prev node
					double b1 = bearing - 90;
					if ( closestNodeIdx > 0 ) {
						// bearing to prev node
						Node prevNode = w.nodes.get(closestNodeIdx-1).node;
						b1 = GISUtils.getBearing(closestNode.lat, closestNode.lon, prevNode.lat, prevNode.lon );
					}
					// find bearing to the next node
					double b2 = bearing - 90;
					if ( closestNodeIdx < w.nodes.size()-1 ) {
						// bearing to next node
						Node nextNode = w.nodes.get(closestNodeIdx+1).node;
						b2 = GISUtils.getBearing(closestNode.lat, closestNode.lon, nextNode.lat, nextNode.lon );
					}
					double bDiff1 = GISUtils.getUnsignedBearingDifference(bearing, b1);
					double bDiff2 = GISUtils.getUnsignedBearingDifference(bearing, b2);
					double bestWayBearing = Math.min(bDiff1, bDiff2); 
					Log.d(TAG,"computed bearings " + b1 + " " + b2 + " diffs " + bDiff1 + " " + bDiff2);
					// figure out the best bearing for current way
					if ( Math.min(bestWayBearing, bestBearing) < bestBearing ) {
						Log.d(TAG,"best way candidate " + w.toString());
						bestWay = w;
						bestBearing = Math.min(bestWayBearing, bestBearing); 
					}
				}
				return bestWay;
			}
			return null;
		} catch (Exception ex) {
			Log.w(TAG,ex);
			return null;
		}
	}
	
}
