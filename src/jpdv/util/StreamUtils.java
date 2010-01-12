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

package jpdv.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrew Young <andrew at vaelen.org>
 */
public class StreamUtils {
    public static final String UNICODE_CHARSET_NAME = "UTF-8";
    public static Charset DEFAULT_CHARSET;
    static {
        setDefaultCharset(UNICODE_CHARSET_NAME);
    }

    public static Charset getCharsetOrDefault(String charsetName) {
        Charset charset = null;
        if(Charset.isSupported(charsetName)) {
            charset = Charset.forName(charsetName);
        } else {
            charset = Charset.defaultCharset();
        }
        return charset;
    }

    public static void setDefaultCharset(String charsetName) {
        DEFAULT_CHARSET = getCharsetOrDefault(charsetName);
    }

    public static URL getURL(URI uri) {
        URL url = null;
        try {
            url = uri.toURL();
        } catch (MalformedURLException ex) {
            Logger.getLogger(StreamUtils.class.getName()).log(Level.SEVERE, String.format("Malformed URL: %s", uri.toString()), ex);
        }
        return url;
    }

    public static String read(URI uri) {
        return read(getURL(uri));
    }

    public static String read(URI uri, String charsetName) {
        return read(getURL(uri), charsetName);
    }

    public static String read(URI uri, Charset charset) {
        return read(getURL(uri), charset);
    }

    public static String read(URL url) {
        return read(url, DEFAULT_CHARSET);
    }

    public static String read(URL url, String charsetName) {
        Charset charset = getCharsetOrDefault(charsetName);
        return read(url, charset);
    }

    public static String read(URL url, Charset charset) {
        String value = "";
        if(url != null) {
            try {
                value = read(url.openStream(), charset);
            } catch (IOException ex) {
                Logger.getLogger(StreamUtils.class.getName()).log(Level.SEVERE, String.format("Couldn't Open Connection To URL: %s", url.toString()), ex);
            }
        } else {
            Logger.getLogger(StreamUtils.class.getName()).log(Level.SEVERE, "URL Not Provided or Malformed");
        }
        return value;
    }

    public static String read(File file) {
        return read(file, DEFAULT_CHARSET);
    }

    public static String read(File file, String charsetName) {
        Charset charset = getCharsetOrDefault(charsetName);
        return read(file, charset);
    }

    public static String read(File file, Charset charset) {
        String value = "";
        if(file != null) {
            try {
                value = read(new FileInputStream(file), charset);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(StreamUtils.class.getName()).log(Level.SEVERE, String.format("File Not Found: %s", file.getAbsolutePath()), ex);
            }
        } else {
            Logger.getLogger(StreamUtils.class.getName()).log(Level.SEVERE, "File Not Provided");
        }
        return value;
    }
    
    public static String read(InputStream stream) {
        return read(stream, DEFAULT_CHARSET);
    }

    public static String read(InputStream stream, String charsetName) {
        Charset charset = getCharsetOrDefault(charsetName);
        return read(stream, charset);
    }

    public static String read(InputStream stream, Charset charset) {
        String value = "";
        if(stream != null) {
            value = read(new InputStreamReader(stream, charset));
        } else {
            Logger.getLogger(StreamUtils.class.getName()).log(Level.SEVERE, "Stream Not Provided");
        }
        return value;
    }

    public static String read(Reader reader) {
        StringWriter sw = new StringWriter();
        if (reader != null) {
            try {
                BufferedReader in = new BufferedReader(reader);
                PrintWriter out = new PrintWriter(sw);
                String line = in.readLine();
                while (line != null) {
                    out.println(line);
                    line = in.readLine();
                }
                out.flush();
                sw.flush();
            } catch (IOException ex) {
                Logger.getLogger(StreamUtils.class.getName()).log(Level.SEVERE, "Couldn't Read From Stream", ex);
            }
        } else {
            Logger.getLogger(StreamUtils.class.getName()).log(Level.SEVERE, "Reader Not Provided");
        }
        return sw.toString();
    }
}
