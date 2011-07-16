package com.merespondeaqui.utils;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.io.IOUtils;


public class GeoUtils {

	private static final double WGS84_a = 6378137.0;  // Major semiaxis [m]
	private static final double WGS84_b = 6356752.3;  // Minor semiaxis [m]
	
	public static double distance(String fromStr, String toStr) throws MalformedURLException, IOException {
		
		LatLng fromLatLng = getLatLng(fromStr);
		LatLng toLatLng = getLatLng(toStr);
		
		return distFrom(fromLatLng.lat, fromLatLng.lng, 
				toLatLng.lat, toLatLng.lng);
	}
	
	public static double[] boundingBox(String placeStr, double distanceInKm)
			throws MalformedURLException, UnsupportedEncodingException,
			IOException {
		LatLng latLng = getLatLng(placeStr);
		return boundingBox(latLng.lat, latLng.lng, distanceInKm);
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
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2)
				* Math.sin(dLng / 2);

		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double dist = earthRadiusInMIles * c;

		return dist / 0.62;
	}
	// Earth radius at a given latitude, according to the WGS-84 ellipsoid [m]
	private static double WGS84EarthRadius(double lat) {
	    // http://en.wikipedia.org/wiki/Earth_radius
	    
		double An = WGS84_a*WGS84_a * Math.cos(lat);
	    double Bn = WGS84_b*WGS84_b * Math.sin(lat);
	    double Ad = WGS84_a * Math.cos(lat);
	    double Bd = WGS84_b * Math.sin(lat);
	    return Math.sqrt((An*An + Bn*Bn)/(Ad*Ad + Bd*Bd));
	}
	// Bounding box surrounding the point at given coordinates,
	// assuming local approximation of Earth surface as a sphere
	// of radius given by WGS84
	private static double[] boundingBox(double latitudeInDegrees,
			double longitudeInDegrees, double halfSideInKm) {

		double lat = Math.toRadians(latitudeInDegrees);
		double lon = Math.toRadians(longitudeInDegrees);
		double halfSide = 1000 * halfSideInKm;

		// Radius of Earth at given latitude
		double radius = WGS84EarthRadius(lat);
		// Radius of the parallel at given latitude
		double pradius = radius * Math.cos(lat);

		double latMin = lat - halfSide / radius;
		double lonMin = lon + halfSide / pradius;
		
		double latMax = lat + halfSide / radius;
		double lonMax = lon - halfSide / pradius;

		return new double[] { Math.toDegrees(latMin), Math.toDegrees(lonMin),
				Math.toDegrees(latMax), Math.toDegrees(lonMax) };
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
