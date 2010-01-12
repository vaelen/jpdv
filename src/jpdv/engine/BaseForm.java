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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BaseForm extends LexicalItem {

    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final char POS_SEPARATOR = '-';

    private static final Map<String, BaseForm> baseForms = new HashMap<String, BaseForm>();

    private final String value;

    private BaseForm(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        boolean ret = false;
        if(obj instanceof BaseForm) {
            BaseForm that = (BaseForm) obj;
            ret = this.getId() == that.getId();
            if(!ret) {
                ret = this.value.equals(that.value);
            }
        }
        return ret;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.value != null ? this.value.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(LexicalItem that) {
        if(that instanceof BaseForm) {
            BaseForm bfThat = (BaseForm) that;
            return this.value.compareTo(bfThat.value);
        } else {
            return super.compareTo(that);
        }
    }

    @Override
    public String toString() {
        return value;
    }

    public static BaseForm getInstance(String value) {
        BaseForm baseForm = baseForms.get(value);
        if (baseForm == null) {
            baseForm = new BaseForm(value);
            baseForms.put(value, baseForm);
        }
        return baseForm;
    }

    public static BaseForm getPOSInstance(String pos) {
        String basePOS = pos;
        int sepLocation = pos.indexOf(POS_SEPARATOR);
        if(sepLocation > -1) {
            basePOS = pos.substring(0, sepLocation);
        }
        return getInstance(basePOS);
    }

    public static List<BaseForm> getInstances(String... values) {
        Set<BaseForm> set = new HashSet<BaseForm>();
        for(String value: values) {
            set.add(getInstance(value));
        }
        ArrayList<BaseForm> list = new ArrayList<BaseForm>(set);
        Collections.sort(list);
        return list;
    }

    public static List<BaseForm> getInstances(File file) throws UnsupportedEncodingException, FileNotFoundException, IOException {
        return getInstances(file, DEFAULT_ENCODING);
    }

    public static List<BaseForm> getInstances(File file, String encoding) throws UnsupportedEncodingException, FileNotFoundException, IOException {
        return getInstances(new InputStreamReader(new FileInputStream(file), encoding));
    }

    public static List<BaseForm> getInstances(Reader reader) throws IOException {
        ArrayList<BaseForm> list = new ArrayList<BaseForm>();
        BufferedReader in = new BufferedReader(reader);
        String line = in.readLine();
        while(line != null) {
            int commentStarts = line.indexOf("#");
            if(commentStarts > -1) {
                line = line.substring(0, commentStarts);
            }
            if(line != null) {
                line = line.trim();
                if(line.length() > 0) {
                    list.add(getInstance(line));
                }
            }
            line = in.readLine();
        }
        return list;
    }



    public static int getBaseFormCount() {
        return baseForms.size();
    }

    public static List<BaseForm> getBaseForms() {
        ArrayList<BaseForm> list = new ArrayList<BaseForm>(baseForms.values());
        Collections.sort(list);
        return list;
    }

}