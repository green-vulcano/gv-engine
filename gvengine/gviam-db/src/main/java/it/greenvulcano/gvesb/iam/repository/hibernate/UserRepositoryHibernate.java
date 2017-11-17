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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import it.greenvulcano.gvesb.iam.domain.User;
import it.greenvulcano.gvesb.iam.domain.jpa.UserJPA;

/**
 * 
 * {@link UserRepository} implementation using Hibernate ORM framework,
 * expects injection of a {@link SessionFactory}  
 * 
 */
public class UserRepositoryHibernate extends RepositoryHibernate {
	
	public enum Parameter {
		username, fullname, email, expired, creationTime, updateTime, passwordTime, enabled, role;
		
		public static Parameter get(String v) {
			try {
				return valueOf(v);
			} catch (Exception e) {
				return null;
			}
		}
	}
	
	public enum Order {desc, asc;
	
		public static Order get(String v) {
			try {
				return valueOf(v);
			} catch (Exception e) {
				return null;
			}
		}
	}	
		
	public void setSessionFactory(SessionFactory sessionFactory) {	
		super.setSessionFactory(sessionFactory);
	}
			
	public Optional<User> get(String username) {		
		return Optional.ofNullable((UserJPA)getSession().createQuery("from UserJPA where username = :uname")
								  .setParameter("uname", username)
								  .uniqueResult());
	}
	
	public Optional<User> get(Long key) {		
		return Optional.ofNullable(getSession().get(UserJPA.class, key));
	}

	public void add(User entity) {
		getSession().saveOrUpdate(UserJPA.class.cast(entity));
		getSession().flush();		
		
	}

	@SuppressWarnings("unchecked")
	public Set<User> getAll() {		
		List<User> users = getSession().createQuery("from UserJPA").list();
		return new LinkedHashSet<User>(users);
	}

	public void remove(User entity) {
		getSession().delete(UserJPA.class.cast(entity));
		getSession().flush();
	}
	
	public Set<User> find(Map<Parameter, Object> parameters, LinkedHashMap<Parameter, Order> order, int firstResult, int maxResult) {
		Set<User> result = new LinkedHashSet<>();
				
		QueryHelper helper = buildQueryHelper(false, parameters, order);
		
		@SuppressWarnings("unchecked")
		Query<UserJPA> q = getSession().createQuery(helper.getQuery().toString());
		helper.getParams().entrySet().forEach(p-> q.setParameter(p.getKey(), p.getValue()));
		
		q.setFirstResult(firstResult);
		q.setMaxResults(maxResult);	
		
		result.addAll(q.getResultList());
		
		return result;
	}

	public int count(Map<Parameter, Object> parameters) {		
		QueryHelper helper = buildQueryHelper(true, parameters, new LinkedHashMap<>());
		
		@SuppressWarnings("unchecked")
		Query<Long> q = getSession().createQuery(helper.getQuery().toString());
		helper.getParams().entrySet().forEach(p-> q.setParameter(p.getKey(), p.getValue()));
		
		return q.uniqueResult().intValue();
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
	
	private QueryHelper buildQueryHelper(boolean countOnly, Map<Parameter, Object> parameters, LinkedHashMap<Parameter, Order> order){
		
		QueryHelper helper = countOnly ? new QueryHelper("select count(u.id) from UserJPA u ") : new QueryHelper("select distinct u from UserJPA u ") ;
		
		if (parameters!=null) {			
			Optional<Object> role = Optional.ofNullable(parameters.get(Parameter.role));			
			if (role.isPresent()) {
				helper.getQuery().append("join u.roles r where r.name = :_role ");
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
				helper.getQuery().append("and u.userInfo.fullname like :_fullname ");
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
	
		order.remove(Parameter.role);
		if (order!=null && !order.isEmpty()) {			
			String orderBy = order.entrySet().stream()				                  
					               .map(e -> {
					            	   
					            	   String field;
					            	   switch (e.getKey()) {
											case email:										
												field = "u.userInfo.email";
											    break;
											    
											case fullname:
												field = "u.userInfo.fullname";
												break;
								
										    default:
										    	field = "u."+e.getKey().name();
										    	break;
										    	
											}
					            	   
					            	   return field+ " " +Optional.ofNullable(e.getValue()).orElse(Order.desc).name();
					            	
					                })
					               .collect(Collectors.joining(",", "order by ", ""));
			helper.getQuery().append(orderBy);
		}
			
		return helper;
	}
}
