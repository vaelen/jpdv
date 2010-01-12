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

import java.io.Serializable;
import org.w3c.dom.Element;

public class Token extends LexicalItem implements Serializable {

    private final int localId;
    private final String reading;
    private final String base;
    private final String pos;
    private final String ctype;
    private final String cform;
    private final String ne;
    private final String value;

    private final BaseForm baseForm;
    private final BaseForm posForm;

    private boolean head;
    private boolean func;

    public Token(int localId, String reading, String base, String pos, String ctype, String cform, String ne, String value) {
        super();
        this.localId = localId;
        this.reading = reading;
        this.base = base;
        this.baseForm = BaseForm.getInstance(base);
        this.pos = pos;
        this.posForm = BaseForm.getPOSInstance(pos);
        this.ctype = ctype;
        this.cform = cform;
        this.ne = ne;
        this.value = value;
    }

    public void setIsHead(boolean head) {
        this.head = head;
    }

    public void setIsFunc(boolean func) {
        this.func = func;
    }

    public boolean isHead() {
        return head;
    }

    public boolean isFunc() {
        return func;
    }

    public String getValue() {
        return value;
    }

    public String getBase() {
        return base;
    }

    public BaseForm getBaseForm() {
        return baseForm;
    }

    public String getCform() {
        return cform;
    }

    public String getCtype() {
        return ctype;
    }

    public int getLocalId() {
        return localId;
    }

    public String getNe() {
        return ne;
    }

    public String getPos() {
        return pos;
    }

    public BaseForm getPosForm() {
        return posForm;
    }

    public String getReading() {
        return reading;
    }

    @Override
    public String toString() {
        return value;
    }

    public static Token fromXML(Element element) {
        int localId = Integer.parseInt(element.getAttribute("id"));
        String reading = element.getAttribute("read");
        String base = element.getAttribute("base");
        String pos = element.getAttribute("pos");
        String ctype = element.getAttribute("ctype");
        String cform = element.getAttribute("cform");
        String ne = element.getAttribute("ne");
        return new Token(localId, reading, base, pos, ctype, cform, ne, base);
    }

}
