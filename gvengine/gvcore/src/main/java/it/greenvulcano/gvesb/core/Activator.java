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
package it.greenvulcano.gvesb.core;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import javax.management.ObjectName;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.core.jmx.JMXServiceManager;
import it.greenvulcano.gvesb.core.jmx.ServiceOperationInfoManager;
import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.log.NMDC;

public class Activator implements BundleActivator  {

	private final Logger LOG = LoggerFactory.getLogger(getClass());
	private final Set<Optional<ObjectName>> names = new LinkedHashSet<>();
	
	@Override
	public void start(BundleContext context) throws Exception {
				
		NMDC.push();
	    try {
	        NMDC.setServer(JMXEntryPoint.getInstance().getServerName());
		
			JMXEntryPoint jmx = JMXEntryPoint.getInstance();
					
			JMXServiceManager svcManager = JMXServiceManager.instance();
			names.add(Optional.ofNullable(jmx.registerObject(svcManager, JMXServiceManager.DESCRIPTOR_NAME)));
			names.add(Optional.ofNullable(jmx.registerObject(svcManager, JMXServiceManager.DESCRIPTOR_NAME + "_Internal")));
			        
	        ServiceOperationInfoManager infoManager = ServiceOperationInfoManager.instance();
	        names.add(Optional.ofNullable(jmx.registerObject(infoManager, ServiceOperationInfoManager.DESCRIPTOR_NAME)));
	        names.add(Optional.ofNullable(jmx.registerObject(infoManager, ServiceOperationInfoManager.DESCRIPTOR_NAME + "_Internal")));
        
	    } catch (Exception e) {
            LOG.error("Failed to register MBean", e);
        } finally {
            NMDC.pop();
        }
        
		LOG.debug("********* GVCore Up&Running");

		String logo ="\n"+
				"             .:::::."+"\n"+
				"            -+  .| :/"+"\n"+
				"            o ALAN  s"+"\n"+
				"            .+.`.| +-                __ _   _ __    ___    ___   _ __"+"\n"+
				"              .:::y+ .....          / _` | | '__|  / _ \\  / _ \\ | '_ \\"+"\n"+
				"                  /yo++++o/-     . | (_| | | |    |  __/ |  __/ | | | |"+"\n"+
				"  .::::.        -o/.       /s/+oo/  \\__, | |_|     \\___|  \\___| |_| |_|"+"\n"+
				" o  @@ +-      .s-     .-.  -s:      ___ /      _ "+"\n"+
				":: @@ @ dyyyyyyso _-:::  `--.++     |___/      | |"+"\n"+
				"`o E@I o-      :s-          .s/    __   ___   _| | ___ __ _ _ __   ___"+"\n"+
				"  .:::-         +o.        `oo`    \\ \\ / / | | | |/ __/ _` | '_ \\ / _ \\"+"\n"+
				"                .ss+:-..-:+o-       \\ V /| |_| | | (_| (_| | | | | (_) |"+"\n"+
				"          .:::./ho``.::::-`          \\_/  \\__,_|_|\\___\\__,_|_| |_|\\___/"+"\n"+
				"        .+ --- +."+"\n"+
				"        o  GVT  o                          T e c h n o l o g i e s"+"\n"+
				"        .+ --- +. "+"\n"+
				"          -:::-'                                 GV ESB v4"+"\n";
		
		System.out.println(logo);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		NMDC.push();
	    try {
	        NMDC.setServer(JMXEntryPoint.getInstance().getServerName());
			JMXEntryPoint jmx = JMXEntryPoint.getInstance();
			
			names.stream().forEach(o->o.ifPresent(jmx::unregisterObject));
	    } catch (Exception e) {
            LOG.error("Failed to register MBean", e);
        } finally {
            NMDC.pop();
        }
		LOG.debug("********* GVCore stopped");
	}

}
