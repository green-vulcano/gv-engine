/**
 * 
 */
package it.greenvulcano.util.file.cache;

import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import it.greenvulcano.util.bin.BinaryUtils;

/**
 * @author gianluca
 *
 */
public class FileCache {
	private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FileCache.class);
	private static FileCache instance = null;
	private static Map<String, CacheEntry> cache = new HashMap<String, CacheEntry>();
	
	public enum Type {
		TEXT("text"), BASE64("base64"),
		BYTES("bytes");
		private String desc;

		private Type(String desc) {
			this.desc = desc;
		}

		@Override
		public String toString() {
			return this.desc;
		}
	}

	private static class CacheEntry {
		private long lastAccessed;
		private Map<Type, Object> content = new HashMap<Type, Object>();
		private String path;

		public CacheEntry(String path, Type type, Object content) {
			this.path = path;
			this.content.put(type, content);
			this.lastAccessed = System.currentTimeMillis();
		}

		public String getPath() {
			return path;
		}
		
		@Override
		public String toString() {
			return path + " - " + content.keySet();
		}

		public boolean isValid() {
			return System.currentTimeMillis() - this.lastAccessed < (5 * 60 * 1000);
		}
		
		public Object getContent(Type type) throws Exception {
			this.lastAccessed = System.currentTimeMillis();
			Object data = content.get(type);
			if (data == null) {
				data = this.content.get(Type.TEXT);
				if (data != null) {
					if (type == Type.BYTES) {
						data = ((String) data).getBytes();
						this.content.put(Type.BYTES, data);
					}
					else { // Type.BASE64
						data = Base64.getEncoder().encodeToString(((String) data).getBytes());
						this.content.put(Type.BASE64, data);
					}
				}
				else {
					data = this.content.get(Type.BYTES);
					if (data != null) {
						if (type == Type.TEXT) {
							data = new String((byte[]) data);
							this.content.put(Type.TEXT, data);
						}
						else { // Type.BASE64
							data = Base64.getEncoder().encodeToString((byte[]) data);
							this.content.put(Type.BASE64, data);
						}
					}
					else {
						data = this.content.get(Type.BASE64);
						if (data != null) {
							if (type == Type.TEXT) {
								data = new String(Base64.getDecoder().decode((String) data));
								this.content.put(Type.TEXT, data);
							}
							else { // Type.BYTES
								data = Base64.getDecoder().decode((String) data);
								this.content.put(Type.BYTES, data);
							}
						}
					}
				}
			}
			return data;
		}
	}

	/**
	 * 
	 */
	private FileCache() {
		// do nothing
	}
	
	public static synchronized FileCache instance() {
		if (instance == null) {
			instance = new FileCache();
		}
		return instance;
	}

	public static Object getContent(String path, Type type) throws Exception {
		return getContent(path, type, true);
	}

	public static Object getContent(String path, Type type, Boolean cached) throws Exception {
		if (!cached) {
			cache.remove(path);
			return readFile(path, type);
		}
		CacheEntry entry = cache.get(path);
		if (entry == null) {
			synchronized (cache) {
				entry = cache.get(path);
				if (entry == null) {
					entry = new CacheEntry(path, type, readFile(path, type));
					cache.put(entry.getPath(), entry);
				}
			}
		}
		Object data = entry.getContent(type);
		return data;
	}
	
	public static void remove(String path) {
		cache.remove(path);
	}
	
	public static void clear() {
		cache.clear();
	}
	
	public static String dump() {
		StringBuffer sb = new StringBuffer();
		cache.values().forEach(v -> {
			sb.append(v).append("\n");
		});
		return sb.toString();
	}

	private static Object readFile(String path, Type type) throws Exception {
		LOG.debug("Reading file [" + path + "]");
		byte[] data = BinaryUtils.readFileAsBytes(path);
		if (type == Type.BYTES) {
			return data;
		}
		if (type == Type.TEXT) {
			return new String(data);
		}
		// Type.BASE_64
		return Base64.getEncoder().encodeToString(data);
	}
	
	private static Timer cleaner = new Timer("FileCacheCleaner", true);
	static {
		cleaner.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					if (instance != null) {
						Iterator<Entry<String, CacheEntry>> it = cache.entrySet().iterator();
						while (it.hasNext()) {
							Entry<String, CacheEntry> e = it.next();
							if (!e.getValue().isValid()) {
								LOG.debug("Removing from cache: " + e.getValue());
								it.remove();
							}
						}
					}
				} catch (Exception exc) {
					LOG.error("Error in FileCacheCleaner", exc);
				}
			}
		}, 5*60*1000, 1*60*1000);
	}
}
