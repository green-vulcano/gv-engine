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
package tests.unit.util.bin;

import it.greenvulcano.util.bin.BinaryUtils;
import it.greenvulcano.util.bin.Dump;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.security.MessageDigest;
import org.apache.commons.io.FileUtils;

import tests.unit.BaseTestCase;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class DumpTestCase extends BaseTestCase
{
    private static final String EXPECTED_DUMP = "00000000; 74 65 73 74 20 64 61 74 61 20 74 6F 20 64 75 6D ; test data to dum\n"
                                                      + "00000010; 70                                              ; p\n";

    /**
     * @throws Exception
     */
    public void testDump() throws Exception
    {
        byte buffer[] = "test data to dump".getBytes();
        Dump dump = new Dump(buffer);
        dump.setMaxBufferLength(Dump.UNBOUNDED);
        ByteArrayOutputStream actualDump = new ByteArrayOutputStream();
        dump.dump(actualDump);
        actualDump.close();
        assertEquals(EXPECTED_DUMP, actualDump.toString());
    }
    
    /**
     * @throws Exception
     */
    public void testDumpRec() throws Exception
    {
        File tmp = new File(System.getProperty("java.io.tmpdir"), "TestDump.dump");
        try {
            FileUtils.deleteQuietly(tmp);

            byte[] dataFile = BinaryUtils.readFileAsBytesFromCP("start.png");
            Dump dump = new Dump(dataFile, Dump.UNBOUNDED);
            OutputStream out = new FileOutputStream(tmp);
            try {
                dump.dump(out);
            }
            finally {
                out.flush();
                out.close();
            }
            dump = new Dump(new FileReader(tmp));
            byte[] dataRec = null;
            try {
                dataRec = dump.recoverToBytes();
            }
            finally {
                out.close();
            }
            
            MessageDigest md = MessageDigest.getInstance("SHA");
            byte[] dFile = md.digest(dataFile);
            byte[] dRec  = md.digest(dataRec);
            boolean result = MessageDigest.isEqual(dFile, dRec);
            System.out.println("Binary Dump conversion succesfull: " + result);
            assertTrue("Binary Dump conversion failed", result);
        }
        finally {
            FileUtils.deleteQuietly(tmp);
        }
    }
}
