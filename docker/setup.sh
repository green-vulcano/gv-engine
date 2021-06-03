#!/usr/local/openjdk-11/bin/java --source 11

public class GaiaConfigurator {

    public static void main(String[] args) throws java.io.IOException {

       setConfigigurationProperty("org.ops4j.pax.url.mvn.cfg", "org.ops4j.pax.url.mvn.localRepository", System.getenv("MAVEN_REPOSITORY"));
       setConfigigurationProperty("org.ops4j.pax.url.mvn.cfg", "org.ops4j.pax.url.mvn.repositories", "https://mvn.greenvulcano.com/nexus/content/groups/public@id=gv");
       setConfigigurationProperty("org.apache.karaf.features.cfg", "featuresRepositories", "mvn:it.greenvulcano.gvesb/features/"+System.getenv("GAIA_VERSION")+"/xml/features");
       java.util.Optional.ofNullable(System.getenv("GAIA_FEATURES_REPOSITORIES"))
                         .ifPresent(repos -> setConfigigurationProperty("org.apache.karaf.features.cfg", "featuresRepositories", repos));
       setConfigigurationProperty("org.apache.karaf.features.cfg", "featuresBoot", System.getenv("GAIA_FEATURES"));
    }

    private static void setConfigigurationProperty(String configFileName, String key, String ... values)  {

       try {
           var configPath = java.nio.file.Paths.get(System.getenv("KARAF_ETC"), configFileName);

           var config = new java.util.Properties();
           config.load(java.nio.file.Files.newInputStream(configPath));

           if (config.containsKey(key)) {

           config.put(key, config.getProperty(key)
                                 .concat(",")
                                 .concat(java.util.stream.Stream.of(values).collect(java.util.stream.Collectors.joining(","))));
           } else {
               config.put(key, java.util.stream.Stream.of(values).collect(java.util.stream.Collectors.joining(",")));
           }

           config.store(java.nio.file.Files.newOutputStream(configPath), "GAIA Configuration");
      } catch (Exception e) {
          e.printStackTrace();
      }
    }

}
