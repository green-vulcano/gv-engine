/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project. All rights
 * reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package tests.unit.util.txt;

import it.greenvulcano.util.txt.DateUtils;
import it.greenvulcano.util.txt.StringPlaceholderExpander;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

/**
 * @version 3.0.0 Feb 26, 2010
 * @author GreenVulcano Developer Team
 */
public class StringPlaceholderExpanderTestCase extends TestCase
{
    private static final String EXPECTED_EXPANDED = "firstProp_secondProp.";

    private static final String DATE_FORMAT       = "yyyyMMddHHmm";

    private DateFormat          sdf               = new SimpleDateFormat(DATE_FORMAT);

    /**
     * @throws Exception
     */
    public void testExpander() throws Exception
    {
        String input = "%{java:my.java.prop.1}_%{java:my.java.prop.2}.%{datetime:" + DATE_FORMAT + "}";
        System.setProperty("my.java.prop.1", "firstProp");
        System.setProperty("my.java.prop.2", "secondProp");
        StringPlaceholderExpander ph = new StringPlaceholderExpander();
        String expanded = ph.expand(input);
        sdf.setTimeZone(DateUtils.getDefaultTimeZone());
        assertEquals(EXPECTED_EXPANDED + sdf.format(new Date()), expanded);
    }
}
