package com.jkoolcloud.tnt4j.streams.admin.backend;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ExecutionException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.jkoolcloud.tnt4j.streams.admin.backend.loginAuth.UsersUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ UsersUtils.class })
public class TestLoginCache {

	private static Logger LOG = LoggerFactory.getLogger(TestLoginCache.class);

	@Test
	public void whenCacheMiss_thenValueIsComputed() throws ExecutionException {
		CacheLoader<String, String> loader;
		loader = new CacheLoader<String, String>() {
			@Override
			public String load(String key) {
				return key.toUpperCase();
			}
		};

		LoadingCache<String, String> cache;
		cache = CacheBuilder.newBuilder().build(loader);

		LOG.info("The cache set after first initialization: {}", cache.asMap());
		assertEquals(0, cache.size());
		assertEquals("HELLO", cache.getUnchecked("hello"));
		assertEquals(1, cache.size());
		cache.get("data");
		LOG.info("The cache set after first initialization: {}", cache.asMap());
	}
}
