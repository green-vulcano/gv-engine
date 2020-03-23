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
package it.greenvulcano.gvesb.iam.service.mongodb;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import org.apache.commons.codec.digest.DigestUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoCommandException;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;

import it.greenvulcano.gvesb.iam.domain.Role;
import it.greenvulcano.gvesb.iam.domain.User;
import it.greenvulcano.gvesb.iam.domain.UserInfo;
import it.greenvulcano.gvesb.iam.domain.mongodb.RoleBSON;
import it.greenvulcano.gvesb.iam.domain.mongodb.UserBSON;
import it.greenvulcano.gvesb.iam.exception.GVSecurityException;
import it.greenvulcano.gvesb.iam.exception.InvalidPasswordException;
import it.greenvulcano.gvesb.iam.exception.InvalidRoleException;
import it.greenvulcano.gvesb.iam.exception.InvalidUsernameException;
import it.greenvulcano.gvesb.iam.exception.PasswordMissmatchException;
import it.greenvulcano.gvesb.iam.exception.UnverifiableUserException;
import it.greenvulcano.gvesb.iam.exception.UserExistException;
import it.greenvulcano.gvesb.iam.exception.UserExpiredException;
import it.greenvulcano.gvesb.iam.exception.UserNotFoundException;
import it.greenvulcano.gvesb.iam.service.SearchCriteria;
import it.greenvulcano.gvesb.iam.service.SearchResult;
import it.greenvulcano.gvesb.iam.service.UsersManager;

public class GVUsersManager implements UsersManager {

