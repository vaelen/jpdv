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
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static final void main(String[] args) {
        Logger logger = Logger.getLogger(Main.class.getName());

        int contextSize = 5;

        if(args.length < 1) {
            logger.log(Level.SEVERE, "Please supply a filename to load,");
            System.exit(1);
        }

        File file = new File(args[0]);
        if(!file.exists()) {
            logger.log(Level.SEVERE, String.format("File doesn't exist: %s", file.getAbsolutePath()));
            System.exit(1);
        }
        if (!file.canRead()) {
            logger.log(Level.SEVERE, String.format("File isn't readable: %s", file.getAbsolutePath()));
            System.exit(1);
        }

        List<BaseForm> targets = BaseForm.getInstances("欠く", "世紀", "社会", "コンピュータ", "ため");
        if(args.length > 1) {
            File targetsFile = new File(args[1]);
            if (!targetsFile.exists()) {
                logger.log(Level.WARNING, String.format("Targets file doesn't exist, using defaults: %s", targetsFile.getAbsolutePath()));
            } else if (!targetsFile.canRead()) {
                logger.log(Level.WARNING, String.format("Targets file isn't readable, using defaults: %s", targetsFile.getAbsolutePath()));
                System.exit(1);
            } else {
                try {
                    targets = BaseForm.getInstances(targetsFile);
                } catch (Exception ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, String.format("Couldn't read targets file: %s", ex.toString()), ex);
                }
            }
        }

        if(args.length > 2) {
            try {
                contextSize = Integer.parseInt(args[2]);
            } catch (Exception ex) {
                logger.log(Level.WARNING, String.format("Couldn't parse context size: %s, Using default value: %,d", args[1], contextSize));
            }
        }

        Corpus corpus = null;
        String path = file.getAbsolutePath();

        if(path.endsWith(".xml")) {
            try {
                logger.log(Level.INFO, String.format("Parsing Cabocha XML File: %s", file.getAbsolutePath()));
                double startTime = new Date().getTime();
                corpus = Corpus.parseXML(new File(args[0]));
                double endTime = new Date().getTime();
                logger.log(Level.INFO, String.format("Total Parse Time: %,.3f seconds", (endTime - startTime) / 1000.0));
                path = path.substring(0, path.length() - 4);
                File binaryFile = new File(String.format("%s.corpus", path));
                logger.log(Level.INFO, String.format("Writing Binary File: %s", binaryFile.getAbsolutePath()));
                corpus.writeBinary(binaryFile);
            } catch (Exception ex) {
                Logger.getLogger(Main.class.getName()).log(Level.WARNING, String.format("Couldn't Write Binary File: %s", ex.toString()), ex);
            }
        } else if(path.endsWith(".corpus")) {
            logger.log(Level.INFO, String.format("Loading Binary File: %s", file.getAbsolutePath()));
            path = path.substring(0, path.length() - 7);
            try {
                double startTime = new Date().getTime();
                corpus = Corpus.fromBinary(file);
                double endTime = new Date().getTime();
                logger.log(Level.INFO, String.format("Total Load Time: %,.3f seconds", (endTime - startTime) / 1000.0));
            } catch (Exception ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, String.format("Couldn't Load Binary File: %s", file.getAbsolutePath()), ex);
                System.exit(1);
            }
        } else {
            logger.log(Level.SEVERE, String.format("File Type Unknown: %s", file.getAbsolutePath()));
        }
        corpus.printStats(new PrintWriter(System.out));

        // Context Space
        {
            logger.log(Level.INFO, String.format("Generating Context Space, Context Size: %,d", contextSize));
            ContextSpace contextSpace = new ContextSpace(corpus, contextSize);
            contextSpace.generateSpace(targets);
            File contextSpaceFile = new File(String.format("%s.context-%d", path, contextSize));
            logger.log(Level.INFO, String.format("Writing Context Space: %s", contextSpaceFile.getAbsolutePath()));
            try {
                contextSpace.writeSpace(contextSpaceFile);
            } catch (FileNotFoundException ex) {
                logger.log(Level.SEVERE, String.format("Couldn't Write Context Space: %s", ex.toString()), ex);
            }
        }

        // Dependency Space
        {
            logger.log(Level.INFO, "Generating Dependency Space");
            DependencySpace dependencySpace = new DependencySpace(corpus);
            dependencySpace.generateSpace(targets);
            File dependencySpaceFile = new File(String.format("%s.dependency", path));
            logger.log(Level.INFO, String.format("Writing Dependency Space: %s", dependencySpaceFile.getAbsolutePath()));
            try {
                dependencySpace.writeSpace(dependencySpaceFile);
            } catch (FileNotFoundException ex) {
                logger.log(Level.SEVERE, String.format("Couldn't Write Dependency Space: %s", ex.toString()), ex);
            }
        }

    }

}
