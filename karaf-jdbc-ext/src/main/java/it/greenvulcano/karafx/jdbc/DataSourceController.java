package it.greenvulcano.karafx.jdbc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

@CrossOriginResourceSharing(allowAllOrigins=true, allowCredentials=true, exposeHeaders={"Content-Type", "Content-Range", "X-Auth-Status"})
public class DataSourceController {

    private static final Logger LOG = LogManager.getLogger(DataSourceController.class);

    private final static String JNDI_PREFIX = "osgi:service/";
    private final static java.nio.file.Path ETC_PATH = Paths.get(System.getProperty("karaf.base"), "etc");
    private final static String CONFIG_FILENAME = "org.ops4j.datasource-%s.cfg";
    private final static Map<String, String> KNOWN_DRIVERS = new LinkedHashMap<>();
    private final static Map<String, String> VALIDATION_QUERIES = new LinkedHashMap<>();

    static {
        KNOWN_DRIVERS.put("h2", "H2");
        KNOWN_DRIVERS.put("mysql", "mysql");
        KNOWN_DRIVERS.put("oracle", "oracle");
        KNOWN_DRIVERS.put("postgresql", "PostgreSQL JDBC Driver");

        VALIDATION_QUERIES.put("postgresql", "SELECT 1");
        VALIDATION_QUERIES.put("h2", "SELECT 1 FROM DUAL");
        VALIDATION_QUERIES.put("mysql", "SELECT 1 FROM DUAL");
        VALIDATION_QUERIES.put("oracle", "SELECT 1 FROM DUAL");
    }

    @POST @RolesAllowed({"gvadmin", "admin"})
    @Path("/datasource")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(String configuration) {

        Response response = null;
        java.nio.file.Path dspath = null;
        Properties connectionProperties = new Properties();

        try {

            LOG.debug("Start creating datasource using config: " + configuration);

            JSONObject config = new JSONObject(configuration);

            String dataSourceName = config.optString("name", UUID.randomUUID().toString());
            LOG.debug("Datasource name: " + dataSourceName);
            connectionProperties.put("dataSourceName", dataSourceName);

            String driverKey = config.optString("driver", "missing").toLowerCase();
            if (KNOWN_DRIVERS.containsKey(driverKey)) {
                connectionProperties.put("osgi.jdbc.driver.name", KNOWN_DRIVERS.get(driverKey));
            } else {
                throw new IllegalArgumentException("Invalid driver: " + driverKey);
            }

            String url = Optional.ofNullable(config.optString("url")).orElseThrow(() -> new IllegalArgumentException("Missing required value: url"));
            connectionProperties.put("url", url);

            Optional.ofNullable(config.optString("user")).ifPresent(u -> connectionProperties.put("user", u));
            Optional.ofNullable(config.optString("password")).ifPresent(u -> connectionProperties.put("password", u));

            connectionProperties.put("pool", "dbcp2");
            connectionProperties.put("jdbc.pool.maxIdle", Integer.toString(2));
            connectionProperties.put("jdbc.pool.minIdle", Integer.toString(1));
            connectionProperties.put("jdbc.pool.maxTotal", Integer.toString(16));
            connectionProperties.put("jdbc.pool.testOnBorrow", Boolean.toString(true));
            connectionProperties.put("jdbc.pool.testOnCreate", Boolean.toString(true));
            connectionProperties.put("jdbc.factory.validationQuery", VALIDATION_QUERIES.get(driverKey));

            LOG.debug("Creating datasource config file in path: " + ETC_PATH);
            dspath = ETC_PATH.resolve(String.format(CONFIG_FILENAME, dataSourceName));
            connectionProperties.store(Files.newOutputStream(dspath, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),null);

            Thread.sleep(3000);

            String jndiname = JNDI_PREFIX + dataSourceName;
            String testResult = testConnection(jndiname);
            LOG.debug("Datasource test result (database product name): " + testResult);
            JSONObject reply = new JSONObject().put("message", "ready").put("id", dataSourceName).put("resource", jndiname);
            response = Response.ok(reply.toString(), MediaType.APPLICATION_JSON).build();

        } catch (JSONException | IllegalArgumentException formatException) {
            LOG.debug("Invalid payload ", formatException);
            JSONObject reply = new JSONObject().put("message", formatException.getMessage());
            response = Response.status(Status.BAD_REQUEST).entity(reply.toString()).type(MediaType.APPLICATION_JSON).build();

        } catch (Exception e) {
            LOG.error("Fail to create datasource ", e);

            Optional.ofNullable(dspath).ifPresent(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException e1) {
                   
                }
            });
            
            JSONObject reply = new JSONObject().put("message", e.getMessage());
            response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(reply.toString()).type(MediaType.APPLICATION_JSON).build();

        }

        return response;
    }

    @DELETE @RolesAllowed({"gvadmin", "admin"})
    @Path("/datasource/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public void delete(@PathParam("name") String name) {

        try {
            LOG.debug("Deleting datasource " + name);
            java.nio.file.Path configPath = ETC_PATH.resolve(String.format(CONFIG_FILENAME, name));
            if (!Files.deleteIfExists(configPath)) {
                LOG.debug("Datasource " + name+" is missing");
                throw new WebApplicationException(Status.NOT_FOUND);
            }

        } catch (IOException e) {
            LOG.error("Fail to delete datasource ", e);
            throw new WebApplicationException(e.getMessage(), Status.INTERNAL_SERVER_ERROR);
        }
    }

    private String testConnection(String jndiname) throws NamingException, SQLException {

        Context context = new InitialContext();

        DataSource dataSource = (DataSource) context.lookup(jndiname);

        try (Connection connection = dataSource.getConnection()) {
            return connection.getMetaData().getDatabaseProductName();
        }

    }

}
