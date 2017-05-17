package it.greenvulcano.gvesb.gviamx.repository.hibernate;

import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import it.greenvulcano.gvesb.gviamx.domain.SignUpRequest;
import it.greenvulcano.gvesb.gviamx.repository.SignUpRepository;

public class SignUpRepositoryHibernate extends RepositoryHibernate<SignUpRequest, Long> implements SignUpRepository {

	
	public SignUpRepositoryHibernate() {
		super(SignUpRequest.class);
	}

	@Override
	public void setSessionFactory(SessionFactory sessionFactory) {
		super.setSessionFactory(sessionFactory);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Optional<SignUpRequest> get(String email) {		
		Query<SignUpRequest> query = getSession().createQuery("from SignUpRequest s where s.email = :_email");
		return query.setParameter("_email", email).uniqueResultOptional();
	}

		
}
