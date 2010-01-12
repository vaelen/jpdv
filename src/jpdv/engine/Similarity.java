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

import Jama.Matrix;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jpdv.vectorspace.CosineSimilarity;

public class Similarity {

    private String[] keys;
    private Matrix matrix;

    private Similarity() {}

    public void writeSimilarityMatrix(File file) throws FileNotFoundException {
        writeSimilarityMatrix(new PrintWriter(file));
    }

    public void writeSimilarityMatrix(File file, String encoding) throws FileNotFoundException, UnsupportedEncodingException {
        writeSimilarityMatrix(new PrintWriter(file, encoding));
    }
    
    public void writeSimilarityMatrix(PrintWriter out) {
        CosineSimilarity similarity = new CosineSimilarity();
        Matrix similarityMatrix = similarity.transform(matrix);
        double[][] sim = similarityMatrix.getArray();
        // Print Header
        out.printf("%s", "WORD");
        for(int i = 0; i < sim.length; i++) {
            String key = keys[i];
            out.printf("\t%s", key);
        }
        out.println();
        out.flush();
        for(int i = 0; i < sim.length; i++) {
            String key = keys[i];
            out.printf("%s", key);
            for(int j = 0; j < sim[i].length; j++) {
                out.printf("\t%.15f", sim[i][j]);
            }
            out.println();
            out.flush();
        }
    }

    public static Similarity fromFile(File file) throws FileNotFoundException, IOException {
        Similarity vectorSpace = null;
        try {
            vectorSpace = fromFile(file, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Similarity.class.getName()).log(Level.SEVERE, "UTF-8 Not Supported", ex);
        }
        return vectorSpace;
    }

    public static Similarity fromFile(File file, String encoding) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        Similarity space = new Similarity();
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
        // The first line is a header
        String header = in.readLine();
        String[] headerParts = header.split("\t");
        int rows = headerParts.length - 1;
        String line = in.readLine();
        int index = 0;
        ArrayList<Object> list = new ArrayList<Object>();
        ArrayList<String> keys = new ArrayList<String>();
        while(line != null) {
            String[] columns = line.split("\t");
            String key = columns[0];
            keys.add(key);
            double[] vec = new double[columns.length - 1];
            for (int i = 1; i < columns.length; i++) {
                vec[i - 1] = Double.parseDouble(columns[i]);
            }
            list.add(vec);
            line = in.readLine();
            index++;
        }

        double[][] m = new double[index][];
        for(int i = 0; i < index; i++) {
            m[i] = (double[]) list.get(i);
        }

        double[][] m2 = new double[rows][index];
        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < index; j++) {
                m2[i][j] = m[j][i];
            }
        }

        space.keys = keys.toArray(new String[]{});
        space.matrix = new Matrix(m2);
        return space;
    }

    public static void main(String[] args) throws Exception {
        Logger logger = Logger.getLogger(Similarity.class.getName());
        if (args.length < 1) {
            logger.log(Level.SEVERE, "Please supply a filename to load,");
            System.exit(1);
        }
        File file = new File(args[0]);
        if (!file.exists()) {
            logger.log(Level.SEVERE, String.format("File doesn't exist: %s", file.getAbsolutePath()));
            System.exit(1);
        }
        if (!file.canRead()) {
            logger.log(Level.SEVERE, String.format("File is not readable: %s", file.getAbsolutePath()));
            System.exit(1);
        }
        String path = file.getAbsolutePath();
        logger.log(Level.INFO, String.format("Loading Vector Space File: %s", file.getAbsolutePath()));
        Similarity space = Similarity.fromFile(file);
        String newPath = String.format("%s.similarity", path);
        File newFile = new File(newPath);
        logger.log(Level.INFO, String.format("Writing Similarity File: %s", newFile.getAbsolutePath()));
        space.writeSimilarityMatrix(newFile);
    }

}
