package net.ivpn.core;

public class SecureStorageTest {

//    private static final String FIELD1 = "FIELD1";
//    private static final String FIELD2 = "FIELD2";
//
//    private static final String FIELD1_VALUE = "FIELD1_VALUE";
//    private static final String FIELD2_VALUE = "FIELD2_VALUE";
//
//    private SecureStorage store;
//    private Context context;
//
//    @Before
//    public void setup() {
//        context = InstrumentationRegistry.getTargetContext();
//        store = SecureStorage.INSTANCE;
//    }
//
//    @Test()
//    public void fieldNotExistIfStoreEmpty() throws Exception {
//        store.init(context);
//
//        boolean results = store.isFieldsExist(FIELD1);
//
//        assertFalse(results);
//    }
//
//    @Test(timeout = 5000)
//    public void putAndCheckForExistingTwoDifferentFields() throws InterruptedException {
//        store.init(context);
//        store.putString(FIELD1, FIELD1_VALUE);
//
//        boolean result = store.isFieldsExist(FIELD1, FIELD2);
//
//        assertFalse(result);
//    }
//
//    @Test(timeout = 5000)
//    public void putAndGetTwoDifferentFields() throws InterruptedException {
//        store.init(context);
//        store.putString(FIELD1, FIELD1_VALUE);
//        store.putString(FIELD2, FIELD2_VALUE);
//
//        boolean result = store.isFieldsExist(FIELD1, FIELD2);
//
//        assertTrue(result);
//
//        String value1 = store.getString(FIELD1);
//        String value2 = store.getString(FIELD2);
//
//        assertNotNull(value1);
//        assertNotNull(value2);
//
//        assertEquals(FIELD1_VALUE, value1);
//        assertEquals(FIELD2_VALUE, value2);
//    }
//
//    @After
//    public void release() {
//        store.clear();
//    }
}
