package com.greatnowhere.osmclient;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.xapi.models.Response;
import com.xapi.models.Way;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class OSMLocationListener {

	private static final String TAG = OSMClient.class.getCanonicalName();
	
	protected OSMClient client;
	protected LocationManager lm;
	protected OSMWayChangedListener ls;
	protected LocationChangeListener lcs;
	protected Way currentWay;
	
	public OSMLocationListener(Context ctx) {
		client = new OSMClient(ctx);
		lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
	}
	
	public void stop() {
		client.stop();
		removeLocationListener();
		ls = null;
	}
	
	public void setOSMWayListener(OSMWayChangedListener listener) {
		ls = listener;
		setUpLocationListener();
	}
	
	public void removeOSMWayListener() {
		removeLocationListener();
		ls = null;
	}
	
	protected void setUpLocationListener() {
		removeLocationListener();
		lcs = new LocationChangeListener();
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 10, lcs);
	}
	
	protected void removeLocationListener() {
		if ( lcs != null ) {
			lm.removeUpdates(lcs);
		}
	}
	
	protected void setCurrentWay(Way w) {
		Log.i(TAG,"Got OSM way " + w.toString());
		if ( ls != null && w != currentWay ) { 
			ls.onOSMWayChangedListener(w);
		}
		currentWay = w;
	}
	
	protected class LocationChangeListener implements LocationListener {
		public void onLocationChanged(final Location arg0) {
			
			Log.i(TAG,"Location changed " + arg0.toString());

			// GPS location changed, request new way from OSM if outside current way
			if ( currentWay == null || !GISUtils.isOnTheWay(arg0, currentWay) ) {

				// Make OSM request
				client.getWay(arg0, new RequestListener<Response>() {
					public void onRequestSuccess(Response result) {
						if ( result == null || result.getMainWay() == null ) {
							Log.w(TAG,"NULL OSM result or way");
							return;
						}
						setCurrentWay(result.getMainWay());
					}
					
					public void onRequestFailure(SpiceException spiceException) {
						Log.w(TAG, "Network request failed: " + spiceException.getLocalizedMessage());
						setCurrentWay(null);
					}
				}); 
				
			} // end if OSM request
			
		}

		public void onProviderDisabled(String arg0) {
		}

		public void onProviderEnabled(String arg0) {
		}

		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		}
	}
	
	public interface OSMWayChangedListener {
		public void onOSMWayChangedListener(Way way);
	}
	
}
