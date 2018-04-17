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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import it.greenvulcano.gvesb.gviamx.domain.UserActionRequest;
import it.greenvulcano.gvesb.gviamx.repository.UserActionRepository;

public class UserActionRepositoryHibernate implements UserActionRepository {
	
	private SessionFactory sessionFactory;
		
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	Session getSession() {
		return sessionFactory.getCurrentSession();
	}
	
	@Override
	public Optional<UserActionRequest> get(Long key) {		
		return Optional.ofNullable(getSession().get(UserActionRequest.class, key));
	}

	@Override
	public void add(UserActionRequest userActionRequest) {
		getSession().saveOrUpdate(userActionRequest);
		getSession().flush();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<UserActionRequest> getAll() {
		List<UserActionRequest> userActions = getSession().createQuery("from "+ UserActionRequest.class.getName()).list();
		return new LinkedHashSet<UserActionRequest>(userActions);
	}

	@Override
	public void remove(UserActionRequest userActionRequest) {
		getSession().delete(userActionRequest);
		getSession().flush();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends UserActionRequest> Optional<T> get(String email, Class<T> type) {		
		Query<T> query = getSession().createQuery("from "+type.getName() + " a where a.email = :_email");		
		return query.setParameter("_email", email).uniqueResultOptional();
	}

}
