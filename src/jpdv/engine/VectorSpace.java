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

package jpdv.engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import jpdv.functions.FunctionExecutor;

public abstract class VectorSpace {

    protected Corpus corpus;
    protected Map<BaseForm, Map<BaseForm, Integer>> space = new TreeMap<BaseForm, Map<BaseForm, Integer>>();

    protected VectorSpace(Corpus corpus) {
        this.corpus = corpus;
    }

    public abstract void generateSpace(Collection<BaseForm> targets);

    public List<BaseForm> getBasisElements() {
        // Generate list of basis elements
        return FunctionExecutor.executeBasisMappingFunction(space);
    }

    protected void incrementCount(BaseForm key, Collection<BaseForm> basisElements) {
        Map<BaseForm, Integer> map = space.get(key);
        if (map == null) {
            map = new TreeMap<BaseForm, Integer>();
            space.put(key, map);
        }
        for(BaseForm basisElement: basisElements) {
            Integer i = map.get(basisElement);
            if (i == null) {
                i = 0;
            }
            i++;
            map.put(basisElement, i);
        }
    }

    public void writeSpace(File file) throws FileNotFoundException {
        writeSpace(new PrintWriter(file));
    }

    public void writeSpace(File file, String encoding) throws FileNotFoundException, UnsupportedEncodingException {
        writeSpace(new PrintWriter(file, encoding));
    }

    public void writeSpace(PrintWriter out) {
        List<BaseForm> basisElements = getBasisElements();
        out.printf("WORD");
        for (BaseForm basisElement : basisElements) {
            out.printf("\t%s", basisElement);
        }
        out.println();
        out.flush();
        for (Map.Entry<BaseForm, Map<BaseForm, Integer>> entry : space.entrySet()) {
            BaseForm current = entry.getKey();
            Map<BaseForm, Integer> map = entry.getValue();
            out.printf("%s", current);
            for (BaseForm basisElement : basisElements) {
                Integer count = map.get(basisElement);
                if (count == null) {
                    count = 0;
                }
                out.printf("\t%d", count);
            }
            out.println();
            out.flush();
        }
    }
}
