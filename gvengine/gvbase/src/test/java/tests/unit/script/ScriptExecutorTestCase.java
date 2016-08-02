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
package tests.unit.script;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.script.ScriptExecutor;
import it.greenvulcano.script.ScriptExecutorFactory;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import tests.unit.BaseTestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.w3c.dom.Node;

/**
 *
 * @version 3.5.0 06/ago/2014
 * @author GreenVulcano Developer Team
 *
 */
@RunWith(JUnit4.class)
public class ScriptExecutorTestCase extends BaseTestCase
{
    private static String[] svc = new String[]{"LIST_PDF", "LIST_PIPPO"};
    
    @Before
    public void init() throws Exception {
    	super.setUp();
    }
    
    @Test
    public final void testFactories() {
        ScriptEngineManager mgr = new ScriptEngineManager();
        List<ScriptEngineFactory> factories = mgr.getEngineFactories();
        assertFalse(factories.isEmpty());
        for (ScriptEngineFactory factory: factories) {
          System.out.println("ScriptEngineFactory Info");
          String engName = factory.getEngineName();
          String engVersion = factory.getEngineVersion();
          String langName = factory.getLanguageName();
          String langVersion = factory.getLanguageVersion();
          String threading = (String) factory.getParameter("THREADING");
          System.out.printf("\tScript Engine: %s (%s)\n", engName, engVersion);
          List<String> engNames = factory.getNames();
          for(String name: engNames) {
            System.out.printf("\tEngine Alias: %s\n", name);
          }
          System.out.printf("\tLanguage: %s (%s)\n", langName, langVersion);
          System.out.printf("\tThreading: %s\n", threading);
        }    
    }
    
