package it.greenvulcano.gvesb.osgi.commands;

import java.util.Optional;
import java.util.Scanner;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.util.crypto.CryptoHelper;

@Command(scope = "gvesb", name = "encrypt", description = "Encrypt a string")
@Service
public class GVEncrypt implements Action {
	
	private final Logger LOG = LoggerFactory.getLogger(getClass());

	@Option(name="-s", aliases="--show-input",description="Use this option in you want to see in clear text the input inserted for encryption" )
	private boolean showInput = false;
	
	@Option(name="-k", aliases="--key-id", description=" The ID of key to use for encryption " )
	private String keyId = null;
	
	@Override
	public Object execute() throws Exception {
		String out;
		try {
			
			System.out.println("Insert text to encrypt:");
			Scanner scanner = new Scanner(System.in);
			
			String input =  scanner.next();
			scanner.close();
			
			if (showInput){
				System.out.println("Input clear text: "+input);
			}		
			
			out = CryptoHelper.encrypt(Optional.ofNullable(keyId).orElse(CryptoHelper.DEFAULT_KEY_ID), input, true);
			System.out.print("Encrypted output: ");
		} catch (Exception exc) {
			LOG.error("GVEncrypt - Deploy configuration failed", exc);
			out = "Encrypt fail: "+exc.getMessage();
		}
		return out;

	}

}
