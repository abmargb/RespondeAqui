package com.merespondeaqui.distance;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.io.IOUtils;


public class Geocode {

	public static double distance(String fromStr, String toStr) throws MalformedURLException, IOException {
		
		LatLng fromLatLng = getLatLng(fromStr);
		LatLng toLatLng = getLatLng(toStr);
		
		return distFrom(fromLatLng.lat, fromLatLng.lng, 
				toLatLng.lat, toLatLng.lng);
	}

	private static LatLng getLatLng(String fromStr) throws IOException,
			MalformedURLException, UnsupportedEncodingException {
		String jsonStr = IOUtils.toString(new URL(
				"http://maps.google.com/maps/api/geocode/json?address=" + URLEncoder.encode(fromStr, "UTF-8") + "&sensor=false"
		).openStream());
		
		JSONObject json = (JSONObject) JSONSerializer.toJSON(jsonStr);
		JSONObject result = (JSONObject) json.getJSONArray("results").get(0);
		JSONObject location = result.getJSONObject("geometry").getJSONObject("location");
		double lat = location.getDouble("lat");
		double lng = location.getDouble("lng");
		
		return new LatLng(lat, lng);
	}
	
	public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
		double earthRadius = 3958.75;
		double dLat = Math.toRadians(lat2-lat1);
		double dLng = Math.toRadians(lng2-lng1);
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		
		Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
		Math.sin(dLng/2) * Math.sin(dLng/2);
		
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double dist = earthRadius * c;

		return dist/0.62;
	}
	
	private static class LatLng {
		double lat;
		double lng;
		
		public LatLng(double lat, double lng) {
			this.lat = lat;
			this.lng = lng;
		}
	}

	
}
