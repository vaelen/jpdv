/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
