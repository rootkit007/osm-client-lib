package com.greatnowhere.osmclient;

import java.util.concurrent.atomic.AtomicBoolean;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.xapi.models.Response;
import com.xapi.models.Way;

import android.content.Context;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

public class OSMLocationListener {

	private static final String TAG = OSMClient.class.getCanonicalName();
	
	protected OSMClient client;
	protected LocationManager lm;
	protected OSMWayChangedListener ls;
	protected LocationChangeListener lcs;
	protected Way currentWay;
	protected Location currentLocation;
	protected long currentLocationTimeStamp;
	protected GPSStatusChangeListener gpsStatusListener;
	
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
		gpsStatusListener = new GPSStatusChangeListener();
		lm.addGpsStatusListener(gpsStatusListener);
	}
	
	protected void removeLocationListener() {
		if ( lcs != null ) {
			lm.removeUpdates(lcs);
			lm.removeGpsStatusListener(gpsStatusListener);
			gpsStatusListener = null;
		}
	}
	
	protected void setCurrentWay(Way w) {
		Log.i(TAG,"Got OSM way " + ( w == null ? "null" : w.toString()));
		if ( ls != null && w != currentWay ) { 
			ls.onOSMWayChangedListener(w);
		}
		currentWay = w;
	}
	
	protected class LocationChangeListener implements LocationListener {
		public void onLocationChanged(final Location arg0) {
			
			Log.i(TAG,"Location changed " + arg0.toString());
			currentLocation = arg0;
			currentLocationTimeStamp = SystemClock.elapsedRealtime();

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
			Log.i(TAG,"Location provider " + arg0 + " disabled");
		}

		public void onProviderEnabled(String arg0) {
			Log.i(TAG,"Location provider " + arg0 + " enabled");
		}

		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			Log.i(TAG,"Location provider " + arg0 + " status change " + arg1);
		}
	}
	
	public interface OSMWayChangedListener {
		public void onOSMWayChangedListener(Way way);
	}
	
	protected class GPSStatusChangeListener implements Listener {

		private AtomicBoolean isGPSFix = new AtomicBoolean(false);
		private final static long GPS_FIX_TIMEOUT = 10000L;
		
		public void onGpsStatusChanged(int event) {
		       switch (event) {
	            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
	                if ( currentLocation != null)
	                    isGPSFix.set((SystemClock.elapsedRealtime() - currentLocationTimeStamp) < GPS_FIX_TIMEOUT);

	                if ( isGPSFix.get() ) { // A fix has been acquired.
	                	Log.i(TAG,"GPS fix acquired");
	                } else { // The fix has been lost.
	                	Log.i(TAG,"GPS fix lost");
	                    setCurrentWay(null);
	                }

	                break;
	            case GpsStatus.GPS_EVENT_FIRST_FIX:
	                isGPSFix.set(true);

	                break;
	        }		
		}
		
	}
	
}
