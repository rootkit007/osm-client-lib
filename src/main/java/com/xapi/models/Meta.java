package com.xapi.models;

import org.simpleframework.xml.Attribute;

public class Meta {

	/**
	 * ISO-formatted timestamp
	 */
	@Attribute(name="osm_base",required=false)
	public String timestamp;
	
}
