package it.greenvulcano.gvesb.iam.repository.hibernate;

import java.util.Optional;

import org.hibernate.SessionFactory;

import it.greenvulcano.gvesb.iam.domain.Credentials;
import it.greenvulcano.gvesb.iam.repository.CredentialsRepository;

/**
 * 
 * {@link CredentialsRepository} implementation using Hibernate ORM framework,
 * expects injection of a {@link SessionFactory}  
 * 
 */
public class CredentialsRepositoryHibernate extends RepositoryHibernate<Credentials, String> implements CredentialsRepository {

	public CredentialsRepositoryHibernate() {
		super(Credentials.class);		
	}
	
	@Override
	public void setSessionFactory(SessionFactory sessionFactory) {	
		super.setSessionFactory(sessionFactory);
	}

	@Override
	public Optional<Credentials> find(String username) {
		return Optional.ofNullable((Credentials)getSession().createQuery("from Credentials where resourceOwner.username = :uname")
				  .setParameter("uname", username)
				  .uniqueResult());
	}

}
