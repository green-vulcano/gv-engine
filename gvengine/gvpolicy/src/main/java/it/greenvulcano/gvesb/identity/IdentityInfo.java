/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project.
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
 */
package it.greenvulcano.gvesb.identity;

import java.util.List;
import java.util.Set;

import org.apache.commons.net.util.SubnetUtils;

/**
 * @version 3.2.0 01/feb/2012
 * @author GreenVulcano Developer Team
 */
public interface IdentityInfo
{
    public void setParent(IdentityInfo parent);

    public String getName();

    public boolean isInRole(String role);

    public boolean isInRole(String[] roles);

    public boolean isInRole(List<String> roles);

    public boolean isInRole(Set<String> roles);

    public boolean matchAddress(String address);

    public boolean matchAddress(String[] addresses);

    public boolean matchAddress(List<String> addresses);

    public boolean matchAddress(Set<String> addresses);

    public boolean matchAddressMask(Set<SubnetUtils.SubnetInfo> addressMasks);

}
