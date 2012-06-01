package com.ddimitroff.projects.dwallet.rest.token;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.jsecurity.crypto.hash.Md5Hash;

import com.ddimitroff.projects.dwallet.db.entities.User;

public class Token implements Comparable<Token> {

	private static final int TOKEN_TIMEOUT = 30; // 30 minutes
	private static final SimpleDateFormat TOKEN_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd-hhmm");

	private User owner;
	private Date createdOn;
	private String id;
	private String validTo;

	public Token(User owner) {
		this.owner = owner;
		this.createdOn = new Date();
		this.id = generateTokenId(owner.getEmail());
		this.validTo = getValidToDateAsString();
	}

	private String generateTokenId(String username) {
		Md5Hash hash = new Md5Hash(username, String.valueOf(createdOn.getTime()));

		return hash.toHex();
	}

	private String getValidToDateAsString() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(createdOn);
		cal.add(Calendar.MINUTE, TOKEN_TIMEOUT);

		return TOKEN_DATE_FORMAT.format(cal.getTime());
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getValidTo() {
		return validTo;
	}

	public void setValidTo(String validTo) {
		this.validTo = validTo;
	}

	@Override
	public String toString() {
		return "Token [owner=" + owner.getEmail() + ", createdOn=" + createdOn + ", id=" + id + ", validTo=" + validTo
				+ "]";
	}

	@Override
	public int compareTo(Token token) {
		return this.getOwner().compareTo(token.getOwner());
	}

}
