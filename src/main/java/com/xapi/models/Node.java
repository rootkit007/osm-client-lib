package com.xapi.models;

import java.util.Map;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementMap;

public class Node {

	@Attribute(name="id")
	public Long id;
	
	@Attribute(name="lat")
	public Double lat;
	
	@Attribute(name="lon")
	public Double lon;
	
	@ElementMap(entry="tag",key="k",value="v",attribute=true,inline=true,required=false)
	public Map<String, String> tags;
	
}
