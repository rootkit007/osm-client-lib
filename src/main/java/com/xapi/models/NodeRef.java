package com.xapi.models;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Transient;

public class NodeRef {

	@Attribute(name="ref")
	public Long ref;

	/**
	 * Resolved Node object, if any
	 */
	@Transient
	public Node node;
}
