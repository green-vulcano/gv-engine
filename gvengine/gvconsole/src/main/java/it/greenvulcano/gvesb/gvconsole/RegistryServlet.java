package it.greenvulcano.gvesb.gvconsole;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(displayName="registry", urlPatterns="/registry", loadOnStartup=1 )
public class RegistryServlet extends HttpServlet {
	private static final long serialVersionUID = 4754234535395544113L;
	
	private final String mapper = "function getEndpoints(){"+ 
		
		"var basePath = '%s';"+
		
		"return {"+
			"gviam : basePath +'/gviam',"+
			"gvscheduler : basePath + '/gvscheduler',"+
			"gvconfig : basePath + '/gvconfig',"+
			"gvesb : basePath + '/gvesb',"+
			"gvmonitoring : basePath + '/gvmonitoring',"+
			"gvproperties : basePath + '/gvproperties'" +
		"}"+
	"}";
			
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {		
				
		Properties cxfConfiguration = new Properties();
		
		Path cxfConfigurationPath = Paths.get(System.getProperty("karaf.etc", "cxf"), "org.apache.cxf.osgi.cfg");
		
		if (Files.exists(cxfConfigurationPath) && Files.isRegularFile(cxfConfigurationPath)){				
			cxfConfiguration.load(Files.newInputStream(cxfConfigurationPath, StandardOpenOption.READ));
		}
		
		
		String cxfContextPath = cxfConfiguration.getProperty("org.apache.cxf.servlet.context", "/cxf");				

		resp.addHeader("Content-type", "application/javascript");
		resp.getWriter().write(String.format(mapper, cxfContextPath));	
	}
}
