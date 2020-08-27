package it.greenvulcano.gvesb.iam.service.mongodb;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.bson.Document;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;

import it.greenvulcano.gvesb.iam.domain.Property;
import it.greenvulcano.gvesb.iam.domain.mongodb.PropertyBson;
import it.greenvulcano.gvesb.iam.exception.UserNotFoundException;
import it.greenvulcano.gvesb.iam.repository.mongodb.Repository;
import it.greenvulcano.gvesb.iam.service.UsersManager;
import it.greenvulcano.util.InetAddressUtils;

public class GVPropertiesManager implements it.greenvulcano.gvesb.iam.service.PropertyManager {

    private Repository mongodbRepository;

    private UsersManager usersManager;

    public void setMongodbRepository(Repository mongodbRepository) {

        this.mongodbRepository = mongodbRepository;
    }

    public void setUsersManager(UsersManager usersManager) {

        this.usersManager = usersManager;
    }

    @Override
    public void store(String key, String value, String user, String address) throws UserNotFoundException {

        if (Objects.isNull(key) || key.isBlank() || key.length() > 256) {
            throw new IllegalArgumentException(String.format("Invalid value for %s: %s", "key", key));
        }

        if (Objects.isNull(value) || value.isBlank() || value.length() > 512) {
            throw new IllegalArgumentException(String.format("Invalid value for %s: %s", "value", value));
        }

        if (Objects.isNull(address) || !(InetAddressUtils.isIPv4Address(address) || InetAddressUtils.isIPv6Address(address))) {
            throw new IllegalArgumentException(String.format("Invalid value for %s: %s", "address", address));
        }

        PropertyBson property = new PropertyBson(key, value, usersManager.getUser(user).getUsername(), address);

        mongodbRepository.getPropertiesCollection()
                         .updateOne(Filters.eq("key", key),
                                    Updates.combine(Updates.set("value", property.getValue()), Updates.set("username", property.getUsername()),
                                                    Updates.set("origin_address", property.getOriginAddress()), Updates.set("setting_timestamp", property.getSettingTimestamp())),
                                    new UpdateOptions().upsert(true));

    }

    @Override
    public Optional<Property> retrieve(String key) {

        Document result = mongodbRepository.getPropertiesCollection().find(Filters.eq("key", key)).first();

        return Optional.ofNullable(result).map(PropertyBson::new);
    }
    
    @Override
    public Set<Property> retrieveAll() {
        Set<Property> resultset = new LinkedHashSet<>();
        
        mongodbRepository.getPropertiesCollection()
                         .find()
                         .sort(Sorts.ascending("key"))
                         .map(PropertyBson::new)
                         .into(resultset);
        
        return resultset;
    }

    public void delete(String key) {

        mongodbRepository.getPropertiesCollection().deleteOne(Filters.eq("key", key));
    }
}
