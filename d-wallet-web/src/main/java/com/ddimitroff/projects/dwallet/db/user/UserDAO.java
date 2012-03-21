package com.ddimitroff.projects.dwallet.db.user;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.ddimitroff.projects.dwallet.db.cash.CashFlowDAOCurrencyType;

@Entity
@Table(name = "USERS")
@NamedQueries({
		@NamedQuery(name = UserDAO.GET_USER_BY_CREDENTIALS, query = "SELECT user FROM UserDAO user WHERE user.email = :email AND user.hashPassword = :password"),
		@NamedQuery(name = UserDAO.GET_USER_BY_EMAIL, query = "SELECT user FROM UserDAO user WHERE user.email = :email") })
public class UserDAO implements Comparable<UserDAO>, Serializable {

	private static final long serialVersionUID = 1L;

	public static final String GET_USER_BY_EMAIL = "User.getUserByEmail";
	public static final String GET_USER_BY_CREDENTIALS = "User.getUserByCredentials";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@Column(unique = true, length = 64)
	private String email;

	@Column(length = 64)
	private String hashPassword;

	@Column(length = 32)
	@Enumerated(EnumType.STRING)
	private UserDAORole role;

	@Column(length = 32)
	@Enumerated(EnumType.STRING)
	private CashFlowDAOCurrencyType defaultCurrency;

	@Column
	private double startupBalance;

	public UserDAO() {
	}

	public UserDAO(String email, UserDAORole role, CashFlowDAOCurrencyType defaultCurrency, double startupBalance) {
		if (null == email) {
			throw new IllegalArgumentException("User name should be specified!");
		}
		if (null == role) {
			throw new IllegalArgumentException("User role should be specified!");
		}
		if (null == defaultCurrency) {
			throw new IllegalArgumentException("User default balance currency should be specified!");
		}

		this.email = email;
		this.role = role;
		this.defaultCurrency = defaultCurrency;
		this.startupBalance = startupBalance;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getHashPassword() {
		return hashPassword;
	}

	public void setHashPassword(String hashPassword) {
		this.hashPassword = hashPassword;
	}

	public UserDAORole getRole() {
		return role;
	}

	public void setRole(UserDAORole role) {
		this.role = role;
	}

	public CashFlowDAOCurrencyType getDefaultCurrency() {
		return defaultCurrency;
	}

	public void setDefaultCurrency(CashFlowDAOCurrencyType defaultCurrency) {
		this.defaultCurrency = defaultCurrency;
	}

	public double getStartupBalance() {
		return startupBalance;
	}

	public void setStartupBalance(double startupBalance) {
		this.startupBalance = startupBalance;
	}

	@Override
	public int compareTo(UserDAO user) {
		return this.getEmail().compareTo(user.getEmail());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(startupBalance);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((defaultCurrency == null) ? 0 : defaultCurrency.hashCode());
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((role == null) ? 0 : role.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserDAO other = (UserDAO) obj;
		if (Double.doubleToLongBits(startupBalance) != Double.doubleToLongBits(other.startupBalance))
			return false;
		if (defaultCurrency != other.defaultCurrency)
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (role != other.role)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return email + " [" + role + "]" + " has " + startupBalance + " balance in " + defaultCurrency;
	}

}
