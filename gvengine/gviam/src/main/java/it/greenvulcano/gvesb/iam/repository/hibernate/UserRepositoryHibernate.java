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

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import it.greenvulcano.gvesb.iam.domain.User;
import it.greenvulcano.gvesb.iam.repository.UserRepository;

public class UserRepositoryHibernate extends RepositoryHibernate<User, String> implements UserRepository {
	
	public UserRepositoryHibernate() {
		super(User.class);
	}
	
	@Override
	public void setSessionFactory(SessionFactory sessionFactory) {	
		super.setSessionFactory(sessionFactory);
	}	
	
	@Override
	public Optional<User> get(String username) {		
		return Optional.ofNullable((User)getSession().createQuery("from User where userName = :uname")
								  .setString("uname", username)
								  .uniqueResult());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Set<User> find(String fullname, Boolean expired, Boolean enabled, Set<String> roles) {
		Set<User> result = new LinkedHashSet<>();
		
		Criteria criteria = getSession().createCriteria(User.class);
		
		if (fullname!=null && fullname.trim().length()>0) {
			criteria.add(Restrictions.ilike("userInfo.fullname", fullname));
		}
		
		if (expired!=null) {
			criteria.add(Restrictions.eq("expired", expired));
		}
		
		if (enabled!=null) {
			criteria.add(Restrictions.eq("enabled", enabled));
		}
		
		if (roles!=null && !roles.isEmpty()) {
			criteria.createAlias("roles", "role");
			
			roles.stream().map(r -> Restrictions.eq("role.name", r)).forEach(criteria::add);
			
		}
		
		result.addAll(criteria.list());
		
		return result;
	}
}
