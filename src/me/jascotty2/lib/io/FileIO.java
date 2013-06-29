/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com> Description:
 * methods for reading/writing files
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package me.jascotty2.lib.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FileIO {

	/**
	 * this is a quote within a csv string <br /> covers “ ” (8220 - 8221)
	 */
	protected final static char nullQuo = (char) 65533;
	//protected final static String nullQuo = String.valueOf((char) 226) + (char) 128 + String.valueOf((char) 156);
	/**
	 * recognized delimeters
	 */
	protected final static ArrayList<Character> delimeters = new ArrayList<Character>() {
		{
			// fields can be tab, comma, or semicolin -delimited
			add('\t');
			add(',');
			add(';');
		}
	};

	public static List<String[]> loadCSVFile(File toLoad) throws FileNotFoundException, IOException {
		ArrayList<String[]> ret = new ArrayList<String[]>();
		if (toLoad.exists() && toLoad.isFile() && toLoad.canRead()) {
			char delim = getDelim(toLoad);
			String line;
			FileReader fstream = new FileReader(toLoad.getAbsolutePath());
			BufferedReader in = new BufferedReader(fstream);
			try {
				if (delim == 0) {
					while ((line = in.readLine()) != null) {
						ret.add(new String[]{line});
					}
				} else {
					while ((line = in.readLine()) != null) {
						if (line.contains("\"")) {
							// need to parse the strings..
							ArrayList<String> fields = new ArrayList<String>();
							boolean inStr = false;
							int start = 0;
							char c;
							for (int i = 0; i < line.length() && (c = line.charAt(i)) != 0; ++i) {
								if (c == '"') {
									inStr = !inStr;
								} else if (!inStr && c == delim) {
									String field = line.substring(start, i);
									if (!field.isEmpty() && field.charAt(0) == '"') {
										field = field.substring(1, field.length() - 1);
									}
									fields.add(field.replace("\"\"", "\"").replace(nullQuo, '"'));
									start = i + 1;
								}
							}
							if (start < line.length()) {
								String field = line.substring(start, line.length());
								if (!field.isEmpty() && field.charAt(0) == '"') {
									field = field.substring(1);
								}
								// just in case is missing the terminating quote
								if (!field.isEmpty() && field.charAt(field.length() - 1) == '"') {
									field = field.substring(0, field.length() - 1);
								}
								fields.add(field.replace("\"\"", "\"").replace(nullQuo, '"'));
							}
							ret.add(fields.toArray(new String[0]));
						} else {
							// simple substitution :)
							ret.add(line.replace(nullQuo, '"').split(String.valueOf(delim)));
						}
					}
				}
			} finally {
				in.close();
				fstream.close();
			}
		}
		return ret;
	}

	private static char getDelim(File toLoad) throws IOException {
		FileReader fstream = new FileReader(toLoad);
		int inC;
		char delim = 0;
		try {
			boolean inStr = false;
			while ((inC = fstream.read()) != -1) {
				if (((char) inC) == '"') {
					inStr = !inStr;
				} else if (!inStr && delimeters.contains((Character) (char) inC)) {
					delim = (char) inC;
					break;
				}
			}
			fstream.close();
		} catch (IOException e) {
			fstream.close();
			throw (e);
		}
		return delim;
	}

	public static String readFile(File toLoad) throws FileNotFoundException, IOException {
		StringBuilder ret = new StringBuilder();
		if (toLoad.exists() && toLoad.isFile() && toLoad.canRead()) {
			FileReader fstream = new FileReader(toLoad.getAbsolutePath());
			BufferedReader in = new BufferedReader(fstream);
			try {
				String line;
				while ((line = in.readLine()) != null) {
					ret.append(line).append("\n");
				}
			} finally {
				in.close();
				fstream.close();
			}
		}
		return ret.toString();
	}

	public static List<String> loadFile(File toLoad) throws FileNotFoundException, IOException {
		ArrayList<String> ret = new ArrayList<String>();
		if (toLoad.exists() && toLoad.isFile() && toLoad.canRead()) {
			//FileReader fstream = new FileReader(toLoad.getAbsolutePath());
			//BufferedReader in = new BufferedReader(fstream);
			FileInputStream fstream = new FileInputStream(toLoad.getAbsolutePath());
			InputStreamReader inr = new InputStreamReader(fstream, "UTF8");
			BufferedReader in = new BufferedReader(inr);
			try {
				String line;
				while ((line = in.readLine()) != null) {
					ret.add(line);
				}
			} finally {
				in.close();
				inr.close();
				fstream.close();
			}
		}
		return ret;
	}

	public static void saveFile(File toSave, String data) throws IOException {
		if (!toSave.exists()) {
			// first check if directory exists, then create the file
			File dir = new File(toSave.getAbsolutePath().substring(0, toSave.getAbsolutePath().lastIndexOf(File.separatorChar)));
			dir.mkdirs();
			toSave.createNewFile();
		}
		FileWriter fstream = new FileWriter(toSave.getAbsolutePath());
		BufferedWriter out = new BufferedWriter(fstream);
		out.write(data);
		out.close();
		fstream.close();
	}

	public static void appendToFile(File toSave, String data) throws IOException {
		if (!toSave.exists()) {
			// first check if directory exists, then create the file
			File dir = new File(toSave.getAbsolutePath().substring(0, toSave.getAbsolutePath().lastIndexOf(File.separatorChar)));
			dir.mkdirs();
			toSave.createNewFile();
		}
		FileWriter fstream = new FileWriter(toSave.getAbsolutePath(), true);
		BufferedWriter out = new BufferedWriter(fstream);
		out.write(data);
		out.close();
		fstream.close();
	}
	
	public static void saveFile(File toSave, String[] lines) throws IOException {
		if (!toSave.exists()) {
			// first check if directory exists, then create the file
			File dir = new File(toSave.getAbsolutePath().substring(0, toSave.getAbsolutePath().lastIndexOf(File.separatorChar)));
			dir.mkdirs();
			toSave.createNewFile();
		}
		FileWriter fstream = new FileWriter(toSave.getAbsolutePath());
		BufferedWriter out = new BufferedWriter(fstream);
		for (int i = 0; i < lines.length; ++i) {
			out.write(lines[i]);
			if (i + 1 < lines.length) {
				out.newLine();
			}
		}
		out.close();
		fstream.close();
	}

	public static void saveFile(File toSave, ArrayList<String> lines) throws IOException {
		if (!toSave.exists()) {
			// first check if directory exists, then create the file
			File dir = new File(toSave.getAbsolutePath().substring(0, toSave.getAbsolutePath().lastIndexOf(File.separatorChar)));
			dir.mkdirs();
			toSave.createNewFile();
		}
		FileWriter fstream = new FileWriter(toSave.getAbsolutePath());
		BufferedWriter out = new BufferedWriter(fstream);

		Iterator<String> toWrite = lines.iterator();
		while (toWrite.hasNext()) {
			out.write(toWrite.next());
			if (toWrite.hasNext()) {
				out.newLine();
			}
		}
		out.close();
		fstream.close();
	}

	public static void saveCSVFile(File toSave, ArrayList<String[]> lines) throws IOException {
		if (!toSave.exists()) {
			// first check if directory exists, then create the file
			File dir = new File(toSave.getAbsolutePath().substring(0, toSave.getAbsolutePath().lastIndexOf(File.separatorChar)));
			dir.mkdirs();
			toSave.createNewFile();
		}
		FileWriter fstream = new FileWriter(toSave);
		BufferedWriter out = new BufferedWriter(fstream);
		Iterator<String[]> toWrite = lines.iterator();
		String line[];
		while (toWrite.hasNext()) {
			if ((line = toWrite.next()) == null) {
				continue;
			}
			for (int i = 0; i < line.length; ++i) {
				boolean str = line[i].contains("\"");
				if (!str) {
					for (Character c : delimeters) {
						if (line[i].contains(String.valueOf(c))) {
							str = true;
							break;
						}
					}
				}
				if (str) {
					out.write("\"" + line[i].replace("\"", "\"\"") + "\"");
				} else {
					out.write(line[i].replace('"', nullQuo));
				}
				// slower:
//				if (Pattern.matches(fpRegex, line[i])) { // (fpRegex as defined from Double documentation)
//					out.write(line[i]);
//				} else {
//					out.write("\"" + line[i].replace("\"", "\"\"") + "\"");
//				}
				if (i + 1 < line.length) {
					out.write(",");
				}
			}
			if (toWrite.hasNext()) {
				out.newLine();
			}
		}
		out.close();
		fstream.close();
	}

	public static File getJarFile(Class jarClass) {
		return new File(jarClass.getProtectionDomain().getCodeSource().getLocation().getPath().
				replace("%20", " ").replace("%25", "%"));
	}

	/**
	 * parses the given filename string for the file extension
	 *
	 * @param filename string to parse
	 * @return extension, beginning with the dot (eg. ".jar")
	 */
	public static String getExtension(File file) {
		return getExtension(file.getAbsolutePath());
	}

	/**
	 * parses the given filename string for the file extension
	 *
	 * @param filename string to parse
	 * @return extension, beginning with the dot (eg. ".jar")
	 */
	public static String getExtension(String filename) {
		if (filename != null) {
			int dot = filename.lastIndexOf(".");
			if (dot > 0 && dot > filename.lastIndexOf(File.separator)) {
				return filename.substring(dot);
			}
		}
		return "";
	}

	public static enum OVERWRITE_CASE {

		NEVER, IF_NEWER, ALWAYS
	}

	public static void extractResource(String path, File writeTo, Class jarClass) throws Exception {
		extractResource(path, writeTo, jarClass, OVERWRITE_CASE.ALWAYS);
	}

	/**
	 * extract a file from the jar archive
	 *
	 * @param path path to the resource, relative to the jar root
	 * @param writeTo path to write to. if doesn't exist, will create directory
	 * if there is not a matching file extension
	 * @param jarClass class in the jar with the resource to extract
	 * @param overwrite what cases should the file be overwriten, if it exists
	 * @throws Exception
	 */
	public static void extractResource(String path, File writeTo, Class jarClass, OVERWRITE_CASE overwrite) throws Exception {
		if (!writeTo.exists()) {
			if (!getExtension(path).equalsIgnoreCase(getExtension(writeTo))) {
				writeTo.mkdirs();

			} else if (writeTo.getParentFile() != null) {
				// ensure parent dirs exist
				writeTo.getParentFile().mkdirs();
			}
		}
		if (writeTo.isDirectory()) {
			String fname = new File(path).getName();
			writeTo = new File(writeTo, fname);
		}
		// check if the file exists and is newer than the JAR
		File jarFile = getJarFile(jarClass);
		if (writeTo.exists()) {
			if (overwrite == OVERWRITE_CASE.NEVER) {
				return;
			} else if (overwrite == OVERWRITE_CASE.IF_NEWER
					&& writeTo.lastModified() >= jarFile.lastModified()) {
				return;
			}
		}

		Exception err = null;
//		// works 1-time, but not after reloading updated plugin...
//		InputStream input = jarClass.getResourceAsStream(path.startsWith("/") ? path : "/" + path);
//		if (input == null) {
//			throw new java.io.FileNotFoundException("Could not find '" + path + "' in " + jarFile.getAbsolutePath());
//		}
//		FileOutputStream output = null;
//		try {
//			System.out.println("writing " + writeTo.getAbsolutePath());
//			output = new FileOutputStream(writeTo);
//			byte[] buf = new byte[8192];
//			int length;
//
//			while ((length = input.read(buf)) > 0) {
//				output.write(buf, 0, length);
//			}
//			
//		} catch (Exception e) {
//			err = e;
//		}
		OutputStream output = null;
		InputStream input = null;
		try {
			// Got to jump through hoops to ensure we can still pull messages from a JAR
			// file after it's been reloaded...
			URL res = jarClass.getResource(path.startsWith("/") ? path : "/" + path);
			if (res == null) {
				throw new java.io.FileNotFoundException("Could not find '" + path + "' in " + jarFile.getAbsolutePath());
			}
			URLConnection resConn = res.openConnection();
			resConn.setUseCaches(false);
			input = resConn.getInputStream();

			if (input == null) {
				throw new java.io.IOException("can't get input stream from " + res);
			} else {
				output = new FileOutputStream(writeTo);
				byte[] buf = new byte[8192];
				int len;
				while ((len = input.read(buf)) > 0) {
					output.write(buf, 0, len);
				}
			}
		} catch (Exception ex) {
			err = ex;
		} finally {
			try {
				if (input != null) {
					input.close();
				}
			} catch (Exception e) {
			}
			try {
				if (output != null) {
					output.close();
				}
			} catch (Exception e) {
			}
		}
		if (err != null) {
			throw err;
		}
	}

	public static InputStream openResource(String path, Class jarClass) throws FileNotFoundException, IOException {
		InputStream in;
		URL res = jarClass.getResource(path.startsWith("/") ? path : "/" + path);
		if (res == null) {
			throw new java.io.FileNotFoundException("Could not find '" + path + "' in " + getJarFile(jarClass).getAbsolutePath());
		}
		URLConnection resConn = res.openConnection();
		resConn.setUseCaches(false);
		in = resConn.getInputStream();
		if (in == null) {
			throw new java.io.FileNotFoundException("Could not get input stream from " + res);
		}
		return in;
	}

	public static String loadResource(String path, Class jarClass) throws FileNotFoundException, IOException {
		InputStream in = openResource(path, jarClass);
		
		StringWriter writer = new StringWriter();
		InputStreamReader inr = null;
		try {
			inr = new InputStreamReader(in);
			char[] buffer = new char[4096];
			int n = 0;
			while (-1 != (n = inr.read(buffer))) {
				writer.write(buffer, 0, n);
			}
		} finally {
			if(inr != null) {
				inr.close();
			}
			writer.close();
		}
		return writer.toString();
	}
}
