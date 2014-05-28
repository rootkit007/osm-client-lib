package com.xapi.models;

import java.util.List;
import java.util.Map;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementMap;

public class Way {

	public static final String TAG_HIGHWAY_TYPE = "highway";
	public static final String TAG_MAXSPEED = "maxspeed";
	public static final String TAG_ROAD_NAME = "name";
	public static final String TAG_ROAD_REF = "ref";
	
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
	
	public String getMaxSpeed() {
		if ( tags != null )
			return tags.get(TAG_MAXSPEED);
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
