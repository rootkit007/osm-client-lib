osm-client-lib
==============

OpenStreetMaps Java client library

Java library for Android, supports making XAPI requests to OpenStreetMaps. It is very much work in progress.

Requires SimpleXML serializer library and RoboSpice, see pom.xml

Usage:

			OSMLocationListener osmListener;
			osmListener = new OSMLocationListener(getApplicationContext());
			osmListener.setMaxRequests(1);
			// this starts GPS location updates and callbacks to OSMListener whenever current way changes
			osmListener.setOSMWayListener(new OSMListener());
			
			class OSMListener implements OSMWayChangedListener {
				public void onOSMWayChangedListener(Way way) {
					Log.i(TAG, "Got OSM way " + ( way == null ? "null" : way.toString()));
					currentWay = way;
				}
			}
			
