package com.api.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

import com.api.exception.CustomException;


public class DataStoreDao {

	/**method to create default directory in the local system when one is not provided
	 * 
	 * @return Path of the Created directory
	 */
	public static synchronized Path createDefaultFolder() {
		String path = null;
		File file = null;
		boolean create = false;
		try {
			/**
			 * uses system directory where the code is running 
			 */
			path = System.getProperty("user.dir");

			file = new File(path + "/testDir");

			create = file.mkdirs();

			if (file != null) {
				return file.toPath();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		if (create)
			return file.toPath();
		else
			return null;

	}

	/** when a directory is given it is checked whether the directory exists or not
	 * 
	 * @param path
	 * @return boolean 
	 */
	public static synchronized boolean CheckDirectoryExits(String path) {

		File file = new File(path);

		if (file.isDirectory()) {
			return true;
		}
		return false;

	}

	
	/**returns the size of the folder being used
	 *  
	 * @param path
	 * @return double size in GB
	 */
	public static synchronized double folderSize(String path) {

		final AtomicLong size = new AtomicLong(0);
		Path dirPath = new File(path).toPath();

		try {
			Files.walkFileTree(dirPath, new SimpleFileVisitor<Path>() {

				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {

					size.addAndGet(attrs.size());
					return FileVisitResult.CONTINUE;
				}

				public FileVisitResult visitFileFailed(Path file, IOException exc) {
					// Skip folders that can't be traversed
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
					// Ignore errors traversing a folder
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			throw new AssertionError("FilePath does not");
		}

		return size.get() / 1073741824;
	}

	/**
	 * if  directory is provided and that does not exists 
	 * it creates all the directories named by its abstract path names
	**/
	public static synchronized boolean createDirectory(String path) {
		File file = null;
		boolean create = false;
		try {
			synchronized (file) {
				file = new File(path.toString());
				create = file.mkdirs();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return create;

	}
	
	/**
	 * @return boolean 
	 * @param Key String format, data in string format and the path where it needs to be saved
	 * 
	 * This used java.nio API for locking the file to be thread safe 
	 * when multiple systems try to access it.
	 * 
	 * After successful finally lock is been released and set free for other resources to access
	 */
	public static synchronized boolean createFile(String key, String data, String path) {

		boolean flag = false;
		RandomAccessFile file = null;
		FileChannel channel = null;
		FileLock lock = null;
		try {

			file = new RandomAccessFile(path + "/" + key + ".file", "rw");
			channel = file.getChannel();

			try {
				lock = channel.tryLock();
			} catch (final OverlappingFileLockException e) {
				file.close();
				channel.close();
			}

			file.writeBytes(data);
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (lock != null)
					lock.release();
				if (file != null)
					file.close();
				if (channel != null)
					channel.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return flag;
	}

	/**
	 * @return String data red from the file
	 * This uses java.nio API to lock the file which is reading and will not allow others to access the same file.
	 * @throws CustomException 
	 */
	public static synchronized String readFile(String key, String path) throws CustomException {

		String data = null;
		FileLock fileLock = null;
		RandomAccessFile file = null;
		FileChannel fc = null;
		InputStream inputStream = null;
		BufferedReader streamReader = null;
		StringBuilder responseStrBuilder = null;

		try {
			file = new RandomAccessFile(path + "/" + key + ".file", "rw");
			fc = file.getChannel();
			fileLock = fc.tryLock();
			if (fileLock != null) {
				inputStream = Channels.newInputStream(fc);

				streamReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
				responseStrBuilder = new StringBuilder();

				String inputStr;
				while ((inputStr = streamReader.readLine()) != null)
					responseStrBuilder.append(inputStr);

				data = responseStrBuilder.toString();

			} else {
				throw new CustomException("CAn't access file. Another process is accessing the file. Please wait.");
			}
		} catch (OverlappingFileLockException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fileLock != null)
					fileLock.release();
				if (inputStream != null)
					inputStream.close();
				if (streamReader != null)
					streamReader.close();
				if (file != null)
					file.close();
				if (fc != null)
					fc.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return data;
	}

	public static synchronized boolean checkData(String data) {
		JSONParser parser = new JSONParser();
		try {
			Object obj = parser.parse(data);
			if (obj instanceof JSONObject) {
				return true;
			} else if (obj instanceof JSONArray) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	public static synchronized boolean delete(String key, String path) {

		try {
			File file = new File(path + "/" + key + ".file");

			synchronized (file) {
				FileUtils.forceDelete(file);
				return true;
			}
		} catch (Exception e) {
			e.getMessage();
		}
		return false;

	}

}
