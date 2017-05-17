package it.greenvulcano.gvesb.gviamx.repository.hibernate;

import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import it.greenvulcano.gvesb.gviamx.domain.PasswordResetRequest;
import it.greenvulcano.gvesb.gviamx.repository.PasswordResetRepository;

public class PasswordResetRepositoryHibernate extends RepositoryHibernate<PasswordResetRequest, Long> implements PasswordResetRepository {

	
	public PasswordResetRepositoryHibernate() {
		super(PasswordResetRequest.class);
	}

	@Override
	public void setSessionFactory(SessionFactory sessionFactory) {
		super.setSessionFactory(sessionFactory);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Optional<PasswordResetRequest> get(String email) {		
		Query<PasswordResetRequest> query = getSession().createQuery("from PasswordResetRequest r where r.email = :_email");
		return query.setParameter("_email", email).uniqueResultOptional();
	}

	
	
}
