/*
Japanese Dependency Vectors (jpdv) - A tool for creating Japanese semantic vector spaces.
Copyright (C) 2009 Andrew Young <andrew at vaelen.org>

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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * This class executes various functions implemented as scripts.
 *
 * These include:
 *   The content selection function
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

    private static void executeTestFunction(Function function) {
        function.bind("foo", "bar");
        function.eval();
    }

    public static void main(String[] args) {
        URL url = FunctionExecutor.class.getClassLoader().getResource("/jpdv/functions/TestFunction.groovy");
        Function function = new Function(url);
        executeTestFunction(function);
    }

}
