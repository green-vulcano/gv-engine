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
package it.greenvulcano.gvesb.iam.repository.hibernate;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import it.greenvulcano.gvesb.iam.domain.User;
import it.greenvulcano.gvesb.iam.repository.UserRepository;

/**
 * 
 * {@link UserRepository} implementation using Hibernate ORM framework,
 * expects injection of a {@link SessionFactory}  
 * 
 */
public class UserRepositoryHibernate extends RepositoryHibernate<User, Integer> implements UserRepository {
	
	public UserRepositoryHibernate() {
		super(User.class);
	}
	
	@Override
	public void setSessionFactory(SessionFactory sessionFactory) {	
		super.setSessionFactory(sessionFactory);
	}	
	
	@Override
	public Optional<User> get(String username) {		
		return Optional.ofNullable((User)getSession().createQuery("from User where username = :uname")
								  .setParameter("uname", username)
								  .uniqueResult());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Set<User> find(String fullname, String email, Boolean expired, Boolean enabled, String role) {
		Set<User> result = new LinkedHashSet<>();
		
		
		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder query = new StringBuilder("from User u ");

		if (role!=null && role.trim().length()>0) {
			query.append("join u.roles role where role.name = :_role ");
			params.put("_role", fullname);	
		} else {
			query.append("where u.id is not null ");			
		}
		
		if (fullname!=null && fullname.trim().length()>0) {
			query.append("and u.userInfo.fullName like :_fullname ");
			params.put("_fullname", fullname+"%");			
		}
		
		if (email!=null && email.trim().length()>0) {
			query.append("and u.userInfo.email = :_email ");
			params.put("_email", email);			
		}
		
		if (expired!=null) {
			query.append("and u.expired = :expired ");
			params.put("_expired", expired);			
		}
		
		if (enabled!=null) {
			query.append("and u.enabled = :enabled ");
			params.put("_enabled", enabled);
		}
		
		Query<User> q = getSession().createQuery(query.toString());		
		params.entrySet().forEach(p-> q.setParameter(p.getKey(), p.getValue()));
		
		result.addAll(q.getResultList());
		
		return result;
	}
}
