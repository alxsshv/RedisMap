import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.redis.RedisMap;
import org.testcontainers.utility.DockerImageName;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.*;

/**
 * Tests for RedisMap
 * @author Alexei Shvariov
 */
public class RedisMapTest {
    private static final RedisContainer REDIS = new RedisContainer(DockerImageName.parse("redis:6.2.6"));
    private static JedisPool jedisPool;

    private final JedisPool mockJedisPool = Mockito.mock(JedisPool.class);

    @BeforeAll
    public static void setUp() {
        REDIS.start();
        if (REDIS.isRunning()) {
            jedisPool = new JedisPool(REDIS.getHost(), REDIS.getFirstMappedPort());
        }


    }

    @BeforeEach
    public void fillDatabase() {
        Jedis jedis = jedisPool.getResource();
        jedis.set("1", "one");
        jedis.set("2", "two");
        jedis.set("3", "three");
        jedis.close();
    }

    @AfterEach
    public void clearDatabase() {
        Jedis jedis = jedisPool.getResource();
        jedis.flushDB();
        jedis.close();
    }


    @AfterAll
    public static void closeResources() {
        jedisPool.close();
        REDIS.stop();
    }



    @Test
    @DisplayName("Test constructor with port and host params when set valid connection params then return new RedisMap")
    public void testConstructor_whenSetValidConnectionParam_thenReturnNewRedisMap() {
        try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
            Assertions.assertNotNull(redisMap);
        }
    }

    @Test
    @DisplayName("Test constructor with port and host params when host is null then throw nullPointerException")
    public void testConstructor_whenHostIsNull_thenThrowNullPointerException() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            try (RedisMap redisMap = new RedisMap(null, REDIS.getFirstMappedPort())) {
                Assertions.assertNull(redisMap);
            }
        } );
    }

    @Test
    @DisplayName("Test constructor with port and host params when port = 0 then throw nullPointerException")
    public void testConstructor_whenPortEquals0_thenThrowIllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            try (RedisMap redisMap = new RedisMap(REDIS.getHost(), 0)) {
                Assertions.assertNull(redisMap);
            }
        } );
    }

    @Test
    @DisplayName("Test constructor with port and host params when set not valid host then throw nullPointerException")
    public void testConstructor_whenSetNotValidHost_thenThrowIllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            try (RedisMap redisMap = new RedisMap("notValidHost", REDIS.getFirstMappedPort())) {
                Assertions.assertNull(redisMap);
            }
        } );
    }

    @Test
    @DisplayName("Test constructor with port and host params when set wrong port then throw nullPointerException")
    public void testConstructor_whenSetWrongPort_thenThrowIllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort() + 9999)) {
                Assertions.assertNull(redisMap);
            }
        } );
    }

    @Test
    @DisplayName("Test size when method called then return valid value")
    public void testSize_whenMethodCalled_thenReturnValidValue() {
        final int expectedSize = 3;
        try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
            Assertions.assertEquals(expectedSize, redisMap.size());
        }
    }

    @Test
    @DisplayName("Test getSize when method called then return valid value")
    public void testGetSize_whenMethodCalled_thenReturnValidValue() {
        final int expectedSize = 3;
        try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
            Assertions.assertEquals(expectedSize, redisMap.getSize());
        }
    }

    @Test
    @DisplayName("Test isEmpty when Redis is not empty then return false")
    public void testIsEmpty_whenRedisNotEmpty_thenReturnFalse() {
        try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
            Assertions.assertFalse(redisMap.isEmpty());
        }
    }

    @Test
    @DisplayName("Test isEmpty when Redis is empty then return true")
    public void testIsEmpty_whenRedisIsEmpty_thenReturnTrue() {
        Jedis jedis = jedisPool.getResource();
        jedis.flushDB();
        jedis.close();
        try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
            Assertions.assertTrue(redisMap.isEmpty());
        }
    }

    @Test
    @DisplayName("Test containsKey when key is contains in Redis then return true")
    public void testContainsKey_whenKeyIsContainsInRedis_thenReturnTrue() {
        String keyFromRedis = "2";
        try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
            Assertions.assertTrue(redisMap.containsKey(keyFromRedis));
        }
    }

    @Test
    @DisplayName("Test containsKey when key is not contains in Redis then return false")
    public void testContainsKey_whenKeyIsNotContainsInRedis_thenReturnFalse() {
        try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
            Assertions.assertFalse(redisMap.containsKey("unknown key"));
        }
    }

    @Test
    @DisplayName("Test containsKey when key is not string type then throw ClassCastException")
    public void testContainsKey_whenKeyIsNotStringType_thenThrowClassCastException() {
        Assertions.assertThrows(ClassCastException.class, () -> {
            try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
                redisMap.containsKey(55L);
            }
        } );
    }

    @Test
    @DisplayName("Test containsKey when key is null then throw NullPointerException")
    public void testContainsKey_whenKeyIsNull_thenThrowNullPointerException() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
                redisMap.containsKey(null);
            }
        } );
    }

    @Test
    @DisplayName("Test containsValue when value contains in Redis then return true")
    public void testContainsValue_whenValueContainsInRedis_thenReturnTrue() {
        String valueFromRedis = "two";
        try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
            Assertions.assertTrue(redisMap.containsValue(valueFromRedis));
        }
    }

    @Test
    @DisplayName("Test containsValue when value not contains in Redis then return false")
    public void testContainsValue_whenValueNotContainsInRedis_thenReturnFalse() {
        try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
            Assertions.assertFalse(redisMap.containsValue("unknown value"));
        }
    }

    @Test
    @DisplayName("Test containsValue when value is not string type in Redis then throw ClassCastException")
    public void testContainsValue_whenValueIsNotStringType_thenThrowClassCastException() {
        Assertions.assertThrows(ClassCastException.class, () -> {
            try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
                redisMap.containsValue(55L);
            }
        } );
    }

    @Test
    @DisplayName("Test containsValue when value is null then throw NullPointerException")
    public void testContainsValue_whenValueIsNull_thenThrowNullPointerException() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
                redisMap.containsValue(null);
            }
        } );
    }

    @Test
    @DisplayName("Test get when key is valid then return valid value")
    public void testGet_whenKeyIsValid_thenReturnValidValue() {
        String expectedValue = "three";
        try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
            Assertions.assertEquals(expectedValue, redisMap.get("3"));
        }
    }

    @Test
    @DisplayName("Test get when key is not valid then return null")
    public void testGet_whenKeyIsNotValid_thenReturnNull() {
        try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
            Assertions.assertNull(redisMap.get("unknown key"));
        }
    }

    @Test
    @DisplayName("Test get when key is null then throw NullPointerException")
    public void testGet_whenKeyIsNull_thenThrowNullPointerException() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
                redisMap.get(null);
            }
        } );
    }

    @Test
    @DisplayName("Test get when key is not string then throw ClassCastException")
    public void testGet_whenKeyIsNotString_thenThrowClassCastException() {
        Assertions.assertThrows(ClassCastException.class, () -> {
            try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
                redisMap.get(777L);
            }
        } );
    }

    @Test
    @DisplayName("Test put when put new key and valid value then return null")
    public void testPut_whenPutNewKeyAndValidValue_thenReturnNull() {
        final int expectedSize = 4;
        try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
            Assertions.assertNull(redisMap.put("5", "five"));
            Assertions.assertEquals(expectedSize, redisMap.getSize());
        }
    }

    @Test
    @DisplayName("Test put when put available key and new value then return previous value")
    public void testPut_whenPutAvailableKeyAndNewValue_thenReturnPreviousValue() {
        final int expectedSize = 3;
        final String previousValue = "two";
        try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
            Assertions.assertEquals(previousValue,redisMap.put("2", "два"));
            Assertions.assertEquals(expectedSize, redisMap.getSize());
        }
    }

    @Test
    @DisplayName("Test put when put when key is null then throw NullPointerException")
    public void testPut_whenKeyIsNull_thenThrowNullPointerException() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
                redisMap.put(null, "value");
            }
        } );
    }

    @Test
    @DisplayName("Test put when put when value is null then throw NullPointerException")
    public void testPut_whenValueIsNull_thenThrowNullPointerException() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
                redisMap.put("key", null);
            }
        } );
    }

    @Test
    @DisplayName("Test remove when Redis contains key then mapping success delete")
    public void testRemove_whenRedisContainsKey_thenMappingSuccessDelete() {
        final int expectedSize = 2;
        final String expectedPreviousValue = "two";
        try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
            Assertions.assertEquals(expectedPreviousValue,redisMap.remove("2"));
            Assertions.assertEquals(expectedSize, redisMap.getSize());
        }
    }

    @Test
    @DisplayName("Test remove when key missing in Redis then nothing will happen")
    public void testRemove_whenKeyMissingInRedis_thenNothingWillHappen() {
        final int expectedSize = 3;
        try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
            Assertions.assertNull(redisMap.remove("unknown key"));
            Assertions.assertEquals(expectedSize, redisMap.getSize());
        }
    }

    @Test
    @DisplayName("Test remove when key is null then throw NullPointerException")
    public void testRemove_whenKeyIsNull_thenThrowNullPointerException() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
                redisMap.remove(null);
            }
        } );
    }

    @Test
    @DisplayName("Test remove when key is not string then throw ClassCastException")
    public void testRemove_whenKeyIsNotString_thenThrowClassCastException() {
        Assertions.assertThrows(ClassCastException.class, () -> {
            try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
                redisMap.remove(23L);
            }
        } );
    }

    @Test
    @DisplayName("Test putAll when collection being added is valid then collection is successfully added")
    public void testPutAll_whenCollectionBeingAddedIsValid_thenCollectionIsSuccessfullyAdded() {
        Map<String, String> numbers = new HashMap<>();
        numbers.put("4", "four");
        numbers.put("5", "five");
        numbers.put("6", "six");
        final int expectedSize = 6;
        try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
            redisMap.putAll(numbers);
            Assertions.assertEquals(expectedSize, redisMap.getSize());
        }
    }

    @Test
    @DisplayName("Test putAll when collection is null then throw NullPointerException")
    public void testPutAll_whenCollectionIsNull_thenThrowNullPointerException() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
                redisMap.putAll(null);
            }
        } );
    }

    @Test
    @DisplayName("Test putAll when collection's key is null then throw NullPointerException")
    public void testPutAll_whenCollectionKeyIsNull_thenThrowNullPointerException() {
        Map<String, String> numbers = new HashMap<>();
        numbers.put("4", "four");
        numbers.put(null, "five");
        numbers.put("6", "six");
        Assertions.assertThrows(NullPointerException.class, () -> {
            try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
                redisMap.putAll(numbers);
            }
        } );
    }

    @Test
    @DisplayName("Test putAll when collection's value is null then throw NullPointerException")
    public void testPutAll_whenCollectionValueIsNull_thenThrowNullPointerException() {
        Map<String, String> numbers = new HashMap<>();
        numbers.put("4", "four");
        numbers.put("5", null);
        numbers.put("6", "six");
        Assertions.assertThrows(NullPointerException.class, () -> {
            try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
                redisMap.putAll(numbers);
            }
        } );
    }

    @Test
    @DisplayName("Test clear when invoke method then Redis successfully cleared")
    public void testClear_whenInvokeMethod_thenRedisSuccessfullyCleared() {
        try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
            Assertions.assertTrue(redisMap.getSize() > 0);
            redisMap.clear();
            Assertions.assertEquals(0, redisMap.getSize());
        }
    }

    @Test
    @DisplayName("Test keySet when invoke method then return set of Redis keys")
    public void testKeySet_whenInvokeMethod_thenReturnSetOfRedisKeys() {
        final int expectedSize = 3;
        final String keyFromRedis = "2";
        try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
            Set<String> keySet = redisMap.keySet();
            Assertions.assertEquals(expectedSize, keySet.size());
            Assertions.assertTrue(keySet.contains(keyFromRedis));
        }
    }

    @Test
    @DisplayName("Test values when invoke method then return collection of Redis values")
    public void testValues_whenInvokeMethod_thenReturnCollectionOfValues() {
        String valueFromRedis = "two";
        try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
            Collection<String> values = redisMap.values();
            Assertions.assertFalse(values.isEmpty());
            Assertions.assertTrue(values.contains(valueFromRedis));
        }
    }

    @Test
    @DisplayName("Test entrySet when invoke method then return set of entry")
    public void testEntrySet_whenInvokeMethod_thenReturnSetOfEntry() {
        final int expectedSize = 3;
        try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
            Set<Map.Entry<String, String>> entries = redisMap.entrySet();
            Assertions.assertFalse(entries.isEmpty());
            Assertions.assertEquals(expectedSize, entries.size());
        }
    }

    @Test
    @DisplayName("Test entry's method getKey when invoke method then return entry's key")
    public void testEntryGetKey_whenRedisContainKey_thenReturnEntryKet() {
        try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
            String key = redisMap.entrySet().stream().findAny().orElseThrow().getKey();
            Assertions.assertNotNull(key);
            Assertions.assertTrue(redisMap.containsKey(key));
        }
    }

    @Test
    @DisplayName("Test entry's method getKey when key not found in Redis then throw IllegalStateException")
    public void testEntryGetKey_whenKeyNotFoundInRedis_thenThrowIllegalStateException() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
                redisMap.remove("1");
                redisMap.remove("2");
                Map.Entry<String, String> entry = redisMap.entrySet().stream().findAny().orElseThrow();
                redisMap.remove("3");
                entry.getKey();
            }
        });
    }

    @Test
    @DisplayName("Test entry's method getValue when Redis contain value then return entry's value")
    public void testEntryGetValue_whenRedisContainValue_thenReturnEntryValue() {
        try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
            String value = redisMap.entrySet().stream().findAny().orElseThrow().getValue();
            Assertions.assertNotNull(value);
            Assertions.assertTrue(redisMap.containsValue(value));
        }
    }

    @Test
    @DisplayName("Test entry's method getValue when value not found in redis then throw IllegalStateException")
    public void testEntryGetValue_whenValueNotFoundInRedis_thenThrowIllegalStateException() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
                redisMap.remove("1");
                redisMap.remove("2");
                Map.Entry<String, String> entry = redisMap.entrySet().stream().findAny().orElseThrow();
                redisMap.remove("3");
                entry.getValue();
            }
        });
    }


    @Test
    @DisplayName("Test entry's method getValue when value has been changed in Redis then throw IllegalStateException")
    public void testEntryGetValue_whenValueHasBeenChangedInRedis_thenThrowIllegalStateException() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
                Map.Entry<String, String> entry = redisMap.entrySet().stream().findAny().orElseThrow();
                redisMap.put("1", "один");
                redisMap.put("2", "два");
                redisMap.put("3", "три");
                entry.getValue();
            }
        });
    }

    @Test
    @DisplayName("Test entry's method setValue when Redis contain entry then return previous value")
    public void testEntrySetValue_whenRedisContainEntry_thenReturnEntryValue() {
        final String newValue = "newValue";
        try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
            Map.Entry<String, String> entry = redisMap.entrySet().stream().findAny().orElseThrow();
            Assertions.assertFalse(redisMap.containsValue(newValue));
            Assertions.assertNotNull(entry.setValue(newValue));
            Assertions.assertTrue(redisMap.containsValue(newValue));
        }
    }

    @Test
    @DisplayName("Test entry's method setValue when value is null then throw NullPointerException")
    public void testEntrySetValue_whenValueIsNull_thenThrowNullPointerException() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
                Map.Entry<String, String> entry = redisMap.entrySet().stream().findAny().orElseThrow();
                entry.setValue(null);
            }
        });
    }

    @Test
    @DisplayName("Test entry's method setValue when value has been changed in Redis then throw IllegalStateException")
    public void testEntrySetValue_whenValueHasBeenChangedInRedis_thenThrowIllegalStateException() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            try (RedisMap redisMap = new RedisMap(REDIS.getHost(), REDIS.getFirstMappedPort())) {
                Map.Entry<String, String> entry = redisMap.entrySet().stream().findAny().orElseThrow();
                redisMap.put("1", "один");
                redisMap.put("2", "два");
                redisMap.put("3", "три");
                entry.setValue("New value");
            }
        });
    }

    @Test
    @DisplayName("Test close when invoke method then JedisPool is closed")
    public void testClose_whenInvokeMethod_thenJedisPoolClosed() {
        when(mockJedisPool.getResource())
                .thenReturn(jedisPool.getResource());
        try(RedisMap redisMap = new RedisMap(mockJedisPool)) {
            redisMap.getSize();
        }
        verify(mockJedisPool,times(1)).close();
    }

}
