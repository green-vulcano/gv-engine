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
load("nashorn:mozilla_compat.js");
importPackage(Packages.it.greenvulcano.gvesb.buffer);
importPackage(Packages.it.greenvulcano.gvesb.utils);
importPackage(Packages.it.greenvulcano.util.metadata);
importPackage(Packages.it.greenvulcano.util.txt);
importPackage(Packages.it.greenvulcano.util.xml);
importPackage(Packages.it.greenvulcano.util.xpath);
importPackage(Packages.it.greenvulcano.configuration);
importPackage(Packages.org.zeromq);
importPackage(Packages.java.lang);
//importPackage(Packages.java.util);

/**
 Remove leading and tailing spaces from str
 */
function trim(str) {
    return str.replace(/^\s*/, "").replace(/\s*$/, "");
}
