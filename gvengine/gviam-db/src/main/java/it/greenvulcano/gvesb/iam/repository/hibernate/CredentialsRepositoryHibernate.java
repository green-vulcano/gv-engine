package it.greenvulcano.gvesb.iam.repository.hibernate;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hibernate.SessionFactory;

import it.greenvulcano.gvesb.iam.domain.Credentials;
import it.greenvulcano.gvesb.iam.domain.jpa.CredentialsJPA;

/**
 * 
 * {@link CredentialsRepository} implementation using Hibernate ORM framework,
 * expects injection of a {@link SessionFactory}  
 * 
 */
public class CredentialsRepositoryHibernate extends RepositoryHibernate {

	
	
	@Override
	public void setSessionFactory(SessionFactory sessionFactory) {	
		super.setSessionFactory(sessionFactory);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
        public Set<Credentials> getUserCredentials(String username) {
		List userCredentials = getSession().createQuery("from CredentialsJPA where resourceOwner.username = :uname order by issueTime desc")
		                                   .setParameter("uname", username)
		                                   .list();
				       
		return new LinkedHashSet<Credentials>(userCredentials);
	}
	
	public Optional<Credentials> get(String key) {		
		return Optional.ofNullable(getSession().get(CredentialsJPA.class, key));
	}

	public void add(Credentials entity) {
		getSession().saveOrUpdate(CredentialsJPA.class.cast(entity));
		getSession().flush();
		
	}

	@SuppressWarnings("unchecked")
	public Set<Credentials> getAll() {
		List<Credentials> credentials = getSession().createQuery("from CredentialsJPA").list();
		return new LinkedHashSet<Credentials>(credentials);
	}

	public void remove(Credentials entity) {
		getSession().delete(CredentialsJPA.class.cast(entity));
		getSession().flush();		
	}
	
	public void removeByUser(Long userId) {
	    getSession().createQuery("delete from CredentialsJPA where resourceOwner.id = :userId")
                        .setParameter("userId", userId)
                        .executeUpdate();
	}

}