    @Test
    public final void testDirectJS() throws Exception  {
    	
        String script = "var services = {\"LIST_EXCEL\":\"1\", \"LIST_PDF\":\"1\", \"LIST_BIRT\":\"1\"};\n"
                      + "var svc = data.getProperty(\"SVC\");\n"
                      + "(\"1\" == services[svc]);";
        ScriptExecutor se = ScriptExecutorFactory.createSE("js", script, null, null);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(GVBufferPropertiesHelper.getPropertiesMapSO(gvb, true), gvb);
            se.cleanUp();
            System.out.println("testDirectJS[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testDirectJS: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }

    @Test
    public final void testDirectOGNL() throws Exception  {
    	
        String script = "#services = #{\"LIST_EXCEL\":\"1\", \"LIST_PDF\":\"1\", \"LIST_BIRT\":\"1\"},\n"
                      + "#svc = #data.property[\"SVC\"],\n"
                      + "(\"1\" == #services[#svc])";
        ScriptExecutor se = ScriptExecutorFactory.createSE("ognl", script, null, null);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(GVBufferPropertiesHelper.getPropertiesMapSO(gvb, true), gvb);
            se.cleanUp();
            System.out.println("testDirectOGNL[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testDirectOGNL: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }

    @Test
    public final void testImmediateJS() throws Exception
    {
        String script = "var services = {\"LIST_EXCEL\":\"1\", \"LIST_PDF\":\"1\", \"LIST_BIRT\":\"1\"};\n"
                      + "var svc = data.getProperty(\"SVC\");\n"
                      + "(\"1\" == services[svc]);";

        GVBuffer gvb = new GVBuffer();
        Map<String, Object> bindings = new TreeMap<String, Object>();
        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            bindings.put("data", gvb);
            Object out = ScriptExecutor.execute("js", script, bindings, null);

            System.out.println("testImmediateJS[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testImmediateJS: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }

    @Test
    public final void testSimpleJS() throws Exception
    {
        Node n = XMLConfig.getNode("GVCore.xml", "//Service[@id-service='TestScript']//ChangeGVBufferNode[@id='testJS']/ChangeGVBuffer/Script");
        ScriptExecutor se = ScriptExecutorFactory.createSE(n);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(GVBufferPropertiesHelper.getPropertiesMapSO(gvb, true), gvb);
            se.cleanUp();
            System.out.println("testSimpleJS[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testSimpleJS: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }

    @Test
    public final void testSimpleOGNL() throws Exception
    {
        Node n = XMLConfig.getNode("GVCore.xml", "//Service[@id-service='TestScript']//ChangeGVBufferNode[@id='testOGNL']/ChangeGVBuffer/Script");
        ScriptExecutor se = ScriptExecutorFactory.createSE(n);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(GVBufferPropertiesHelper.getPropertiesMapSO(gvb, true), gvb);
            se.cleanUp();
            System.out.println("testSimpleOGNL[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testSimpleOGNL: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }

    @Test
    public final void testSimpleGroovy() throws Exception
    {
        Node n = XMLConfig.getNode("GVCore.xml", "//Service[@id-service='TestScript']//ChangeGVBufferNode[@id='testGroovy']/ChangeGVBuffer/Script");
        ScriptExecutor se = ScriptExecutorFactory.createSE(n);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(GVBufferPropertiesHelper.getPropertiesMapSO(gvb, true), gvb);
            se.cleanUp();
            System.out.println("testSimpleGroovy[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testSimpleGroovy: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }

    @Test 
    public final void testSimpleJRuby() throws Exception
    {
        Node n = XMLConfig.getNode("GVCore.xml", "//Service[@id-service='TestScript']//ChangeGVBufferNode[@id='testJRuby']/ChangeGVBuffer/Script");
        ScriptExecutor se = ScriptExecutorFactory.createSE(n);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(GVBufferPropertiesHelper.getPropertiesMapSO(gvb, true), gvb);
            se.cleanUp();
            System.out.println("testSimpleJRuby[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testSimpleJRuby: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }
       
    @Test
    public final void testSimpleJython() throws Exception
    {
        Node n = XMLConfig.getNode("GVCore.xml", "//Service[@id-service='TestScript']//ChangeGVBufferNode[@id='testJython']/ChangeGVBuffer/Script");
        ScriptExecutor se = ScriptExecutorFactory.createSE(n);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(GVBufferPropertiesHelper.getPropertiesMapSO(gvb, true), gvb);
            se.cleanUp();
            System.out.println("testSimpleJython[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testSimpleJython: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }

    @Test
    public final void testSimpleJS_props() throws Exception
    {
        Node n = XMLConfig.getNode("GVCore.xml", "//Service[@id-service='TestScript']//ChangeGVBufferNode[@id='testJS_props']/ChangeGVBuffer/Script");
        ScriptExecutor se = ScriptExecutorFactory.createSE(n);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(GVBufferPropertiesHelper.getPropertiesMapSO(gvb, true), gvb);
            se.cleanUp();
            System.out.println("testSimpleJS_props[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testSimpleJS_props: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }

    @Test
    public final void testSimpleOGNL_props() throws Exception
    {
        Node n = XMLConfig.getNode("GVCore.xml", "//Service[@id-service='TestScript']//ChangeGVBufferNode[@id='testOGNL_props']/ChangeGVBuffer/Script");
        ScriptExecutor se = ScriptExecutorFactory.createSE(n);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(GVBufferPropertiesHelper.getPropertiesMapSO(gvb, true), gvb);
            se.cleanUp();
            System.out.println("testSimpleOGNL_props[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testSimpleOGNL_props: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }

    @Test
    public final void testSimpleGroovy_props() throws Exception
    {
        Node n = XMLConfig.getNode("GVCore.xml", "//Service[@id-service='TestScript']//ChangeGVBufferNode[@id='testGroovy_props']/ChangeGVBuffer/Script");
        ScriptExecutor se = ScriptExecutorFactory.createSE(n);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(GVBufferPropertiesHelper.getPropertiesMapSO(gvb, true), gvb);
            se.cleanUp();
            System.out.println("testSimpleGroovy_props[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testSimpleGroovy_props: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }

    @Test
    public final void testSimpleJRuby_props() throws Exception
    {
        Node n = XMLConfig.getNode("GVCore.xml", "//Service[@id-service='TestScript']//ChangeGVBufferNode[@id='testJRuby_props']/ChangeGVBuffer/Script");
        ScriptExecutor se = ScriptExecutorFactory.createSE(n);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(GVBufferPropertiesHelper.getPropertiesMapSO(gvb, true), gvb);
            se.cleanUp();
            System.out.println("testSimpleJRuby_props[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testSimpleJRuby_props: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }
     
    @Test
    public final void testSimpleJython_props() throws Exception
    {
        Node n = XMLConfig.getNode("GVCore.xml", "//Service[@id-service='TestScript']//ChangeGVBufferNode[@id='testJython_props']/ChangeGVBuffer/Script");
        ScriptExecutor se = ScriptExecutorFactory.createSE(n);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(GVBufferPropertiesHelper.getPropertiesMapSO(gvb, true), gvb);
            se.cleanUp();
            System.out.println("testSimpleJython_props[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testSimpleJython_props: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }

    @Test
    public final void testSimpleJS_file() throws Exception
    {
        Node n = XMLConfig.getNode("GVCore.xml", "//Service[@id-service='TestScript']//ChangeGVBufferNode[@id='testJS_file']/ChangeGVBuffer/Script");
        ScriptExecutor se = ScriptExecutorFactory.createSE(n);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(GVBufferPropertiesHelper.getPropertiesMapSO(gvb, true), gvb);
            se.cleanUp();
            System.out.println("testSimpleJS_file[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testSimpleJS_file: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }

   @Test
    public final void testSimpleOGNL_file() throws Exception
    {
        Node n = XMLConfig.getNode("GVCore.xml", "//Service[@id-service='TestScript']//ChangeGVBufferNode[@id='testOGNL_file']/ChangeGVBuffer/Script");
        ScriptExecutor se = ScriptExecutorFactory.createSE(n);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(GVBufferPropertiesHelper.getPropertiesMapSO(gvb, true), gvb);
            se.cleanUp();
            System.out.println("testSimpleOGNL_file[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testSimpleOGNL_file: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }

    @Test
    public final void testSimpleGroovy_file() throws Exception
    {
        Node n = XMLConfig.getNode("GVCore.xml", "//Service[@id-service='TestScript']//ChangeGVBufferNode[@id='testGroovy_file']/ChangeGVBuffer/Script");
        ScriptExecutor se = ScriptExecutorFactory.createSE(n);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(GVBufferPropertiesHelper.getPropertiesMapSO(gvb, true), gvb);
            se.cleanUp();
            System.out.println("testSimpleGroovy_file[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testSimpleGroovy_file: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }

    @Test
    public final void testSimpleJRuby_file() throws Exception
    {
        Node n = XMLConfig.getNode("GVCore.xml", "//Service[@id-service='TestScript']//ChangeGVBufferNode[@id='testJRuby_file']/ChangeGVBuffer/Script");
        ScriptExecutor se = ScriptExecutorFactory.createSE(n);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(GVBufferPropertiesHelper.getPropertiesMapSO(gvb, true), gvb);
            se.cleanUp();
            System.out.println("testSimpleJRuby_file[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testSimpleJRuby_file: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }
    
    @Test
    public final void testSimpleJython_file() throws Exception
    {
        Node n = XMLConfig.getNode("GVCore.xml", "//Service[@id-service='TestScript']//ChangeGVBufferNode[@id='testJython_file']/ChangeGVBuffer/Script");
        ScriptExecutor se = ScriptExecutorFactory.createSE(n);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(GVBufferPropertiesHelper.getPropertiesMapSO(gvb, true), gvb);
            se.cleanUp();
            System.out.println("testSimpleJython_file[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testSimpleJython_file: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }

    @Test
    public final void testSimpleJS_inc_file() throws Exception
    {
        Node n = XMLConfig.getNode("GVCore.xml", "//Service[@id-service='TestScript']//ChangeGVBufferNode[@id='testJS_inc_file']/ChangeGVBuffer/Script");
        ScriptExecutor se = ScriptExecutorFactory.createSE(n);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(GVBufferPropertiesHelper.getPropertiesMapSO(gvb, true), gvb);
            se.cleanUp();
            System.out.println("testSimpleJS_inc_file[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testSimpleJS_inc_file: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }

    @Test
    public final void testSimpleOGNL_inc_file() throws Exception
    {
        Node n = XMLConfig.getNode("GVCore.xml", "//Service[@id-service='TestScript']//ChangeGVBufferNode[@id='testOGNL_inc_file']/ChangeGVBuffer/Script");
        ScriptExecutor se = ScriptExecutorFactory.createSE(n);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(GVBufferPropertiesHelper.getPropertiesMapSO(gvb, true), gvb);
            se.cleanUp();
            System.out.println("testSimpleOGNL_inc_file[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testSimpleOGNL_inc_file: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }

    @Test
    public final void testSimpleGroovy_inc_file() throws Exception
    {
        Node n = XMLConfig.getNode("GVCore.xml", "//Service[@id-service='TestScript']//ChangeGVBufferNode[@id='testGroovy_inc_file']/ChangeGVBuffer/Script");
        ScriptExecutor se = ScriptExecutorFactory.createSE(n);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(GVBufferPropertiesHelper.getPropertiesMapSO(gvb, true), gvb);
            se.cleanUp();
            System.out.println("testSimpleGroovy_inc_file[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testSimpleGroovy_inc_file: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }

    @Test
    public final void testSimpleJRuby_inc_file() throws Exception
    {
        Node n = XMLConfig.getNode("GVCore.xml", "//Service[@id-service='TestScript']//ChangeGVBufferNode[@id='testJRuby_inc_file']/ChangeGVBuffer/Script");
        ScriptExecutor se = ScriptExecutorFactory.createSE(n);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(GVBufferPropertiesHelper.getPropertiesMapSO(gvb, true), gvb);
            se.cleanUp();
            System.out.println("testSimpleJRuby_inc_file[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testSimpleJRuby_inc_file: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }
       
    @Test
    public final void testSimpleJython_inc_file() throws Exception
    {
        Node n = XMLConfig.getNode("GVCore.xml", "//Service[@id-service='TestScript']//ChangeGVBufferNode[@id='testJython_inc_file']/ChangeGVBuffer/Script");
        ScriptExecutor se = ScriptExecutorFactory.createSE(n);

        GVBuffer gvb = new GVBuffer();

        for (int j = 0; j < 10; j++) {
            gvb.setProperty("SVC", svc[j % 2]);
            se.putProperty("data", gvb);
            Object out = se.execute(GVBufferPropertiesHelper.getPropertiesMapSO(gvb, true), gvb);
            se.cleanUp();
            System.out.println("testSimpleJython_inc_file[" + j + "]: " + out.getClass() + " -> " + out);

            assertEquals("testSimpleJython_inc_file: Failed iteration " + j, out, (j % 2 == 0));            
        }
    }

}
