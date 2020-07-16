package com.api.utility;

import java.lang.ref.SoftReference;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.api.dao.DataStoreDao;
import com.api.model.Cache;
import com.api.model.CacheObject;

public class InMemoryCache implements Cache {

	private static final int CLEAN_UP_PERIOD_IN_SEC = 5;

	private final ConcurrentHashMap<String, SoftReference<CacheObject>> cache = new ConcurrentHashMap<>();

	public InMemoryCache() {
		Thread cleanerThread = new Thread(() -> {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					Thread.sleep(CLEAN_UP_PERIOD_IN_SEC * 1000);
					Set<String> it = cache.keySet();
					for(String s : it){
						String valueDeadKey = (String) getDeadKey(s);
						DataStoreDao.delete(s, valueDeadKey);
					}
					cache.entrySet().removeIf(entry -> Optional.ofNullable(entry.getValue()).map(SoftReference::get)
							.map(CacheObject::isExpired).orElse(false));
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		});
		cleanerThread.setDaemon(true);
		cleanerThread.start();
	}

	@Override
	public void add(String key, String directory, long periodInMillis) {
		long expiryTime = 0;
		if (key == null) {
			return;
		} else if (directory == null) {
			return;
		} else {

			if (periodInMillis != 0)
				expiryTime = System.currentTimeMillis() + periodInMillis;

			cache.put(key, new SoftReference<>(new CacheObject(key, directory, expiryTime)));
		}
	}

	@Override
	public void remove(String key) {
		cache.remove(key);
	}

	@Override
	public Object get(String key) {
		return Optional.ofNullable(cache.get(key)).map(SoftReference::get)
				.filter(cacheObject -> !cacheObject.isExpired()).map(CacheObject::getValue).orElse(null);
	}

	@Override
	public Object getDeadKey(String key) {
		return Optional.ofNullable(cache.get(key)).map(SoftReference::get)
				.filter(cacheObject -> cacheObject.isExpired()).map(CacheObject::getValue).orElse(null);
	}

	@Override
	public void clear() {
		cache.clear();
	}

	@Override
	public long size() {
		return cache.entrySet().stream().filter(entry -> Optional.ofNullable(entry.getValue()).map(SoftReference::get)
				.map(cacheObject -> !cacheObject.isExpired()).orElse(false)).count();
	}


}