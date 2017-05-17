/*******************************************************************************
 * Copyright (c) 2009, 2016 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *  
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
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
