/**
 * Copyright 2006 Servprise International, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.servprise.plugins.jython;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.python.core.Options;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyModule;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.core.imp;
import org.python.util.InteractiveConsole;
import org.python.util.jython;

/**
 * Maven mojo to start an interactive jython shell.
 * 
 * @author Kevin Menard
 *
 * @prefix jython
 * @goal shell
 * @requiresDependencyResolution runtime
 */
public class JythonShellMojo extends AbstractMojo
{
    /**
     * @parameter default-value="${project.compileClasspathElements}"
     * @required
     * @readonly
     */
    private List<String> dependency;
    
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        // Setup the basic python system state from these options
        PySystemState.initialize(PySystemState.getBaseProperties(),
                                 new Properties(), new String[0]);

        // Now create an interpreter
        InteractiveConsole interp = null;
        try {
            String interpClass = PySystemState.registry.getProperty(
                                    "python.console",
                                    "org.python.util.InteractiveConsole");
            interp = (InteractiveConsole)
                             Class.forName(interpClass).newInstance();
        } catch (Exception e) {
            interp = new InteractiveConsole();
        }

        //System.err.println("interp");
        PyModule mod = imp.addModule("__main__");
        interp.setLocals(mod.__dict__);
        //System.err.println("imp");

        String msg = "";
        if (Options.importSite) {
            try {
                imp.load("site");
            } catch (PyException pye) {
                if (!Py.matchException(pye, Py.ImportError)) {
                    System.err.println("error importing site");
                    Py.printException(pye);
                    System.exit(-1);
                }
            }
        }


        Py.getSystemState().path.insert(0, new PyString(""));
        for (String a: dependency) {
    		Py.getSystemState().path.insert(0, new PyString(a));
    	}

        try {
            interp.interact();
        } catch (Throwable t) {
            Py.printException(t);
        }
        interp.cleanup();
//        System.exit(0);
    }
}
