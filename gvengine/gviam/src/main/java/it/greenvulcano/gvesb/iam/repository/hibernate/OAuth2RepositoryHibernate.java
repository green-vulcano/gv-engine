package it.greenvulcano.gvesb.iam.repository.hibernate;

import org.hibernate.SessionFactory;

import it.greenvulcano.gvesb.iam.domain.OAuth2Identity;

public class OAuth2RepositoryHibernate extends RepositoryHibernate<OAuth2Identity, String> {

	public OAuth2RepositoryHibernate() {
		super(OAuth2Identity.class);		
	}
	
	@Override
	public void setSessionFactory(SessionFactory sessionFactory) {	
		super.setSessionFactory(sessionFactory);
	}

}
