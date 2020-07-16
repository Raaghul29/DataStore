package com.api.datastorage;

import com.api.dao.DataStoreDao;
import com.api.exception.CustomException;
import com.api.utility.InMemoryCache;

/**
 * The {@code DataStore} class provides services in supporting basic CRD(Create,
 * Read, Delete) operations on a key value based file system.
 * 
 * This data store is meant to be used as a local storage for one single process
 * on one laptop.
 * 
 * The data store will support the following functional requirements. 1. It can
 * be initialized using an optional file path. If one is not provided, it will
 * reliably create itself in a reasonable location on the laptop. 2. A new
 * key-value pair can be added to the data store using the Create operation. The
 * key is always a string - capped at 32chars. The value is always a JSON object
 * - capped at 16KB. 3. A Read operation on a key can be performed by providing
 * the key, and receiving the value in response, as a JSON object. 4. A Delete
 * operation can be performed by providing the key. 5. Every key supports
 * setting a Time-To-Live property when it is created. This property is
 * optional. Once the Time-To-Live for a key has expired, the key will no longer
 * be available for Read or Delete operations.
 * 
 * @author Raaghul Krishnakumar
 * 
 * @since 1.5 
 * 
 */

public class DataStore {

	/**
	 * Initialising cache when the code in initialised and being used.
	 */
	InMemoryCache cache = new InMemoryCache();

	/**
	 * Store the data with the key provided in default location on the laptop
	 * with infinite time duration which is referring to {@code DataStore}
	 * 
	 * @param key
	 *            the key capped at a length of 32 characters referring the
	 *            data.
	 * @param data
	 *            the actual data in the json format with respect to the key
	 *            provided.
	 * @since 1.5
	 */

	public synchronized void store(String key, String data) {
		try {
			create(key, data, null, 0);
		} catch (CustomException e) {
			System.out.println(e);
		}

	}

	/**
	 * Store the data with the key provided in a desired path provided on the
	 * laptop with expiring time duration of the data and the key which is
	 * referring to {@code DataStore}
	 * 
	 * @param key
	 *            the key capped at a length of 32 characters referring the
	 *            data.
	 * @param data
	 *            the actual data in the json format with respect to the key
	 *            provided.
	 * @param path
	 *            the location or the place where the data needs to be stored.
	 * @param timeToLive
	 *            the exiry time for the data being added to the file system of
	 *            type long.
	 *            
	 * @since 1.5
	 */

	public synchronized void store(String key, String data, String path, long timeToLive) {
		try {
			create(key, data, path, timeToLive);
		} catch (CustomException e) {
			System.out.println(e);

		}

	}

	/**
	 * Store the data with the key provided in default location on the laptop
	 * with expiring time duration of the data and the key which is referring to
	 * {@code DataStore}
	 * 
	 * @param key
	 *            the key capped at a length of 32 characters referring the
	 *            data.
	 * @param data
	 *            the actual data in the json format with respect to the key
	 *            provided.
	 * @param timeToLive
	 *            the exiry time for the data being added to the file system of
	 *            type long.
	 *            
	 * @since 1.5           
	 */
	public synchronized void store(String key, String data, long timeToLive) {
		try {
			create(key, data, null, timeToLive);
		} catch (CustomException e) {
			System.out.println(e);

		}

	}

	/**
	 * Store the data with the key provided in default location on the laptop
	 * with nfinite time duration of the data and the key which is referring to
	 * {@code DataStore}
	 * 
	 * @param key
	 *            the key capped at a length of 32 characters referring the
	 *            data.
	 * @param data
	 *            the actual data in the json format with respect to the key
	 *            provided.
	 * @param path
	 *            the location or the place where the data needs to be stored.
	 *            
	 * @since 1.5
	 */
	public synchronized void store(String key, String data, String path) {
		try {
			create(key, data, path, 0);
		} catch (CustomException e) {
			System.out.println(e);

		}

	}

	/**
	 * Returns String data read for the key provided. If the key is valid, then
	 * the data is retrived.
	 * 
	 * @param key
	 *            the key capped at a length of 32 characters referring the
	 *            data.
	 * 
	 * @return String of the data retrived for the key
	 * 
	 * @since 1.2
	 * 
	 */
	public synchronized String read(String key) {

		try {
			return readData(key);
		} catch (CustomException e) {
			System.out.println(e.getMessage());
		}
		return null;

	}

	/**
	 * Deletes the data for the key provided, if the key provided is valid.
	 * 
	 * @param key
	 *            the key capped at a length of 32 characters referring the
	 *            data.
	 *            
	 *@since 1.2            
	 */

	public synchronized void delete(String key) {
		try {
			deleteData(key);
		} catch (CustomException e) {
			System.out.println(e.getMessage());
		}
	}

	protected synchronized void create(String key, String data, String path, long ttl) throws CustomException {
		try {

			if (key.length() <= 32) {

				if (DataStoreDao.checkData(data)) {
					if (data != null) {
						if (path == null)
							path = DataStoreDao.createDefaultFolder().toString();
						String value = null;
						if (cache.size() > 0) {
							value = (String) cache.get(key);
						}
						if (value == null) {
							boolean folderexists = DataStoreDao.CheckDirectoryExits(path);
							if (!folderexists) {
								folderexists = DataStoreDao.createDirectory(path);
								if (!folderexists) {
									throw new CustomException("Unable to create Directory Path Specified");
								}
							}
							double dirSize = DataStoreDao.folderSize(path);
							if (dirSize < 1) {
								boolean createFile = DataStoreDao.createFile(key, data, path);

								if (createFile) {
									cache.add(key, path, ttl);
								} else {
									throw new CustomException("Failed creating file");
								}
							} else {
								throw new CustomException("Folder size exceeding 1 GB");
							}
						} else {
							throw new CustomException("File with same key already exists");
						}
					} else {
						throw new CustomException("Please provide data to store");
					}
				} else {
					throw new CustomException("Data is not a Json Data");
				}
			} else {
				throw new CustomException("Key length exceedin 32 characters");
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());

		}
	}

	protected synchronized void deleteData(String key) throws CustomException {

		try {
			if (key != null) {

				String path = (String) cache.get(key);
				if (path != null) {

					boolean delete = DataStoreDao.delete(key, path);
					if (delete) {
						cache.remove(key);
					} else {
						throw new CustomException("Error occured during Deletion please Try Again");
					}
				} else {
					throw new CustomException("Please provide a valid Key to Delete");
				}

			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	protected synchronized String readData(String key) throws CustomException {
		String data = null;
		try {
			if (key != null) {

				String path = (String) cache.get(key);

				if (path != null)
					data = DataStoreDao.readFile(key, path);
				else {
					throw new CustomException("Key expired or Not available");
				}
			} else {
				throw new CustomException("Please provide a valid Key to Read");
			}
		} catch (Exception e) {
			return e.getMessage();
		}
		return data;

	}

}
