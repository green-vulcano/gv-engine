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

import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.txt.DateUtils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * 
 * @version 3.0.0 10/giu/2010
 * @author GreenVulcano Developer Team
 */
public class PropertiesHandlerTestCase extends TestCase
{

	private static final String BASE_DIR = "target" + File.separator + "test-classes";
	
	@Override
	protected void setUp() throws Exception {		
		super.setUp();
		 System.setProperty("gv.app.home", BASE_DIR);
	}
	
    /**
     *
     */
    @Test
    public void testExpand1() throws Exception
    {
        HashMap<String, Object> props = new HashMap<String, Object>();
        props.put("pippo", "pluto");
        props.put("topolino", "paperoga");

        String match = "..pluto..paperoga";
        String src = "..@{{pippo}}..@{{topolino}}";
        String dest = PropertiesHandler.expand(src, props);
        assertEquals(match, dest);
    }

    /**
     *
     */
    @Test
    public void testExpand2() throws Exception
    {
        String match = System.getProperty("java.runtime.name");
        String src = "${{java.runtime.name}}";
        String dest = PropertiesHandler.expand(src);
        assertEquals(match, dest);

        src = "sp{{java.runtime.name}}";
        dest = PropertiesHandler.expand(src);
        assertEquals(match, dest);
    }

    /**
     *
     */
    @Test
    public void testExpand3() throws Exception
    {
        String match = "10";
        String src = "js{{basic:: var i=5; var r = new Number(i*2); r.toFixed(0);}}";
        String dest = PropertiesHandler.expand(src);
        assertEquals(match, dest);
    }

    /**
     *
     */
    @Test
    public void testExpand3a() throws Exception
    {
        String match = "10";
        String src = "script{{js::basic:: var i=5; var r = new Number(i*2); r.toFixed(0);}}";
        String dest = PropertiesHandler.expand(src);
        assertEquals(match, dest);
    }

    /**
     *
     */
    @Test
    public void testExpand3b() throws Exception
    {
        String match = "10";
        String src = "script{{js:: var i=5; var r = new Number(i*2); r.toFixed(0);}}";
        String dest = PropertiesHandler.expand(src);
        assertEquals(match, dest);
    }

    /**
     *
     */
    @Test
    public void testExpand3c() throws Exception
    {
        String match = "10";
        String src = "script{{ognl:: #i=5, #i*2}}";
        String dest = PropertiesHandler.expand(src);
        assertEquals(match, dest);
    }
    
    /**
     *
     */
    @Test
    public void testExpand3d() throws Exception
    {
        String match = "10";
        String src = "script{{jruby:: i=5; i*2}}";
        String dest = PropertiesHandler.expand(src);
        assertEquals(match, dest);
    }

    /**
     *
     */
    @Test
    public void testExpand4() throws Exception
    {
        HashMap<String, Object> props = new HashMap<String, Object>();
        props.put("prop1", "<elem0 attr0='vattr0'><elem1 attr1='vattr1'/></elem0>");

        String match = "vattr1";
        String src = "xpath{{prop1:://elem1/@attr1}}";
        String dest = PropertiesHandler.expand(src, props);
        assertEquals(match, dest);
    }

    /**
     *
     */
    @Test
    public void testExpand5() throws Exception
    {
        String match = "pippo";
        String src = "xpath{{file://fileManager_test.xml:://element}}";
        String dest = PropertiesHandler.expand(src);
        assertEquals(match, dest);
    }

    /**
     *
     */
    @Test
    public void testExpand6() throws Exception
    {
        HashMap<String, Object> props = new HashMap<String, Object>();
        props.put("prop1", "<elem0 attr0='vattr0'><elem1 attr1='vattr1'/></elem0>");
        props.put("prop2", "xpath{{prop1:://elem1/@attr1}}");

        String match = "vattr1";
        String src = "@{{prop2}}";
        String dest = PropertiesHandler.expand(src, props);
        assertEquals(match, dest);
    }

    /**
     *
     */
    @Test
    public void testExpand7() throws Exception
    {
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
        df.setTimeZone(DateUtils.getDefaultTimeZone());
        String match = df.format(new Date());
        String src = "timestamp{{yyyyMMddHHmm}}";
        String dest = PropertiesHandler.expand(src);
        assertEquals(match, dest);
    }

    /**
    *
    */
    @Test
    public void testExpand7b() throws Exception
    {
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        String match = df.format(new Date());
        String src = "timestamp{{yyyyMMddHHmm::GMT}}";
        String dest = PropertiesHandler.expand(src);
        assertEquals(match, dest);
    }

    /**
     *
     */
    @Test
    public void testExpand8() throws Exception
    {
        String match = "15/01/2010 12:30";
        String src = "dateformat{{201001151230::yyyyMMddHHmm::dd/MM/yyyy HH:mm}}";
        String dest = PropertiesHandler.expand(src);
        assertEquals(match, dest);
    }

    /**
    *
    */
    @Test
    public void testExpand8b() throws Exception
    {
        String match = "15/01/2010 13:30";
        String src = "dateformat{{201001151230::yyyyMMddHHmm::dd/MM/yyyy HH:mm::GMT::Europe/Rome}}";
        String dest = PropertiesHandler.expand(src);
        assertEquals(match, dest);
    }

    /**
    *
    */
    @Test
    public void testExpand8c() throws Exception
    {
        String match = "15/04/2010 14:30";
        String src = "dateformat{{201004151230::yyyyMMddHHmm::dd/MM/yyyy HH:mm::GMT::Europe/Rome}}";
        String dest = PropertiesHandler.expand(src);
        assertEquals(match, dest);
    }

