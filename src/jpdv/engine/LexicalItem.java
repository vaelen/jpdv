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

package jpdv.engine;

import java.io.Serializable;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class LexicalItem implements Comparable<LexicalItem>, Serializable {

    private static final HashSet<LexicalItemListener> listeners = new HashSet<LexicalItemListener>();

    private static final AtomicInteger idCounter = new AtomicInteger(0);

    private final int id;

    protected LexicalItem() {
        id = idCounter.getAndIncrement();
        fireLexicalItemCreated(this);
    }

    public final int getId() {
        return id;
    }
    
    @Override
    public boolean equals(Object obj) {
        boolean ret = false;
        if (obj instanceof Chunk) {
            ret = ((Chunk) obj).getId() == getId();
        }
        return ret;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + this.id;
        return hash;
    }

    @Override
    public String toString() {
        return String.format("LexicalItem #%d: %s", getId(), getClass().getSimpleName());
    }

    public int compareTo(LexicalItem that) {
        return this.id - that.id;
    }

    public static void fireLexicalItemCreated(LexicalItem lexicalItem) {
        for(LexicalItemListener listener: listeners) {
            listener.lexicalItemCreated(lexicalItem);
        }
    }

    public static void addListener(LexicalItemListener listener) {
        listeners.add(listener);
    }

    public static void removeListener(LexicalItemListener listener) {
        listeners.remove(listener);
    }

}
