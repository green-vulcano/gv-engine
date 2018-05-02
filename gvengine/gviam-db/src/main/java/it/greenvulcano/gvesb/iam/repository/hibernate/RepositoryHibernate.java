package it.greenvulcano.gvesb.iam.repository.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

abstract class RepositoryHibernate {
	
	private SessionFactory sessionFactory;
			
	protected void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	protected Session getSession() {
		return sessionFactory.getCurrentSession();
	}
	
}