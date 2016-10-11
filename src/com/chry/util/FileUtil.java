package com.chry.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.chry.util.http.HttpClient;

public class FileUtil {
	static Logger logger = LogManager.getLogger(HttpClient.class.getName());

	static public boolean exists(String path) {
        File f = new File(path);
        return f.exists();
    }

    static public void renameFile(String newName, String oldName) throws IOException {
        if (File.separatorChar == '\\') {
            copyFile(oldName, newName);
            File oldF = new File(oldName);
            oldF.delete();
        }
        else {
            File oldF = new File(oldName);
            File newF = new File(newName);
            if (newF.exists()) {
                newF.delete();
            }
            if (!oldF.renameTo(newF)) {
                throw new IOException(String.format("Unable to rename %s to %s", oldName, newName));
            }
        }
    }

    static public void copyFile(String src, String dst) throws IOException {
        FileOutputStream fos = null;
        FileInputStream fis = null;

        try {
            fos = new FileOutputStream(dst);
            fis = new FileInputStream(src);
            byte[] buf = new byte[4096];

            int nread = fis.read(buf);
            while (nread > 0) {
                fos.write(buf, 0, nread);
                nread = fis.read(buf);
            }
        }
        catch (IOException e) {
            File f = new File(dst);
            if (f.exists()) {
                f.delete();
            }

            throw e;
        }
        finally {
            if (fis != null) {
                try {
                    fis.close();
                }
                catch (Exception e) {
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                }
                catch (Exception e) {
                }
            }
        }
    }

    static public String readFileToString(String fileName) throws IOException {
        String ret = "";
        BufferedReader br = null;
        try {
        	  br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));  
             String line = null;  
             while( ( line = br.readLine() ) != null ) {
            	 ret += line + "\n";
             }
        } finally {
        	if (br != null) {
        		try {
					br.close();
				} catch (IOException e) {
				}
        	}
        }
        return ret;
    }

    static public void deleteDirectory(File dir) {
        File[] children = dir.listFiles();

        if(children != null) {
            for (File f : children) {
                if (!f.isDirectory()) {
                    f.delete();
                }
                else {
                    deleteDirectory(f);
                }
            }
        }


        dir.delete();
    }


    static public void deleteDirectory(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        deleteDirectory(file);
    }


    static public void renameDirectory(String newDir, String oldDir) {
        File ofile = new File(oldDir);
        File nfile = new File(newDir);
        ofile.renameTo(nfile);
    }


    static public void createDirectory(String path) throws IOException {
        File f = new File(path);
        f.mkdir();
    }


    static public boolean deleteFile(String path) {
        File f = new File(path);
        return f.delete();
    }


    static public void createFile(String path) throws IOException {
    	File file = new File(path); 
    	if (!file.exists()) {
    		file.createNewFile(); 
    	} 
    }

    static public void hideDirectory(String path) throws IOException {
    	String string=" attrib +H  " + path;
    	Runtime.getRuntime().exec(string);
    }

    static public void WriteStringToFile(String s, String filePath) throws IOException {
    	PrintStream ps = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(file),"UTF-8");
            BufferedWriter writer = new BufferedWriter(write);   
            writer.write(s);
            writer.close();
        } finally {
        	if (ps != null) {
        		ps.close();
        	}
        }
    }    
}
