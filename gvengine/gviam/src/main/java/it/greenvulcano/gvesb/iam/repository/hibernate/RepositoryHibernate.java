package it.greenvulcano.gvesb.iam.repository.hibernate;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import it.greenvulcano.gvesb.iam.repository.Repository;

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
	
	static class QueryHelper {
		
		private final Map<String, Object> params = new LinkedHashMap<>();
		private final StringBuilder query;
		
		public QueryHelper() {
			query = new StringBuilder();
		}
		
		public QueryHelper(String queryStart) {
			 query = new StringBuilder(queryStart);
		}

		public Map<String, Object> getParams() {
			return params;
		}

		public StringBuilder getQuery() {
			return query;
		}
		
		
	}

}
