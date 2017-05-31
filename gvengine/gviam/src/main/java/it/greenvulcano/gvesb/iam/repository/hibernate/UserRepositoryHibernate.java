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
import java.util.stream.Collectors;

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
	
	@Override
	public Set<User> find(Map<Parameter, Object> parameters, LinkedHashMap<Parameter, Order> order, int firstResult, int maxResult) {
		Set<User> result = new LinkedHashSet<>();
				
		QueryHelper helper = buildQueryHelper(false, parameters, order);
		
		@SuppressWarnings("unchecked")
		Query<User> q = getSession().createQuery(helper.getQuery().toString());
		helper.getParams().entrySet().forEach(p-> q.setParameter(p.getKey(), p.getValue()));
		
		q.setFirstResult(firstResult);
		q.setMaxResults(maxResult);	
		
		result.addAll(q.getResultList());
		
		return result;
	}

	@Override
	public int count(Map<Parameter, Object> parameters) {		
		QueryHelper helper = buildQueryHelper(true, parameters, new LinkedHashMap<>());
		
		@SuppressWarnings("unchecked")
		Query<Long> q = getSession().createQuery(helper.getQuery().toString());
		helper.getParams().entrySet().forEach(p-> q.setParameter(p.getKey(), p.getValue()));
		
		return q.uniqueResult().intValue();
	}
	
	private QueryHelper buildQueryHelper(boolean countOnly, Map<Parameter, Object> parameters, LinkedHashMap<Parameter, Order> order){
		
		QueryHelper helper = countOnly ? new QueryHelper("select count(u.id) from User u ") : new QueryHelper("from User u ") ;
		
		if (parameters!=null) {			
			Optional<Object> role = Optional.ofNullable(parameters.get(Parameter.role));			
			if (role.isPresent()) {
				helper.getQuery().append("join u.roles role where role.name = :_role ");
				helper.getParams().put("_role", role.get().toString());
			} else {
				helper.getQuery().append("where u.id is not null ");
			}
			
			Optional.ofNullable((String)parameters.get(Parameter.username)).ifPresent(username-> {
				helper.getQuery().append("and u.username like :_username ");
				if (username!=null) username = username.replaceAll("\\*","%");
				helper.getParams().put("_username", username);
			});
			
			Optional.ofNullable((String)parameters.get(Parameter.fullname)).ifPresent(fullname-> {
				helper.getQuery().append("and u.userInfo.fullName like :_fullname ");
				if (fullname!=null) fullname = fullname.replaceAll("\\*","%");
				helper.getParams().put("_fullname", fullname);
			});
			
			Optional.ofNullable((String)parameters.get(Parameter.email)).ifPresent(email-> {
				helper.getQuery().append("and u.userInfo.email like :_email ");
				if (email!=null) email = email.replaceAll("\\*","%");
				helper.getParams().put("_email", email);			
			});
			
			Optional.ofNullable(parameters.get(Parameter.expired)).ifPresent(expired-> {
				helper.getQuery().append("and u.expired = :_expired ");
				helper.getParams().put("_expired", Boolean.valueOf(expired.toString()));			
			});
			
			Optional.ofNullable(parameters.get(Parameter.enabled)).ifPresent(enabled -> {
				helper.getQuery().append("and u.enabled = :_enabled ");
				helper.getParams().put("_enabled", Boolean.valueOf(enabled.toString()));
			});
		}
	
		if (order!=null && !order.isEmpty()) {			
			String orderBy = order.entrySet().stream()					               
					               .map(e -> e.getKey().name()+ " " +Optional.ofNullable(e.getValue()).orElse(Order.desc).name())
					               .collect(Collectors.joining(",", "order by ", ""));
			helper.getQuery().append(orderBy);
		}
			
		return helper;
	}
}
