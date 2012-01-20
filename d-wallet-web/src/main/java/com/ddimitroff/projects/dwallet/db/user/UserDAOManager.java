package com.ddimitroff.projects.dwallet.db.user;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ddimitroff.projects.dwallet.rest.user.UserRO;

@Component
@Transactional
public class UserDAOManager implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(UserDAOManager.class);

	@PersistenceContext(name = "dwallet", type = PersistenceContextType.EXTENDED)
	private EntityManager em;

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	public UserDAO getUserByCredentialsEmail(String email, String password) {
		try {
			UserDAO user = (UserDAO) em.createNamedQuery(UserDAO.GET_USER_BY_CREDENTIALS).setParameter("email", email).setParameter("password", password)
					.getSingleResult();
			return user;
		} catch (NoResultException e) {
			return null;
		}
	}

	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	public UserDAO getUserByName(String email) {
		try {
			return (UserDAO) em.createNamedQuery(UserDAO.GET_USER_BY_EMAIL).setParameter("email", email).getSingleResult();
		} catch (NoResultException ex) {
			return null;
		}
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void saveUser(UserDAO user) throws Exception {
		if (em.find(UserDAO.class, user.getId()) != null) {
			em.merge(user);
			logger.info("User " + user + " updated successfully.");
		} else {
			if (getUserByName(user.getEmail()) != null) {
				throw new Exception("User [" + user.getEmail() + "] already exists in database");
			}
			em.persist(user);

			logger.info("User [" + user.getEmail() + "] created successfully");
		}
		em.flush();
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void deleteUser(UserDAO actor, UserDAO user) {
		UserDAO toBeDeleted = em.find(UserDAO.class, user.getId());
		if (toBeDeleted != null) {
			em.remove(toBeDeleted);
			em.flush();

			logger.info("User " + user + " deleted successfully");
		}
	}

	public UserRO convert(UserDAO dao) {
		UserRO ro = new UserRO(dao.getEmail(), dao.getHashPassword());
		logger.info("UserDAO successfully converted to UserRO!");

		return ro;
	}

	public UserDAO convert(UserRO ro) {
		UserDAO dao = new UserDAO();
		dao.setEmail(ro.getUsername());
		dao.setHashPassword(ro.getHashPassword());
		dao.setRole(UserDAORole.USER);
		logger.info("UserRO successfully converted to UserDAO!");

		return dao;
	}

	public UserDAO getConvertedUser(UserRO ro) {
		UserDAO dao = getUserByCredentialsEmail(ro.getUsername(), ro.getHashPassword());
		if (null != dao) {
			logger.info("UserRO successfully converted to UserDAO and got from DB!");
			return dao;
		}

		return null;
	}

}