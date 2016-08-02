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
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import it.greenvulcano.gvesb.iam.domain.User;
import it.greenvulcano.gvesb.iam.repository.UserRepository;

public class UserRepositoryHibernate implements UserRepository {
	
	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	private Session getSession() {
		return sessionFactory.getCurrentSession();
	}

	@Override
	public Optional<User> get(String username) {		
		return Optional.ofNullable((User)getSession().createQuery("from User where userName = :uname")
								  .setString("uname", username)
								  .uniqueResult());
	}

	@Override
	public void add(User user) {
		getSession().saveOrUpdate(user);
		getSession().flush();

	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<User> getAll() {		
		List<User> users = getSession().createQuery("from User").list();
		return new LinkedHashSet<>(users);
	}

	@Override
	public void remove(User user) {
		getSession().delete(user);
		getSession().flush();
	}

}
