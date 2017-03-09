package it.greenvulcano.gvesb.osgi.commands;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.util.crypto.CryptoHelper;
import it.greenvulcano.util.crypto.CryptoUtils;
import it.greenvulcano.util.crypto.KeyID;
import it.greenvulcano.util.crypto.KeyStoreID;
import it.greenvulcano.util.crypto.KeyStoreUtils;

@Command(scope = "gvesb", name = "keygen", description = "Generate the root keystore")
@Service
public class GVKeyGen implements Action {
	
	private final static Logger LOG = LoggerFactory.getLogger(GVKeyGen.class);
	
	@Override
	public Object execute() throws Exception {
		StringBuilder message = new StringBuilder();
		
		Path destination = Paths.get(XMLConfig.getBaseConfigPath()).getParent();
				
		if (Files.exists(destination) && Files.isDirectory(destination) && Files.isWritable(destination)) {
		
			try {
			
			KeyStoreID defaultKeyStoreID = new KeyStoreID(CryptoHelper.DEFAULT_KEYSTORE_ID, 
												   KeyStoreUtils.DEFAULT_KEYSTORE_TYPE, 
												   CryptoHelper.DEFAULT_KEY_STORE_NAME, 
												   CryptoHelper.SECRET_KEY_STORE_PWD,										   
												   KeyStoreUtils.DEFAULT_KEYSTORE_PROVIDER);
			
			KeyID defaultKeyid = new KeyID(CryptoHelper.DEFAULT_KEY_ID, CryptoUtils.TRIPLE_DES_TYPE, defaultKeyStoreID, CryptoHelper.SECRET_KEY_NAME, CryptoHelper.SECRET_KEY_PWD);
						
			SecretKey secretKey = CryptoUtils.generateSecretKey(CryptoUtils.TRIPLE_DES_TYPE, CryptoHelper.SECRET_KEY_PWD.getBytes());
						
			KeyStoreUtils.writeKey(destination.toAbsolutePath().toString(), defaultKeyid, secretKey, null);
			
			Path keystore = destination.resolve(CryptoHelper.DEFAULT_KEY_STORE_NAME);
			
			if (Files.exists(keystore)) {
			
				message.append("Created root keystore ")
				       .append(keystore.toAbsolutePath().toString())
				       .append('\n')
				       .append("keep it in a safe place out of reach of hackers");
			} else {
				throw new FileNotFoundException("Fail to create root keystore file "+keystore.toAbsolutePath().toString());
			}
			
			} catch (Exception e) {
				LOG.error("Error creating root keystore", e);
				
				String type = Optional.ofNullable(e.getCause()).orElse(e).getClass().getName();
				String error = Optional.ofNullable(e.getCause()).orElse(e).getMessage();
				
				message.append("Root keystore creation error: ")
				       .append(type)
				       .append(' ')
				       .append(error);
			}
		} else {
			message.append("Configuration path unreacheable "+destination.toAbsolutePath());
		}
		
		return message;
	}

}
