/*
Japanese Dependency Vectors (jpdv) - A tool for creating Japanese semantic vector spaces.
Copyright (C) 2010 Andrew Young <andrew at vaelen.org>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published
by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version. This program is distributed in the
hope that it will be useful, but WITHOUT ANY WARRANTY; without
even the implied warranty of MERCHANTABILITY or FITNESS FOR
A PARTICULAR PURPOSE. See the GNU General Public License
for more details. You should have received a copy of the GNU General
Public License along with this program. If not, see <http://www.gnu.org/licenses/>.

Linking this library statically or dynamically with other modules is
making a combined work based on this library. Thus, the terms and
conditions of the GNU General Public License cover the whole combination.

As a special exception, the copyright holders of this library give you permission
to link this library with independent modules to produce an executable, regardless
of the license terms of these independent modules, and to copy and distribute
the resulting executable under terms of your choice, provided that you also meet,
for each linked independent module, the terms and conditions of the license of
that module. An independent module is a module which is not derived from or
based on this library. If you modify this library, you may extend this exception
to your version of the library, but you are not obligated to do so. If you do not
wish to do so, delete this exception statement from your version.
*/

package jpdv.functions;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import jpdv.util.StreamUtils;

/**
 * This class encapsulates a function script.
 * One of these is created for each function during startup and is then reused
 * so that the script doesn't need to be recompiled, etc.
 * @author Andrew Young <andrew at vaelen.org>
 */
public class Function {

    public static final String DEFAULT_LANGUAGE = "JavaScript";

    private String language;
    private String extension;
    private final String script;
    private ScriptEngine scriptEngine;
    private Bindings bindings;

    public Function(String language, String script) {
        this.language = language;
        this.script = script;
        init();
    }

    public Function(String language, File scriptSource) {
        this.language = language;
        this.script = StreamUtils.read(scriptSource);
        init();
    }

    public Function(File scriptSource) {
        if(scriptSource != null) {
            this.extension = getExtension(scriptSource.getName()).toString();
        }
        this.script = StreamUtils.read(scriptSource);
        init();
    }

    public Function(String language, Reader scriptSource) {
        this.language = language;
        this.script = StreamUtils.read(scriptSource);
        init();
    }

    public Function(String language, InputStream scriptSource) {
        this.language = language;
        this.script = StreamUtils.read(scriptSource);
        init();
    }

    public Function(String language, URI scriptSource) {
        this.language = language;
        this.script = StreamUtils.read(scriptSource);
        init();
    }

    public Function(URI scriptSource) {
        if(scriptSource != null) {
            this.extension = getExtension(scriptSource.getPath()).toString();
        }
        this.script = StreamUtils.read(scriptSource);
        init();
    }

    public Function(String language, URL scriptSource) {
        this.language = language;
        this.script = StreamUtils.read(scriptSource);
        init();
    }

    public Function(URL scriptSource) {
        if(scriptSource != null) {
            this.extension = getExtension(scriptSource.getPath()).toString();
        }
        this.script = StreamUtils.read(scriptSource);
        init();
    }

    private static String getExtension(String path) {
        String ext = null;
        if(path != null && path.length() > 0) {
            try {
                ext = path.substring(path.lastIndexOf('.') + 1);
            } catch (Exception ex) {
                // No Extension Found
            }
        }
        return ext;
    }

    private void init() {
        // create a script engine manager
        ScriptEngineManager factory = new ScriptEngineManager();
        // create a JavaScript engine
        if(language != null) {
            scriptEngine = factory.getEngineByName(language);
        } else if (extension != null) {
            scriptEngine = factory.getEngineByExtension(extension);
        } else {
            scriptEngine = factory.getEngineByName(DEFAULT_LANGUAGE);
        }
        if(scriptEngine != null) {
            bindings = scriptEngine.getContext().getBindings(ScriptContext.ENGINE_SCOPE);
        }
    }

    protected void bind(String name, Object value) {
        if(bindings != null) {
            bindings.put(name, value);
        } else {
            Logger.getLogger(Function.class.getName()).log(Level.WARNING, String.format("Couldn't Bind Value For Key: %s", name));
        }
    }

    protected Object eval() {
        Object ret = null;
        try {
            ret = scriptEngine.eval(script);
        } catch (ScriptException ex) {
            Logger.getLogger(Function.class.getName()).log(Level.SEVERE, "Couldn't Execute Function", ex);
        }
        return ret;
    }

}