    final Logger logger = LoggerFactory.getLogger(getClass());
    private MongoClient mongoClient;
    private String databaseName;
    
    
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }
    
    
    public void setDatabaseName(String gviamDatabaseName) {
        this.databaseName = gviamDatabaseName;
    }

    @Override
    public User createUser(String username, String password) throws InvalidUsernameException, InvalidPasswordException, UserExistException {

        if (!username.matches(User.USERNAME_PATTERN)) {
            throwException(new InvalidUsernameException());
        }
            
        if (!password.matches(User.PASSWORD_PATTERN)) {
            throwException(new InvalidPasswordException());
        }
            

        UserBSON user = UserBSON.newUser(username, DigestUtils.sha256Hex(password), false, true, null);

        try {
            mongoClient.getDatabase(databaseName)
                       .getCollection(UserBSON.COLLECTION_NAME)
                       .insertOne(Document.parse(user.toString()));

        } catch (MongoCommandException constraintViolationException) {
            throwException(new UserExistException(username));
        }

        return user;
    }

    @Override
    public Role createRole(String name, String description) throws InvalidRoleException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateUser(String username, UserInfo userInfo, Set<Role> grantedRoles, boolean enabled, boolean expired) throws UserNotFoundException, InvalidRoleException {

        UserBSON user = (UserBSON) getUser(username);
        
        AtomicInteger version = new AtomicInteger(user.getVersion());
        int originalVersion = version.get();
        
        user.setUserInfo(userInfo);
        user.setEnabled(enabled);
        user.setExpired(expired);
        user.clearRoles();
        user.setUpdateTime(new Date());
        user.setVersion(version.incrementAndGet());
        
        if (grantedRoles != null) {

            Predicate<Role> roleIsValid = role -> Optional.ofNullable(role.getName()).orElse("").matches(Role.ROLE_PATTERN);

            Optional<Role> notValidRole = grantedRoles.stream().filter(roleIsValid.negate()).findAny();
            if (notValidRole.isPresent()) {
                throw new InvalidRoleException(notValidRole.get().getName());
            }
        }         

        UpdateResult result = mongoClient.getDatabase(databaseName)
                                         .getCollection(UserBSON.COLLECTION_NAME)
                                         .updateOne( Filters.and(Filters.eq("username", username), Filters.eq("version", originalVersion)), Document.parse(user.toString()));

        if ( result.getModifiedCount()== 0) {
             new ConcurrentModificationException("Persistence state changed in the meanwhile");
        }
      
    }

    @Override
    public User getUser(Long id) throws UserNotFoundException {

        Document user = mongoClient.getDatabase(databaseName)
                                   .getCollection(UserBSON.COLLECTION_NAME)
                                   .find(Filters.eq("userid", id)).first();

        return Optional.ofNullable(user)
                       .map(UserBSON::new)
                       .orElseThrow(() -> new UserNotFoundException(id.toString()));
    }

    @Override
    public User getUser(String username) throws UserNotFoundException {

        Document user = mongoClient.getDatabase(databaseName)
                                   .getCollection(UserBSON.COLLECTION_NAME)
                                   .find(Filters.eq("username", username)).first();

        return Optional.ofNullable(user)
                       .map(UserBSON::new)
                       .orElseThrow(() -> new UserNotFoundException(username));
    }

    @Override
    public void deleteUser(String username) {

        mongoClient.getDatabase(databaseName)
                   .getCollection(UserBSON.COLLECTION_NAME)
                   .findOneAndDelete(Filters.eq("username", username));
                   
    }

    @Override
    public User resetUserPassword(String username, String defaultPassword) throws UserNotFoundException, InvalidPasswordException, UnverifiableUserException {

        UserBSON user = (UserBSON) getUser(username);
        
        AtomicInteger version = new AtomicInteger(user.getVersion());
        int originalVersion = version.get();

        if (!Objects.requireNonNull(defaultPassword, "A default password is required").matches(User.PASSWORD_PATTERN))
            throw new InvalidPasswordException();
        if (!user.getPassword().isPresent())
            throw new UnverifiableUserException(username);

        user.setPassword(DigestUtils.sha256Hex(defaultPassword));
        
        Date editTime = new Date();
        user.setPasswordTime(editTime);
        user.setUpdateTime(editTime);
        user.setExpired(true);
        user.setVersion(version.incrementAndGet());
        
        UpdateResult result = mongoClient.getDatabase(databaseName)
                .getCollection(UserBSON.COLLECTION_NAME)
                .updateOne( Filters.and(Filters.eq("username", username), Filters.eq("version", originalVersion)), Document.parse(user.toString()));

        if ( result.getModifiedCount()== 0) {
            new ConcurrentModificationException("Persistence state changed in the meanwhile");
        }

        return user;

    }

    @Override
    public User changeUserPassword(String username, String oldPassword, String newPassword)
            throws UserNotFoundException, PasswordMissmatchException, InvalidPasswordException, UnverifiableUserException {

        UserBSON user = (UserBSON) getUser(username);
        
        AtomicInteger version = new AtomicInteger(user.getVersion());
        int originalVersion = version.get();
        
        if (!user.getPassword().isPresent()) {
            throw new UnverifiableUserException(username);
        }

        if (!DigestUtils.sha256Hex(oldPassword).equals(user.getPassword().get())) {
            throw new PasswordMissmatchException(username);
        }
        
        if (!newPassword.matches(User.PASSWORD_PATTERN)) {
            throw new InvalidPasswordException();
        }
        
        Date editTime = new Date();
        user.setPassword(DigestUtils.sha256Hex(newPassword));
        user.setPasswordTime(editTime);
        user.setUpdateTime(editTime);
        user.setExpired(false);
        user.setVersion(version.incrementAndGet());

        UpdateResult result = mongoClient.getDatabase(databaseName)
                .getCollection(UserBSON.COLLECTION_NAME)
                .updateOne( Filters.and(Filters.eq("username", username), Filters.eq("version", originalVersion)), Document.parse(user.toString()));

        if ( result.getModifiedCount()== 0) {
            new ConcurrentModificationException("Persistence state changed in the meanwhile");
        }

        return user;
    }

    @Override
    public User validateUser(String username, String password) throws UserNotFoundException, PasswordMissmatchException, UserExpiredException, UnverifiableUserException {

        User user = getUser(username);
        if (user.getPassword().orElseThrow(() -> new UnverifiableUserException(username)).equals(DigestUtils.sha256Hex(password))) {
            if (user.isExpired()) {
                throw new UserExpiredException(username);
            } else if (!user.isEnabled()) {
                throw new UserNotFoundException(username);
            }
        } else {
            throw new PasswordMissmatchException(username);
        }

        return user;

    }

    @Override
    public void deleteRole(String roleName) {

        mongoClient.getDatabase(databaseName)
                   .getCollection(UserBSON.COLLECTION_NAME)
                   .updateMany(new Document(), Updates.pull("roles", Filters.eq("name", roleName)));

    }

    @Override
    public Set<Role> getRoles() {

        Set<Role> roles = new HashSet<>();
        
        mongoClient.getDatabase(databaseName)
                   .getCollection(UserBSON.COLLECTION_NAME)
                   .aggregate(Arrays.asList(Aggregates.unwind("$roles"), 
                                            Aggregates.project(Projections.fields(Projections.excludeId(), Projections.include("roles"))), 
                                            Aggregates.group("$roles.name", Accumulators.first("role", "$roles")),
                                            Aggregates.replaceRoot("role")))
                   .iterator()
                   .forEachRemaining(d -> {                   
                       roles.add( new RoleBSON(d.getString("name"), d.getString("description")) );
                       
                   });
         
        
        return roles;
    }

    @Override
    public Role getRole(String name) {


        mongoClient.getDatabase(databaseName)
                   .getCollection(UserBSON.COLLECTION_NAME)
                   .aggregate(Arrays.asList(Aggregates.match(Filters.eq("roles.name", name)),
                                            Aggregates.unwind("$roles"), 
                                            Aggregates.project(Projections.fields(Projections.excludeId(), Projections.include("roles"))), 
                                            Aggregates.group("$roles.name", Accumulators.first("role", "$roles")),
                                            Aggregates.replaceRoot("role")))
                   .first();
        
        return null;
    }

    public Set<User> getUsers() {

        Set<User> users = new LinkedHashSet<>(); 

        mongoClient.getDatabase(databaseName)
                   .getCollection(UserBSON.COLLECTION_NAME)
                   .find()
                   .sort(Sorts.ascending("username"))
                   .iterator()
                   .forEachRemaining(d ->  users.add(new UserBSON(d)));
        
        return users;
    }

    @Override
    public SearchResult searchUsers(SearchCriteria criteria) {

        SearchResult result = new SearchResult();

        List<Bson> filters = new LinkedList<>();
        
        Optional.ofNullable(criteria.getParameters().get("username")).ifPresent(username-> filters.add(Filters.eq("username", username)) );
        Optional.ofNullable(criteria.getParameters().get("enabled")).ifPresent(enabled-> filters.add(Filters.eq("enabled", enabled)) );
        Optional.ofNullable(criteria.getParameters().get("expired")).ifPresent(expired-> filters.add(Filters.eq("expired", expired)) );
        
        
        List<Bson> pipeline = new LinkedList<>();
        
        //  match stage
        if (!filters.isEmpty()) {
            pipeline.add(Aggregates.match(Filters.and(filters)));
        }
        
        pipeline.add(Aggregates.sort(Sorts.orderBy(Sorts.ascending("username"))));
        
        pipeline.add(Aggregates.skip(criteria.getOffset()));
        pipeline.add(Aggregates.limit(criteria.getLimit()));
        
        result.setTotalCount(criteria.getLimit());
        
        Set<User> resultset = new LinkedHashSet<>();
        
        mongoClient.getDatabase(databaseName)
                   .getCollection(UserBSON.COLLECTION_NAME)
                   .aggregate(pipeline)
                   .iterator()
                   .forEachRemaining(d ->  resultset.add(new UserBSON(d)));
        
        
        result.setFounds(resultset);
        

        return result;
    }

    @Override
    public User enableUser(String username, boolean enable) throws UserNotFoundException {

        User user = getUser(username);
        try {
            
            user.setEnabled(enable);
            updateUser(username, user.getUserInfo(), user.getRoles(), user.isEnabled(), user.isExpired());
    
        } catch (UserNotFoundException | InvalidRoleException e) {
            throwException(e);
        }
        
        return user;
    }

    @Override
    public User setUserExpiration(String username, boolean expired) throws UserNotFoundException {

        User user = getUser(username);
        try {
            
            user.setExpired(expired);
            updateUser(username, user.getUserInfo(), user.getRoles(), user.isEnabled(), user.isExpired());
    
        } catch (UserNotFoundException | InvalidRoleException e) {
            throwException(e);
        }

        return user;
    }

    @Override
    public User switchUserStatus(String username) throws UserNotFoundException {
        User user = getUser(username);
        try {
            
            user.setEnabled(!user.isEnabled());
            updateUser(username, user.getUserInfo(), user.getRoles(), user.isEnabled(), user.isExpired());
    
        } catch (UserNotFoundException | InvalidRoleException e) {
            throwException(e);
        }        

        return user;
    }

    @Override
    public void checkManagementRequirements() {

        long admins = mongoClient.getDatabase(databaseName)
                               .getCollection(UserBSON.COLLECTION_NAME)
                               .countDocuments(Filters.and(Filters.eq("roles.name", Authority.ADMINISTRATOR), Filters.eq("enabled", Boolean.TRUE)));
                   

        /**
         * Adding default user 'gvadmin' if no present
         */
        if (admins == 0) {
            logger.info("Creating a default 'gvadmin'");
            
            try {
                createUser("gvadmin", "gvadmin");
            } catch (SecurityException | InvalidUsernameException | InvalidPasswordException | UserExistException e) {
                logger.info("A user named 'gvadmin' exist: restoring his default settings", e);

                try {
                    resetUserPassword("gvadmin", "gvadmin");
                    changeUserPassword("gvadmin", "gvadmin", "gvadmin");
                } catch (UserNotFoundException | InvalidPasswordException | UnverifiableUserException | PasswordMissmatchException e1) {
                    logger.error("Failed to restore credentials for the default user 'gvadmin'", e);
                }                
                

            }

            Set<Role> roles = new HashSet<>();
            roles.add(new RoleBSON(Authority.ADMINISTRATOR, "Created by GV"));

            // roles required to use karaf
            roles.add(new RoleBSON("admin", "Created by GV"));
            roles.add(new RoleBSON("manager", "Created by GV"));
            roles.add(new RoleBSON("viewer", "Created by GV"));
            roles.add(new RoleBSON("systembundles", "Created by GV"));
            roles.add(new RoleBSON("ssh", "Created by GV"));

            try {
                updateUser("gvadmin", null, roles, true, false);
            } catch (UserNotFoundException | InvalidRoleException e) {
                logger.error("Failed to restore settings for the default user 'gvadmin'", e);
            }
        }

    }

    @Override
    public void updateUsername(String username, String newUsername) throws UserNotFoundException, InvalidUsernameException {

        UserBSON user = (UserBSON) getUser(username);
        if (username.matches(User.USERNAME_PATTERN)) {
            AtomicInteger version = new AtomicInteger(user.getVersion());
            int originalVersion = version.get();
            
            user.setUsername(newUsername);
            user.setUpdateTime(new Date());
            user.setVersion(version.incrementAndGet());
                
            UpdateResult result = mongoClient.getDatabase(databaseName)
                                             .getCollection(UserBSON.COLLECTION_NAME)
                                             .updateOne( Filters.and(Filters.eq("username", username), Filters.eq("version", originalVersion)), Document.parse(user.toString()));

            if ( result.getModifiedCount()== 0) {
                 new ConcurrentModificationException("Persistence state changed in the meanwhile");
            }
               
               
        } else {
            throw new InvalidUsernameException();
        }

    }
    
    @Override
    public void revokeRole(String username, String role) throws UserNotFoundException {

        try {
            UserBSON user = (UserBSON) getUser(username);
            user.removeRole(role);
    
            updateUser(username, user.getUserInfo(), user.getRoles(), user.isEnabled(), user.isExpired());
    
        } catch (UserNotFoundException | InvalidRoleException e) {
            throwException(e);
        }
    }
    

    @Override
    public void addRole(String username, String rolename) {

        if (!rolename.matches(Role.ROLE_PATTERN))
            throwException(new InvalidRoleException(rolename));
        
        try {
            UserBSON user = (UserBSON) getUser(username);

            Role role = Optional.ofNullable(getRole(rolename)).orElse(new RoleBSON(rolename, "Created by JAAS"));
            user.addRole(role);
    
            updateUser(username, user.getUserInfo(), user.getRoles(), user.isEnabled(), user.isExpired());
    
        } catch (UserNotFoundException | InvalidRoleException e) {
            throwException(e);
        } 

    }
    
    private void throwException(GVSecurityException cause) {
        throw new SecurityException(cause);
    }

}