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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Chunk extends LexicalItem implements Iterable<Token>, Serializable {

    public enum Rel { D, O, UNKNOWN }

    private final LinkedHashMap<Integer, Token> tokens = new LinkedHashMap<Integer, Token>();
    private final SortedSet<Chunk> children = new TreeSet<Chunk>();

    private final int localId;
    private final int link;
    private final Rel rel;
    private final double score;
    private final int head;
    private final int func;

    public Chunk(int localId, int link, String rel, double score, int head, int func) {
        this.localId = localId;
        this.link = link;
        Rel r = Rel.UNKNOWN;
        try {
            r = Rel.valueOf(rel);
        } catch (Exception ex) {
            // Do Nothing
        }
        this.rel = r;
        this.score = score;
        this.head = head;
        this.func = func;
    }

    public void addToken(Token token) {
        if(token.getLocalId() == func) {
            token.setIsFunc(true);
        }
        if(token.getLocalId() == head) {
            token.setIsHead(true);
        }
        tokens.put(token.getLocalId(), token);
    }

    public Token getToken(int localId) {
        return tokens.get(localId);
    }

    public List<Token> getTokens() {
        return new ArrayList(tokens.values());
    }

    public void addChild(Chunk chunk) {
        children.add(chunk);
    }

    public Set<Chunk> getChildren() {
        return children;
    }

    public Token getFunc() {
        return tokens.get(func);
    }

    public int getFuncId() {
        return func;
    }

    public Token getHead() {
        return tokens.get(head);
    }

    public int getHeadId() {
        return head;
    }

    public int getLink() {
        return link;
    }

    public int getLocalId() {
        return localId;
    }

    public Rel getRel() {
        return rel;
    }

    public double getScore() {
        return score;
    }

    public Iterator<Token> iterator() {
        return tokens.values().iterator();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(Token token: tokens.values()) {
            sb.append(token.toString());
        }
        return sb.toString();
    }

    public static Chunk fromXML(Element element) {
        int localId = Integer.parseInt(element.getAttribute("id"));
        int link = Integer.parseInt(element.getAttribute("link"));
        String rel = element.getAttribute("rel");
        double score = Double.parseDouble(element.getAttribute("score"));
        int head = Integer.parseInt(element.getAttribute("head"));
        int func = Integer.parseInt(element.getAttribute("func"));
        Chunk chunk = new Chunk(localId, link, rel, score, head, func);
        NodeList nodeList = element.getElementsByTagName("tok");
        for(int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);
            if(childNode instanceof Element) {
                Element childElement = (Element) childNode;
                Token token = Token.fromXML(childElement);
                chunk.addToken(token);
            }
        }
        return chunk;
    }


}
