package org.redis;

import lombok.NonNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.*;


/**
 * Map implementation for Redis database.
 * This implementation not  provides  optional matching operations and does not allow the use of null values or null keys.
 * This class does not provide any guarantees that the data will be up-to-date
 * when the Redis database is changed from multiple sources or multiple RedisMap instances.
 *
 * @Author Alexei Shvariov
 */
public class RedisMap implements Map<String, String>, AutoCloseable {
    private final JedisPool jedisPool;
    private final Jedis jedis;

    /**
     * Constructs an  RedisMap with the specified connection parameters.
     * @param host - Redis database connection host.
     * @param port - Redis database connection port.
     * @throws NullPointerException - if host is null.
     * @throws IllegalArgumentException – if host empty or port equals 0;
     * */
    public RedisMap(String host, int port) {
        if (host == null) {
            throw new NullPointerException("Host cannot be null");
        }
        if (host.isEmpty()) {
            throw new IllegalArgumentException("Host is empty. Please set redis database connection host.");
        }
        if (port == 0) {
            throw new IllegalArgumentException("The port is not specified. Please set redis database connection port.");
        }
        try {
            this.jedisPool = new JedisPool(host, port);
            this.jedis = jedisPool.getResource();
        } catch (JedisConnectionException ex) {
            throw  new IllegalArgumentException("Invalid host or port value. Connection error: " + ex.getMessage());
        }
    }

    /**
    * Constructs an  RedisMap with the specified connection parameters.
    * @param jedisPool - pool of connections for Redis database.
    * @throws NullPointerException - if host is null.
    */
    public RedisMap(JedisPool jedisPool) {
        if (jedisPool == null) {
            throw new NullPointerException("Jedis pool cannot be null");
        }
        try {
            this.jedisPool = jedisPool;
            this.jedis = jedisPool.getResource();
        } catch (JedisConnectionException ex) {
            throw  new IllegalArgumentException("Invalid host or port value. Connection error: " + ex.getMessage());
        }
    }

    /**
    * Returns the number of key mappings in redis database.
    * If the map contains more than Integer.MAX_VALUE elements, returns Integer.MAX_VALUE.
    * For getting size in long format use {@link  RedisMap#getSize()}
    * @return the number of key mappings in redis database
    */
    @Override
    public int size() {
        final long size = getSize();
        if (size > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) size;
    }

    /**
    *  Returns the number of key mappings in redis database.
    *  @return the number of key mappings in redis database
    */
    public long getSize() {
        return jedis.dbSize();
    }

    /**
     * Returns true if this map contains no key-value mappings.
     * @return true if this map contains no key-value mappings
     * */
    @Override
    public boolean isEmpty() {
        return getSize() == 0L;
    }

    /**
    * Returns true if this map contains a mapping for the specified key.
    * More formally, returns true if and only if this map contains a mapping for a key k such that Objects.equals(key, k).
    * (There can be at most one such mapping.)
    * @param key – key whose presence in this map is to be tested
    * @return true if this map contains a mapping for the specified key
    * @throws ClassCastException – if the key's type not String.
    * @throws NullPointerException – if the specified key is null.
    */
    @Override
    public boolean containsKey(Object key) {
        if (key == null) {
            throw new NullPointerException("Key cannot be null");
        }
        if (!(key instanceof String)) {
            throw new ClassCastException("Key type must be String");
        }
        return jedis.exists((String) key);
    }

