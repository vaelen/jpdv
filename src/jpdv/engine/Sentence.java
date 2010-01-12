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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Sentence extends LexicalItem implements Serializable, Iterable<Chunk> {

    public static final Token SENTENCE_SEPARATOR = new Token(-1, "<s>", "<s>", "", "", "", "0", "<s>");

    private final LinkedHashMap<Integer, Chunk> chunks = new LinkedHashMap<Integer, Chunk>();

    public Sentence() {
        super();
    }

    public void addChunk(Chunk chunk) {
        chunks.put(chunk.getLocalId(), chunk);
    }

    public Chunk getChunk(int localId) {
        return chunks.get(localId);
    }

    public List<Chunk> getChunks() {
        return new ArrayList<Chunk>(chunks.values());
    }

    public void fillTree() {
        for (Chunk chunk: chunks.values()) {
            int link = chunk.getLink();
            Chunk parent = chunks.get(link);
            if(parent != null) {
                parent.addChild(chunk);
            }
        }
    }

    public Iterator<Chunk> iterator() {
        return chunks.values().iterator();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(Chunk chunk: chunks.values()) {
            sb.append(chunk.toString());
        }
        return sb.toString();
    }

    public static Sentence fromXML(Element element) {
        Sentence sentence = new Sentence();
        NodeList nodeList = element.getElementsByTagName("chunk");
        for(int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);
            if(childNode instanceof Element) {
                Element childElement = (Element) childNode;
                Chunk chunk = Chunk.fromXML(childElement);
                sentence.addChunk(chunk);
            }
        }
        sentence.fillTree();
        return sentence;
    }
}
