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

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class has three (or more) threads.
 * One thread puts new trees into the processing queue.
 * One (or more) thread(s) then pull items off the processing queue and process them.
 * A final thread updates the vector space itself with the results.
 * @author Andrew Young <andrew at vaelen.org>
 */
public class DependencySpace extends VectorSpace {

    public static final char PATH_SEPARATOR = '⇄';
    public static final int PROCESSING_QUEUE_CAPACITY = Integer.MAX_VALUE;
    public static final int UPDATE_QUEUE_CAPACITY = Integer.MAX_VALUE;


    protected final AtomicBoolean done = new AtomicBoolean(false);

    protected final Deque<Deque<Chunk>> processingQueue = new LinkedBlockingDeque<Deque<Chunk>>(PROCESSING_QUEUE_CAPACITY);
    protected final Deque<Deque<BaseForm>> updateQueue = new LinkedBlockingDeque<Deque<BaseForm>>(UPDATE_QUEUE_CAPACITY);

    @Override
    public void generateSpace(final Collection<BaseForm> targets) {



        SpaceUpdater spaceUpdater = new SpaceUpdater();
        spaceUpdater.start();

        Deque<Chunk> queue = new ArrayDeque<Chunk>();
    }

    private void findNextTarget(Set<BaseForm> targets) {
        Deque<Chunk> queue = new ArrayDeque<Chunk>();
    }

    private BaseForm createPathForm(Collection<BaseForm> path) {
        StringBuilder sb = new StringBuilder();
        for (BaseForm node : path) {
            sb.append(node.getValue());
            sb.append(PATH_SEPARATOR);
        }
        // Delete the final extra path separator
        sb.deleteCharAt(sb.length() - 1);
        Logger logger = Logger.getLogger(DependencySpace.class.getName());
        if(logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, "Created Path: %s", sb.toString());
        }
        return BaseForm.getInstance(sb.toString());
    }

    private void buildPaths(BaseForm target, Chunk last, Chunk current, Deque<Chunk> stack, Deque<BaseForm> path, Deque<BaseForm> updates) {
        // Add our current path
        BaseForm pathForm = createPathForm(path);
        updates.add(pathForm);
        // Follow all children except the one we came from.
        for(Chunk child: current.getChildren()) {
            Token head = child.getHead();
            if(head != null) {
                Token func = child.getFunc();
                if (func != null) {
                    path.add(func.getBaseForm());
                }
                path.addLast(head.getBaseForm());
                buildPaths(target, current, child, null, path, updates);
                path.removeLast(); // Remove the headword
                if(func != null) {
                    path.removeLast(); // Remove the edge
                }
            }
        }

        if (stack != null) {
            // When decending to children, the stack is null to prevent duplicate work.
            if (stack.isEmpty()) {
                // This is the root node of the sentence.
                path.addLast(Sentence.SENTENCE_SEPARATOR.getBaseForm());
                BaseForm rootPathForm = createPathForm(path);
                updates.add(rootPathForm);
                path.removeLast();
            } else {
                // Now process the parent
                Chunk parent = stack.pop();
                Token head = parent.getHead();
                if(head != null) {
                    Token func = current.getFunc();
                    if (func != null) {
                        path.add(func.getBaseForm());
                    }
                    path.addLast(head.getBaseForm());
                    buildPaths(target, current, parent, stack, path, updates);
                    path.removeLast(); // Remove the headword
                    if(func != null) {
                        path.removeLast();  // Remove the edge
                    }
                }
            }
        }
    }


    /** This class processes pending chunks, finding all possible paths from the target. */
    private class ChunkProcessor extends Thread {

        private static final long SLEEP_TIME = 5000L;

        private final Collection<BaseForm> targets;

        public ChunkProcessor(Collection<BaseForm> targets) {
            this.targets = targets;
        }

        @Override
        public void run() {
            while (!done.get()) {
                Deque<Chunk> stack = processingQueue.pollFirst();
                if(stack == null) {
                    try {
                        sleep(SLEEP_TIME);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(DependencySpace.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else if (!stack.isEmpty()) {
                    // Get the root chunk off the stack
                    final Chunk root = stack.pop();
                    // Find all targets in the root chunk.
                    for(Token token: root) {
                        BaseForm baseForm = token.getBaseForm();
                        if(targets.contains(baseForm)) {
                            // Found a target, use it.
                            Deque<BaseForm> path = new ArrayDeque<BaseForm>();
                            path.addLast(baseForm);
                            Deque<BaseForm> updates = new ArrayDeque<BaseForm>();
                            buildPaths(baseForm, root, root, stack, path, updates);
                            // Add updates to the update queue.
                            updateQueue.addLast(updates);
                        }
                    }
                }

            }
        }
    }

    /** This class posts pending updates to the vector space in a thread-safe manner. */
    private class SpaceUpdater extends Thread {

        private static final long SLEEP_TIME = 5000L;

        @Override
        public void run() {
            while (!done.get()) {
                Deque<BaseForm> update = updateQueue.pollFirst();
                if(update == null) {
                    try {
                        sleep(SLEEP_TIME);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(DependencySpace.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    // Perform update, first element is the key.
                    BaseForm key = update.removeFirst();
                    incrementCount(key, update);
                }

            }
        }
    }

}