    /**
     * Returns true if Redis maps one or more keys to the specified value.
     * More formally, returns true if and only if Redis contains at least one mapping to a value v such that Objects.equals(value, v).
     * @param value – value whose presence in this RedisMap is to be tested
     * @return true if this RedisMap maps one or more keys to the specified value
     * @throws ClassCastException – if the value is a not String type.
     * @throws NullPointerException – if the specified value is null.
     * */
    @Override
    public boolean containsValue(Object value) {
        if (value == null) {
            throw new NullPointerException("Key cannot be null");
        }
        if (!(value instanceof String)) {
            throw new ClassCastException("Key type must be String");
        }
        for (String key : keySet()) {
            String keyValue = jedis.get(key);
            if (keyValue.equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
    * Returns the value to which the specified key is mapped, or null if this map contains no mapping for the key.
    * @param key – the key whose associated value is to be returned
    * @return the value to which the specified key is mapped, or null if this map contains no mapping for the key
    * @throws ClassCastException – if the key's type is not a string.
    * @throws NullPointerException – if the specified key is null.
    */
    @Override
    public String get(Object key) {
        if (key == null) {
            throw new NullPointerException("Key cannot be null");
        }
        if (!(key instanceof String)) {
            throw new ClassCastException("Key type must be String");
        }
        return jedis.get((String) key);
    }

    /**
     * Associates the specified value with the specified key in this map
     * If the map previously contained a mapping for the key, the old value is replaced by the specified value.
     * (A map m is said to contain a mapping for a key k if and only if m.containsKey(k) would return true.)
     * @param key – key with which the specified value is to be associated
     * @param value – value to be associated with the specified key
     * @return the previous value associated with key, or null if there was no mapping for key.
     * @throws NullPointerException – if the specified key or value is null and this map does not permit null keys or values
     * */
    @Override
    public String put(String key, String value) {
        if (key == null || value == null) {
            throw new NullPointerException("Key or value cannot be null");
        }
        String previousValue  = get(key);
        jedis.set(key, value);
        return previousValue;
    }

    /**
     * Removes the mapping for a key from this map if it is present.
     * <p>Returns the value to which this map previously associated the key,
     * or {@code null} if the map contained no mapping for the key.
     *
     * <p>The map will not contain a mapping for the specified key once the
     * call returns.
     *
     * @param key - key whose mapping is to be removed from the map
     * @return the previous value associated with {@code key}, or
     *         {@code null} if there was no mapping for {@code key}.
     * @throws ClassCastException if the key is of an inappropriate type for
     *         this map ({@linkplain Collection##optional-restrictions optional})
     * @throws NullPointerException if the specified key is null and this
     *         map does not permit null keys ({@linkplain Collection##optional-restrictions optional})
     */
    @Override
    public String remove(Object key) {
        if (key == null) {
            throw new NullPointerException("Key value cannot be null");
        }
        if (!(key instanceof String)) {
            throw new ClassCastException("Key value must be string");
        }
        String deletedValue = get(key);
        jedis.del((String) key);
        return deletedValue;
    }

    /**
     * Copies all the mappings from the specified map to redis.
     * The effect of this call is equivalent to that of calling put(k, v) on this map once for each mapping from key k to value v in redis.
     * The behavior of this operation is undefined if the specified map is modified while the operation is in progress.
     * If the specified map has a defined encounter order, processing of its mappings generally occurs in that order.
     * @param m – mappings to be stored in this map.
     * @throws NullPointerException – if the specified map is null, or if this map does not permit null keys or values,
     * and the specified map contains null keys or values.
     * */
    @Override
    public void putAll(@NonNull Map<? extends String, ? extends String> m) {
        if (m.containsKey(null) || m.containsValue(null)) {
            throw new NullPointerException("Key value cannot be null");
        }
        for (String k : m.keySet()) {
            jedis.set(k, m.get(k));
        }
    }

    /**
     * Removes all the mappings from this map.
     * The map will be empty after this call returns.
     */
    @Override
    public void clear() {
        jedis.flushDB();
    }

    /**
     * KeySet - is a snapshot of a collection of Redis keys.
     * Changes to the RedisMap after calling KeySet() are not reflected in the set.
     * Operations on a KeySet do not change the RedisMap.
     * @return a set view of the keys contained in Redis.
     */
    @Override
    @NonNull
    public Set<String> keySet() {
        return jedis.keys("*");
    }

    /**
     * Values - is a collection of the Redis mappings values.
     * Changes to the RedisMap after calling values() are not reflected in the collection.
     * Operations on values do not modify the values on Redis.
     * @return  - collection of the Redis mappings values.
     * */
    @Override
    @NonNull
    public Collection<String> values() {
        List<String> values = new ArrayList<>();
        for (String key : keySet()) {
            values.add(get(key));
        }
        return values;
    }

    /**
     * Returns a Set view of the mappings contained in Redis.
     * The set is not fully supported by the RedisMap, but when the {@link Entry} method {@link Entry#setValue(String)} is called,
     * the value for the entry key in the Entry and in the RedisMap is set.
     * If set elements no changes to the Set, but changes to the map,
     * when invokes {@link Entry} methods: {@link Entry#getKey()}, {@link Entry#getValue()}, {@link Entry#setValue(String)}
     * then throws {@link IllegalStateException}.
     * If the map is modified while an iteration over the set is in progress the results of the iteration are undefined.
     * It does not support the add or addAll operations.
     * @return a set view of the mappings contained in Redis
     * */
    @Override
    @NonNull
    public Set<Map.Entry<String, String>> entrySet() {
        Set<Map.Entry<String, String>> entrySet = new HashSet<>();
        for (String key : keySet()) {
            Map.Entry<String, String> entry = new Entry(key, RedisMap.this.get(key));
            entrySet.add(entry);
        }
        return entrySet;
    }

    /**
     * A RedisMap entry (key-value pair).
     * <p>
     * Entry is not fully bound to RedisMap. When {@link Entry#setValue(String)} method is called,
     * the value for the entry key is set in Entry and in RedisMap.
     * If the RedisMap is modified and the key or value of the Entry is not valid for Redis,
     * methods {@link Entry#getKey()}, {@link Entry#getValue()}, {@link Entry#setValue(String)} throw {@link IllegalStateException}.
     * It is also undefined if the backing map has been modified after the Entry was
     * returned by the iterator, except through the {@link Entry#setValue(String)} method.
     * <p>
     * An Entry may also be obtained from a RedisMap's entry-set view by other means, for
     * example, using the
     * {@link Set#parallelStream parallelStream},
     * {@link Set#stream stream},
     * {@link Set#spliterator spliterator} methods,
     * any of the
     * {@link Set#toArray toArray} overloads,
     * or by copying the entry-set view into another collection.
     *
     * @see RedisMap#entrySet()
     */
    final class Entry implements Map.Entry<String, String> {
        private final String key;
        private String value;

        public Entry(String key, String value) {
            this.key = key;
            this.value = value;
        }

        /**
         * Returns the key corresponding to this entry.
         * @return the key corresponding to this entry
         * @throws IllegalStateException if the entry has been removed from the RedisMap.
         * */
        @Override
        public String getKey() {
            if (!RedisMap.this.containsKey(key)) {
                throw new IllegalStateException("Entry key not found in Redis");
            }
            return key;
        }

        /**
         * Returns the value corresponding to this entry.
         * If the mapping has been deleted from Redis, an IllegalStateException will be thrown.
         * If the mapping value has been change in Redis, an IllegalStateException will be thrown.
         * @return string value corresponding to this entry.
         * @throws IllegalStateException if entry value not equals value in Redis for entry's key.
         * */
        @Override
        public String getValue() {
            String redisValue = RedisMap.this.get(key);
            if (redisValue == null || !redisValue.equals(value)) {
                throw new IllegalStateException("Entry value not actual value in Redis for key = " + key);
            }
            return value;
        }

        /**
         * Replaces the value corresponding to this entry and the mapping's value Redis with the specified value.
         * If the mapping has already been removed from Redis, the entry's key will be added with the new value and return null.
         * @param value – new value to be stored in this entry and RedisMap.
         * @return old value corresponding to the entry
        * @throws NullPointerException – if new value is null.
        * @throws IllegalStateException if entry value not equals value in Redis for entry's key.
        */
        @Override
        public String setValue(String value) {
            if (value == null) {
                throw new NullPointerException("Entry value cannot be null");
            }
            String previousValue = getValue();
            RedisMap.this.put(key, value);
            this.value = value;
            return previousValue;
        }
    }

    @Override
    public void close() {
        jedis.close();
        jedisPool.close();
    }
}
