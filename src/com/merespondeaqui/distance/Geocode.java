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

	private static final double MIN_LAT = Math.toRadians(-90d);  // -PI/2
	private static final double MAX_LAT = Math.toRadians(90d);   //  PI/2
	private static final double MIN_LON = Math.toRadians(-180d); // -PI
	private static final double MAX_LON = Math.toRadians(180d);  //  PI
	
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
		double earthRadiusInMIles = 3958.75;
		double dLat = Math.toRadians(lat2-lat1);
		double dLng = Math.toRadians(lng2-lng1);
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		
		Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
		Math.sin(dLng/2) * Math.sin(dLng/2);
		
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double dist = earthRadiusInMIles * c;

		return dist/0.62;
	}
	
	public static double[] boundingBox(double lat, double lng, double distanceInKm) {
		double earthRadiusInKm = 6371.01;
		
		// angular distance in radians on a great circle
		double radDist = distanceInKm / earthRadiusInKm;

		double radLat = Math.toRadians(lat);
		double radLon = Math.toRadians(lng);
		
		double minLat = radLat - radDist;
		double maxLat = radLat + radDist;

		double minLon, maxLon;
		if (minLat > MIN_LAT && maxLat < MAX_LAT) {
			double deltaLon = Math.asin(Math.sin(radDist) /
				Math.cos(radLat));
			minLon = radLon - deltaLon;
			if (minLon < MIN_LON) minLon += 2d * Math.PI;
			maxLon = radLon + deltaLon;
			if (maxLon > MAX_LON) maxLon -= 2d * Math.PI;
		} else {
			// a pole is within the distance
			minLat = Math.max(minLat, MIN_LAT);
			maxLat = Math.min(maxLat, MAX_LAT);
			minLon = MIN_LON;
			maxLon = MAX_LON;
		}

		return new double[]{minLat, minLon, maxLat, maxLon};
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
