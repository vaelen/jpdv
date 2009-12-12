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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ContextSpace extends VectorSpace {

    // The context size is the number of tokens to look at
    // on each side of the current token.
    private int contextSize;
    private Iterator<Sentence> sentenceIterator;
    private Iterator<Chunk> chunkIterator;
    private Iterator<Token> tokenIterator;

    public ContextSpace(Corpus corpus, int contextSize) {
        super(corpus);
        this.contextSize = contextSize;
    }

    @Override
    public void generateSpace(Collection<BaseForm> targets) {
        Set<BaseForm> targetSet = new HashSet<BaseForm>(targets);
        Logger logger = Logger.getLogger(ContextSpace.class.getName());
        // Begin Setup
        Deque<Token> preContext = new LinkedList<Token>();
        Deque<Token> postContext = new LinkedList<Token>();
        Token current = nextToken();
        while (current != null && preContext.size() < contextSize) {
            preContext.addLast(current);
            current = nextToken();
        }
        boolean done = false;
        while (!done && postContext.size() < contextSize) {
            Token future = nextToken();
            if (future == null) {
                done = true;
            } else {
                postContext.addLast(future);
            }
        }
        // End Setup

        // Iterate
        while (current != null) {
            if (targetSet.contains(current.getBaseForm())) {
                incrementCount(current, preContext, postContext);
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, String.format("Pre-Context: %s, Token: %s, Post-Context: %s", preContext.toString(), current, postContext.toString()));
                }
            }
            preContext.removeFirst();
            preContext.addLast(current);
            if (postContext.isEmpty()) {
                current = null;
            } else {
                current = postContext.removeFirst();

                Token future = nextToken();
                if (future != null) {
                    postContext.addLast(future);
                }
            }
        }
    }

    private void incrementCount(Token current, Collection<Token> preContext, Collection<Token> postContext) {
        BaseForm key = current.getBaseForm();
        ArrayList<BaseForm> basisElements = new ArrayList<BaseForm>(preContext.size() + postContext.size());
        for (Token t : preContext) {
            basisElements.add(t.getBaseForm());
        }
        for (Token t : postContext) {
            basisElements.add(t.getBaseForm());
        }
        incrementCount(key, basisElements);
    }

    private Token nextToken() {
        Token token = null;
        if (sentenceIterator == null) {
            sentenceIterator = corpus.iterator();
            if (sentenceIterator.hasNext()) {
                chunkIterator = sentenceIterator.next().iterator();
                if (chunkIterator.hasNext()) {
                    tokenIterator = chunkIterator.next().iterator();
                    // First sentence
                    token = Sentence.SENTENCE_SEPARATOR;
                }
            }
        } else if (tokenIterator == null) {
            // We're done
            token = null;
        } else {
            while (tokenIterator != null && !tokenIterator.hasNext()) {
                // Move to next chunk
                while (tokenIterator != null && !chunkIterator.hasNext()) {
                    // Move to next sentence
                    token = Sentence.SENTENCE_SEPARATOR;
                    if (!sentenceIterator.hasNext()) {
                        // End of corpus
                        tokenIterator = null;
                    } else {
                        chunkIterator = sentenceIterator.next().iterator();
                    }
                }
                if (tokenIterator != null && chunkIterator.hasNext()) {
                    tokenIterator = chunkIterator.next().iterator();
                }
            }
        }

        if (tokenIterator != null && tokenIterator.hasNext() && token == null) {
            token = tokenIterator.next();
        }

        return token;

    }
}