    /**
     *
     */
    @Test
    public void testExpand9() throws Exception
    {
        HashMap<String, Object> props = new HashMap<String, Object>();
        String src = "decode{{@{{prop1}}::1::AAA::2::BBB::CCC}}";

        props.put("prop1", "1");
        String dest = PropertiesHandler.expand(src, props);
        assertEquals("AAA", dest);

        props.put("prop1", "2");
        dest = PropertiesHandler.expand(src, props);
        assertEquals("BBB", dest);

        props.put("prop1", "3");
        dest = PropertiesHandler.expand(src, props);
        assertEquals("CCC", dest);
    }

    /**
     *
     */
    @Test
    public void testExpand10() throws Exception
    {
        HashMap<String, Object> props = new HashMap<String, Object>();
        String src = "decodeL{{|::@{{prop1}}::1|3::AAA::2|4::BBB::CCC}}";

        props.put("prop1", "1");
        String dest = PropertiesHandler.expand(src, props);
        assertEquals("AAA", dest);
        props.put("prop1", "3");
        dest = PropertiesHandler.expand(src, props);
        assertEquals("AAA", dest);

        props.put("prop1", "2");
        dest = PropertiesHandler.expand(src, props);
        assertEquals("BBB", dest);
        props.put("prop1", "4");
        dest = PropertiesHandler.expand(src, props);
        assertEquals("BBB", dest);

        props.put("prop1", "5");
        dest = PropertiesHandler.expand(src, props);
        assertEquals("CCC", dest);
    }

    /**
     *
     */
    @Test
    public void testExpand11() throws Exception
    {
        String obj = "";
        String src = "%{{class}}";
        String dest = PropertiesHandler.expand(src, null, obj);
        assertEquals("String", dest);

        src = "%{{fqclass}}";
        dest = PropertiesHandler.expand(src, null, obj);
        assertEquals("java.lang.String", dest);

        src = "%{{package}}";
        dest = PropertiesHandler.expand(src, null, obj);
        assertEquals("java.lang", dest);
    }

    /**
     *
     */
    @Test
    public void testExpand12() throws Exception
    {
        String src = "escJS{{aaa\" \n... 'vv'}}";
        String dest = PropertiesHandler.expand(src);
        assertEquals("aaa\\\" \\n... \\'vv\\'", dest);
    }

    /**
     *
     */
    @Test
    public void testExpand13() throws Exception
    {
        String src = "escSQL{{aaa. 'vv'}}";
        String dest = PropertiesHandler.expand(src);
        assertEquals("aaa. ''vv''", dest);
    }

    /**
     *
     */
    @Test
    public void testExpand14() throws Exception
    {
        String src = "escXML{{aaa< > ' \"..&}}";
        String dest = PropertiesHandler.expand(src);
        assertEquals("aaa&lt; &gt; &apos; &quot;..&amp;", dest);
    }

    /**
     *
     */
    @Test
    public void testExpand15() throws Exception
    {
        String src = "replace{{aaaBBB 'BBB'..BBB::BBB::C_C}}";
        String dest = PropertiesHandler.expand(src);
        assertEquals("aaaC_C 'C_C'..C_C", dest);
    }

    /**
    *
    */
   @Test
   public void testExpand16() throws Exception
   {
       String match = System.getenv("PATH");
       System.out.println("----- PATH env: " + match);
       String src = "env{{PATH}}";
       String dest = PropertiesHandler.expand(src);
       assertEquals(match, dest);

       match = System.getenv("HOME");
       System.out.println("----- HOME env: " + match);
       src = "env{{HOME}}";
       dest = PropertiesHandler.expand(src);
       assertEquals(match, dest);
   }

/**
    *
    */
   @Test
   public void testExpand17() throws Exception
   {
       String src = "urlEnc{{aa bb&c/.=}}";
       String dest = PropertiesHandler.expand(src);
       assertEquals("aa+bb%26c%2F.%3D", dest);
   }

   /**
   *
   */
  @Test
  public void testExpand18() throws Exception
  {
      String src = "urlDec{{aa+bb%26c%2F.%3D}}";
      String dest = PropertiesHandler.expand(src);
      assertEquals("aa bb&c/.=", dest);
  }
  
  /**
  *
  */
  @Test
  public void testExpand19() throws Exception
  {
     String src = "http://www.report.com/report?VAL_SESSIONID=urlEnc{{@{{VAL_SESSIONID}}}}&DAT_RIFERIMENTO=urlEnc{{@{{DAT_RIFERIMENTO}}}}&DAT_CARICAMENTO=urlEnc{{@{{DAT_CARICAMENTO}}}}";
     
     HashMap<String, Object> props = new HashMap<String, Object>();
     props.put("DAT_CARICAMENTO", "2013-10-16T17:36:38.620"); 
     props.put("DAT_RIFERIMENTO", "2013-10-16");
     props.put("VAL_SESSIONID", "AC12FAB8526003FD0E9D4425");
     
     String dest = PropertiesHandler.expand(src);
     assertEquals("No encode", src, dest);
     
     dest = PropertiesHandler.expand(src, props);
     assertEquals("Encode", "http://www.report.com/report?VAL_SESSIONID=AC12FAB8526003FD0E9D4425&DAT_RIFERIMENTO=2013-10-16&DAT_CARICAMENTO=2013-10-16T17%3A36%3A38.620", dest);
  }
}
