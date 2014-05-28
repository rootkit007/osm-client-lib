package com.greatnowhere.osmclient;

import java.net.URI;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.XmlSpringAndroidSpiceService;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;
import com.xapi.models.Response;

public class OSMClient {
	
	private static final String TAG = OSMClient.class.getCanonicalName();

	private String serviceURL;
	private SpiceManager sm = new SpiceManager(XmlSpringAndroidSpiceService.class);
	public static final double WAY_RADIUS_METERS = 3D;
	public static final String DEFAULT_SERVICE_URL = "http://www.overpass-api.de/api/xapi?*";

	public OSMClient(Context ctx) {
		sm.start(ctx);
		serviceURL = DEFAULT_SERVICE_URL;
	}
	
	public OSMClient(String serviceURL, Context ctx) {
		this(ctx);
		this.serviceURL = serviceURL;
	}
	
	public void stop() {
		sm.shouldStop();
	}
	
	public void getWay(Location loc, RequestListener<Response> requestListener) {
		sm.execute(new OSMXAPIRequest(loc, WAY_RADIUS_METERS), requestListener);
	}
	
	private class OSMXAPIRequest extends SpringAndroidSpiceRequest<Response> {

		private String requestUrl;
		
		public OSMXAPIRequest(Location loc, double boxRadius) {
			super(Response.class);
			double[] bBox = GISUtils.getBoundingBoxCoords(loc, boxRadius);
			requestUrl = serviceURL + "[bbox=" + String.format("%f,%f,%f,%f", bBox[0], bBox[1], bBox[2], bBox[3]) + "]";
			Log.i(TAG, requestUrl);
		}

		@Override
		public Response loadDataFromNetwork() throws Exception {
			URI uri = new URI(requestUrl);
			return getRestTemplate().getForObject(uri, Response.class);
		}
		
	}
	
}
