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

package jpdv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Corpus extends LexicalItem implements Iterable<Sentence>, Serializable, LexicalItemListener {

    private final ArrayList<Sentence> sentences = new ArrayList<Sentence>();

    private final LinkedHashMap<Integer, Token> tokenMap = new LinkedHashMap<Integer, Token>();
    private final LinkedHashMap<Integer, Chunk> chunkMap = new LinkedHashMap<Integer, Chunk>();
    private final LinkedHashMap<Integer, Sentence> sentenceMap = new LinkedHashMap<Integer, Sentence>();


    public Corpus() {
        super();
        LexicalItem.addListener(this);
    }

    public void addSentence(Sentence sentence) {
        sentences.add(sentence);
    }

    public List<Sentence> getSetences() {
        return new ArrayList<Sentence>(sentences);
    }

    public Iterator<Sentence> iterator() {
        return sentences.iterator();
    }

    public Token getToken(int id) {
        return tokenMap.get(id);
    }

    public List<Token> getTokens() {
        ArrayList<Token> list = new ArrayList(tokenMap.values());
        Collections.sort(list);
        return list;
    }

    public Chunk getChunk(int id) {
        return chunkMap.get(id);
    }

    public Sentence getSentence(int id) {
        return sentenceMap.get(id);
    }

    public int getChunkCount() {
        return chunkMap.size();
    }

    public int getTokenCount() {
        return tokenMap.size();
    }

    public int getSentenceCount() {
        return sentenceMap.size();
    }

    public int getLexicalItemCount() {
        return getChunkCount() + getTokenCount() + getSentenceCount();
    }

    public void lexicalItemCreated(LexicalItem lexicalItem) {
        if (lexicalItem instanceof Token) {
            synchronized(tokenMap) {
                tokenMap.put(lexicalItem.getId(), (Token) lexicalItem);
            }
        } else if (lexicalItem instanceof Chunk) {
            synchronized(chunkMap) {
                chunkMap.put(lexicalItem.getId(), (Chunk) lexicalItem);
            }
        } else if (lexicalItem instanceof Sentence) {
            synchronized(sentences) {
                sentenceMap.put(lexicalItem.getId(), (Sentence) lexicalItem);
            }
        }
    }

    public void printStats(PrintWriter out) {
        out.printf("Stats:%n--------------------------------%n    Sentences: %,d%n       Chunks: %,d%n       Tokens: %,d%n   Base Forms: %,d%nLexical Items: %,d%n", getSentenceCount(), getChunkCount(), getTokenCount(), BaseForm.getBaseFormCount(), getLexicalItemCount());
        out.flush();
    }

    public void writeText(File file) throws FileNotFoundException {
        writeText(new PrintWriter(file));
    }

    public void writeText(File file, String encoding) throws FileNotFoundException, UnsupportedEncodingException {
        writeText(new PrintWriter(file, encoding));
    }

    public void writeText(PrintWriter out) {
        for (Sentence sentence : sentences) {
            out.println(sentence.toString());
            out.flush();
        }
    }

    public void writeBinary(File file) throws FileNotFoundException, IOException {
        writeBinary(new FileOutputStream(file));
    }

    public void writeBinary(OutputStream out) throws IOException {
        ObjectOutputStream o = new ObjectOutputStream(out);
        o.writeObject(this);
    }

    public static Corpus fromBinary(File file) throws IOException, ClassNotFoundException {
        return fromBinary(new FileInputStream(file));
    }

    public static Corpus fromBinary(InputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream i = new ObjectInputStream(in);
        return (Corpus) i.readObject();
    }

    public static Corpus fromXML(Element element) {
        Corpus corpus = new Corpus();
        NodeList nodeList = element.getElementsByTagName("sentence");
        for(int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);
            if(childNode instanceof Element) {
                Element childElement = (Element) childNode;
                Sentence sentence = Sentence.fromXML(childElement);
                corpus.addSentence(sentence);
            }
        }
        return corpus;
    }

    public static Corpus parseXML(File file) {
        Corpus corpus = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);
            corpus = Corpus.fromXML(document.getDocumentElement());
        } catch (Exception ex) {
            Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, String.format("Couldn't Parse XML Corpus: %s", ex.toString()), ex);
        }
        return corpus;
    }



}
