package com.merespondeaqui;

import java.util.Date;

import twitter4j.Annotations;
import twitter4j.GeoLocation;
import twitter4j.Place;
import twitter4j.Tweet;

public class TweetImpl implements Tweet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String text;
	private final String user;

	public TweetImpl(String text, String user) {
		this.text = text;
		this.user = user;
	}

	@Override
	public int compareTo(Tweet o) {
		return 0;
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public long getToUserId() {
		return 0;
	}

	@Override
	public String getToUser() {
		return null;
	}

	@Override
	public String getFromUser() {
		return user;
	}

	@Override
	public long getId() {
		return 0;
	}

	@Override
	public long getFromUserId() {
		return 0;
	}

	@Override
	public String getIsoLanguageCode() {
		return null;
	}

	@Override
	public String getSource() {
		return null;
	}

	@Override
	public String getProfileImageUrl() {
		return null;
	}

	@Override
	public Date getCreatedAt() {
		return null;
	}

	@Override
	public GeoLocation getGeoLocation() {
		return null;
	}

	@Override
	public String getLocation() {
		return null;
	}

	@Override
	public Place getPlace() {
		return null;
	}

	@Override
	public Annotations getAnnotations() {
		return null;
	}

}
