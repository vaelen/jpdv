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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import jpdv.engine.BaseForm;

/**
 * This class executes various functions implemented as scripts.
 *
 * These include:
 *   The context selection function
 *   The path value function
 *   The basis mapping function
 *
 * So that the end user can choose which scripting language they want to use,
 * this class uses the Java Scripting API to execute the scripts.  The default
 * implementations are written in Groovy.  JavaScript is also supported by
 * default by the Java Scripting API.  All other languages will require the
 * end user to add custom jars to the classpath before running jpdv.
 * 
 * @author Andrew Young <andrew at vaelen.org>
 */
public class FunctionExecutor {

    private static final Map<FunctionType, Function> FUNCTION_MAP = new ConcurrentHashMap<FunctionType, Function>();
    static {
        for(FunctionType type: FunctionType.values()) {
            FUNCTION_MAP.put(type, getDefaultImpl(type));
        }
    }

    public static Function getDefaultImpl(FunctionType type) {
        Function function = null;
        switch(type) {
            case BASIS_MAPPING:
                function = getDefaultImpl("BasisMappingFunction.groovy");
                break;
            case CONTEXT_SELECTION:
                function = getDefaultImpl("ContextSelectionFunction.groovy");
                break;
            case PATH_VALUE:
                function = getDefaultImpl("PathValueFunction.groovy");
                break;
            case TEST:
            default:
                function = getDefaultImpl("TestFunction.groovy");
                break;
        }
        return function;
    }

    private static Function getDefaultImpl(String name) {
        return new Function(getResource(String.format("/jpdv/functions/impl/%s", name)));
    }

    private static URL getResource(String name) {
        URL url = FunctionExecutor.class.getResource(name);
        if(url == null) {
            Logger.getLogger(FunctionExecutor.class.getName()).log(Level.WARNING, String.format("Resource Not Found: %s", name));
        }
        return url;
    }

    public static void setFunction(FunctionType type, Function function) {
        FUNCTION_MAP.put(type, function);
    }

    public static Function getFunction(FunctionType type) {
        return FUNCTION_MAP.get(type);
    }

    public static synchronized void executeTestFunction(String foo) {
        Function function = FUNCTION_MAP.get(FunctionType.TEST);
        function.bind("foo", foo);
        function.bind("date", new Date());
        function.eval();
    }

    public static synchronized boolean executeContextSelectionFunction(Collection<BaseForm> path) {
        boolean ret = true;
        Function function = FUNCTION_MAP.get(FunctionType.CONTEXT_SELECTION);
        function.bind("path", path);
        Object obj = function.eval();
        if(obj instanceof Boolean) {
            ret = ((Boolean)obj).booleanValue();
        }
        return ret;
    }

    public static synchronized double executePathValueFunction(String path) {
        double ret = 1.0;
        Function function = FUNCTION_MAP.get(FunctionType.PATH_VALUE);
        function.bind("path", path);
        Object obj = function.eval();
        if(obj instanceof Number) {
            ret = ((Number)obj).doubleValue();
        }
        return ret;
    }

    public static synchronized List<BaseForm> executeBasisMappingFunction(Map<BaseForm, Map<BaseForm, Double>> space) {
        List<BaseForm> list = new ArrayList<BaseForm>();
        Function function = FUNCTION_MAP.get(FunctionType.BASIS_MAPPING);
        function.bind("space", space);
        Object obj = function.eval();
        if(obj instanceof Iterable) {
            Iterable i = (Iterable) obj;
            for(Object o: i) {
                BaseForm b = null;
                if(o instanceof BaseForm) {
                    b = (BaseForm) o;
                } else if (o != null) {
                    b = BaseForm.getInstance(o.toString());
                }
                if(b != null) {
                    list.add(b);
                }
            }
        }
        return list;
    }

    public static void main(String[] args) {
        executeTestFunction("bar");
    }

}
