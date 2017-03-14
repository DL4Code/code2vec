package edu.lu.uni.util;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileHelper {
	
	private static Logger log = LoggerFactory.getLogger(FileHelper.class);
	
	public static void renameFile() {
		List<File> files = getAllDirectories("outputData/cnn/");
		files.addAll(getAllDirectories("outputData/encoder/"));
		
//		for (File file : files) {
//			
//			String fileName = file.getPath();
//			if ("RAW_CAMEL_TOKENIATION".equals(file.getName())) {
//				//1
//				file.renameTo(new File(fileName.replace(file.getName(), "1")));
//			} else if ("SIMPLIFIED_NLP".equals(file.getName())) {
//				//2
//				file.renameTo(new File(fileName.replace(file.getName(), "2")));
//			} else if ("SIMPLIFIED_NLP(2)".equals(file.getName())) {
//				//3
//				file.renameTo(new File(fileName.replace(file.getName(), "3")));
//			} else if ("TOKENAZATION_WITH_NLP".equals(file.getName())) {
//				//4
//				file.renameTo(new File(fileName.replace(file.getName(), "4")));
//			} else if ("TOKENAZATION_WITH_NLP(2)".equals(file.getName())) {
//				//5
//				file.renameTo(new File(fileName.replace(file.getName(), "5")));
//			}
//		}
	}
	
	public static List<File> getAllDirectories(String filePath) {
		return listAllDirectories(new File(filePath));
	}

	/**
	 * Recursively list all files in file.
	 * 
	 * @param file
	 * @return
	 */
	private static List<File> listAllDirectories(File file) {
		List<File> fileList = new ArrayList<>();
		
		File[] files = file.listFiles();
		
		for (File f : files) {
			if (f.isDirectory()) {
				fileList.add(f);
				fileList.addAll(listAllDirectories(f));
			} 
		}
		
		return fileList;
	}
	
	public static void deleteDirectory(String dir) {
		File file = new File(dir);
		
		if (file.exists()) {
			if (file.isDirectory()) {
				File[] files = file.listFiles();
				if (files.length > 0) {
					for (File f : files) {
						if (f.isFile()) {
							deleteFile(f.getAbsolutePath());
						} else {
							deleteDirectory(f.getAbsolutePath());
						}
					}
				}
				file.delete();
			} else {
				deleteFile(dir);
			}
		}
	}
	
	public static void deleteFiles(String dir) {
		File file = new File(dir);
		
		if (file.exists()) {
			if (file.isDirectory()) {
				File[] files = file.listFiles();
				if (files.length > 0) {
					for (File f : files) {
						if (f.isFile()) {
							deleteFile(f.getAbsolutePath());
						} else {
							deleteFiles(f.getAbsolutePath());
						}
					}
				}
			} else {
				deleteFile(dir);
			}
		}
	}
	
	public static void deleteFile(String fileName) {
		File file = new File(fileName);
		
		if (file.exists()) {
			if (file.isFile()) {
				file.delete();
			} else {
				deleteDirectory(fileName);
			}
		} 
	}
	
	public static void addMethodNameToFeatures(String file, String folderPath, String fileType, String oldFileType, String newfileType) throws IOException {
		List<File> integerFeatureFiles = FileHelper.getAllFiles(folderPath, fileType);
		
		for (File integerFeatureFile : integerFeatureFiles) {
			String fileName = integerFeatureFile.getName();
			if (file.contains(fileName.substring(0, fileName.lastIndexOf(fileType)))) {
				addMethodName(file, integerFeatureFile, oldFileType, newfileType);
				break;
			}
		}
	}

	public static void addMethodName(String file, File integerFeatureFile, String oldFileType, String newfileType) throws IOException {
		FileInputStream fisFile = null;
		FileInputStream fisIntegerFeatureFile = null;
		Scanner scFile = null;
		Scanner scIntegerFeatureFile = null;
		StringBuilder content = new StringBuilder();

		try {
			fisFile = new FileInputStream(file);
			fisIntegerFeatureFile = new FileInputStream(integerFeatureFile);
			scFile = new Scanner(fisFile, "UTF-8");
			scIntegerFeatureFile = new Scanner(fisIntegerFeatureFile, "UTF-8");

			int index = 0;
			while (scFile.hasNext() && scIntegerFeatureFile.hasNext()) {
				String featureLine = scIntegerFeatureFile.nextLine();
				int indexOfHarshKey = featureLine.indexOf("#");
				
				if (indexOfHarshKey < 0) {
					log.error("The below raw feature is invalid!\n" + featureLine);
					continue;
				}
				content.append(featureLine.substring(0, indexOfHarshKey + 1) + scFile.nextLine() + "\n");
				
				index ++;
				if (index % 1000 == 0) {
					FileHelper.outputToFile(file.replace(oldFileType, newfileType), content);
					content.setLength(0);
				}
			}
			if (content.length() > 0) {
				FileHelper.outputToFile(file.replace(oldFileType, newfileType), content);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (scFile != null) {
				scFile.close();
				scFile = null;
			}
			if (scIntegerFeatureFile != null) {
				scIntegerFeatureFile.close();
				scIntegerFeatureFile = null;
			}
			if (fisFile != null) {
				fisFile.close();
				fisFile = null;
			}
			if (fisIntegerFeatureFile != null) {
				fisIntegerFeatureFile.close();
				fisIntegerFeatureFile = null;
			}
		}
	}
	
	public static void createFile(File file, String content) {
		FileWriter writer = null;
		BufferedWriter bw = null;

		try {
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			
			if (!file.exists()) {
				file.createNewFile();
			}
			writer = new FileWriter(file);
			bw = new BufferedWriter(writer);
			bw.write(content);
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(bw);
			close(writer);
		}
	}
	
	public static Scanner readFileByScanner(File file) {
		FileInputStream inputStream = null;
		Scanner scanner = null;

		try {
			inputStream = new FileInputStream(file);
			scanner = new Scanner(inputStream, "UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (scanner != null) {
				scanner.close();
				scanner = null;
			}
			close(inputStream);
		}
		
		return scanner;
	}

	public static String readFile(File file) {
		byte[] input = null;
		BufferedInputStream bis = null;
		
		try {
			
			bis = new BufferedInputStream(new FileInputStream(file));
			input = new byte[bis.available()];
			bis.read(input);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(bis);
		}
		
		String sourceCode = null;
		if (input != null) {
			sourceCode = new String(input);
		}
		
		return sourceCode;
	}
	
	/**
	 * Check whether a file path is valid or not.
	 * 
	 * @param path, file path.
	 * @return true, the file path is valid.
	 * 		   false, the file path is invalid.
	 */
	public static boolean isValidPath(String path) {
		File file = new File(path);
		
		if (file.exists()) {
			return true;
		}
		
		return false;
	}

	public static List<File> getAllFiles(String filePath, String type) {
		return listAllFiles(new File(filePath), type);
	}

	/**
	 * Recursively list all files in file.
	 * 
	 * @param file
	 * @return
	 */
	private static List<File> listAllFiles(File file, String type) {
		List<File> fileList = new ArrayList<>();
		
		File[] files = file.listFiles();
		
		for (File f : files) {
			if (f.isFile()) {
				if (f.toString().endsWith(type)) {
					fileList.add(f);
				}
			} else {
				fileList.addAll(listAllFiles(f, type));
			}
		}
		
		return fileList;
	}

	public static String getFileName(String filePath) {
		File file = new File(filePath);
		
		if (file.exists()) {
			return file.getName();
		} else {
			return null;
		}
	}

	public static String getParentFilePath(String filePath) {
		File file = new File(filePath);
		
		if (file.exists()) {
			return file.getParent();
		}
		
		return null;
	}

	public static void outputToFileCover(String fileName, StringBuilder content) {
		File file = new File(fileName);
		FileWriter writer = null;
		BufferedWriter bw = null;

		try {
			
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			if (!file.exists()) {
				file.createNewFile();
			}
			writer = new FileWriter(file, false);
			bw = new BufferedWriter(writer);
			bw.write(content.toString());
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(bw);
			close(writer);
		}
	}
	
	public static void outputToFile(String fileName, StringBuilder content) {
		File file = new File(fileName);
		FileWriter writer = null;
		BufferedWriter bw = null;

		try {
			
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			if (!file.exists()) {
				file.createNewFile();
			}
			writer = new FileWriter(file, true);
			bw = new BufferedWriter(writer);
			bw.write(content.toString());
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(bw);
			close(writer);
		}
	}

	private static void close(FileWriter writer) {
		try {
			if (writer != null) {
				writer.close();
				writer = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void close(BufferedWriter bw) {
		try {
			if (bw != null) {
				bw.close();
				bw = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void close(BufferedInputStream bis) {
		try {
			if (bis != null) {
				bis.close();
				bis = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static List<File> getAllFilesInCurrentFolder(String folderPath, String fileType) {
		List<File> fileList = new ArrayList<>();
		
		File file = new File(folderPath);
		File[] files = file.listFiles();
		
		for (File f : files) {
			if (f.isFile()) {
				if (f.toString().endsWith(fileType)) {
					fileList.add(f);
				}
			} 
		}
		
		return fileList;
	}

	private static void close(FileInputStream inputStream) {
		try {
			if (inputStream != null) {
				inputStream.close();
				inputStream = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void makeDirectory(String fileName) {
		deleteFile(fileName);
		File file = new File(fileName.substring(0, fileName.lastIndexOf("/")));
		if (!file.exists()) {
			file.mkdirs();
		}
	}

}
