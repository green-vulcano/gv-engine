package it.greenvulcano.gvesb.gviamx.repository.hibernate;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import it.greenvulcano.gvesb.gviamx.repository.Repository;

abstract class RepositoryHibernate<T, K extends Serializable> implements Repository<T, K> {
	
	private SessionFactory sessionFactory;
	
	private Class<T> entityClass;
	
	RepositoryHibernate(Class<T> entityClass) {
		this.entityClass = entityClass;
	} 
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	Session getSession() {
		return sessionFactory.getCurrentSession();
	}
	
	@Override
	public Optional<T> get(K key) {		
		return Optional.ofNullable((T)getSession().get(entityClass, key));
	}

	@Override
	public void add(T role) {
		getSession().saveOrUpdate(role);
		getSession().flush();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<T> getAll() {		
		List<T> roles = getSession().createQuery("from "+ entityClass.getName()).list();
		return new LinkedHashSet<T>(roles);
	}

	@Override
	public void remove(T role) {
		getSession().delete(role);
		getSession().flush();
	}

}
