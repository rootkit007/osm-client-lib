package com.xapi.models;

import java.util.List;
import java.util.Map;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementMap;

import android.util.Log;

public class Way {
	
	private static final String TAG = Way.class.getCanonicalName();

	public static final String TAG_HIGHWAY_TYPE = "highway";
	public static final String TAG_MAXSPEED = "maxspeed";
	public static final String TAG_ROAD_NAME = "name";
	public static final String TAG_ROAD_REF = "ref";

	public static final double KPH_TO_MS = 0.277778D;
	public static final double MPH_TO_MS = 0.44704D;
	public static final double KNOTS_TO_MS = 0.514444D;

	@Attribute(name="id")
	public Long id;
	
	@ElementList(inline=true,entry="nd",required=false)
	public List<NodeRef> nodes;
	
	@ElementMap(entry="tag",key="k",value="v",attribute=true,inline=true,required=false)
	public Map<String, String> tags;

	public String getHighwayType() {
		if ( tags != null )
			return tags.get(TAG_HIGHWAY_TYPE);
		return null;
	}
	
	/**
	 * Returns maxspeed in m/s
	 * or NULL if unknown
	 * @return
	 */
	public Double getMaxSpeed() {
		if ( tags != null ) {
			String maxSpeed = tags.get(TAG_MAXSPEED);
			if ( maxSpeed != null && !maxSpeed.isEmpty() ) {
				String[] comp = maxSpeed.split(" ");
				Double speedValue = 0D;
				try {
					speedValue = Double.parseDouble(comp[0]);
				} catch (Exception ex) {
					Log.w(TAG,"Error converting speed limit value " + comp[0]);
					return null;
				}
				// default unit is kph
				Double multiplier = KPH_TO_MS;
				if ( comp.length > 1 ) {
					if ( comp[1].equalsIgnoreCase("mph") ) {
						multiplier = MPH_TO_MS;
					}
					if ( comp[1].equalsIgnoreCase("knots") ) {
						multiplier = KNOTS_TO_MS;
					}
				}
				return speedValue * multiplier;
			}
		}
		return null;
	}
	
	public String getRoadName() {
		String roadName = null;
		if ( tags != null ) {
			roadName = tags.get(TAG_ROAD_NAME);
			if ( roadName == null )
				roadName = tags.get(TAG_ROAD_REF);
		}
		return roadName;
	}

	public String toString() {
		return String.format("id %d name %s nodes %d", id, getRoadName(), nodes.size()); 
	}
	
	public boolean equals(Object o) {
		if ( o != null && o instanceof Way ) {
			return this.id.equals((Way)o);
		}
		return false;
	}
	
}
