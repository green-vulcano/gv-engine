package tests.unit.gvesb.gvcore;

import java.util.Base64;

import junit.framework.TestCase;

public class LogoTest extends TestCase {
	
	
	public void testLogo(){
		
		String logo = "    .-------."+"\n"+
				"    |       |"+"\n"+
				" -=___________=-"+"\n"+
				"   ____   ____"+"\n"+
				"  |____)=(____|"+"\n"+
				"\n"+
				"       ###"+"\n"+
				"      # = #"+"\n"+
				"      #####"+"\n"+
				"       ###"+"\n"+
				"\n"+
				"    GV ESB v4"+"\n";
		
		System.out.println(logo);
		
		String decoded = Base64.getEncoder().encodeToString(logo.getBytes());
		
		System.out.println(decoded);
		
		assertEquals(logo, new String(Base64.getDecoder().decode(decoded)));
		
	}

}
