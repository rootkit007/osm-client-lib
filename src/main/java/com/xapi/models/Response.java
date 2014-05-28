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

@Root(name="osm",strict=false)
public class Response {

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
	
}
