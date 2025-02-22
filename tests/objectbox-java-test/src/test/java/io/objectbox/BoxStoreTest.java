/*
 * Copyright 2017 ObjectBox Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.objectbox;

import io.objectbox.exception.DbException;
import org.junit.Test;

import java.io.File;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class BoxStoreTest extends AbstractObjectBoxTest {

    @Test
    public void testUnalignedMemoryAccess() {
        BoxStore.testUnalignedMemoryAccess();
    }

    @Test
    public void testClose() {
        assertFalse(store.isClosed());
        store.close();
        assertTrue(store.isClosed());

        // Double close should be fine
        store.close();
    }

    @Test
    public void testEmptyTransaction() {
        Transaction transaction = store.beginTx();
        transaction.commit();
    }

    @Test
    public void testSameBox() {
        Box<TestEntity> box1 = store.boxFor(TestEntity.class);
        Box<TestEntity> box2 = store.boxFor(TestEntity.class);
        assertSame(box1, box2);
    }

    @Test(expected = RuntimeException.class)
    public void testBoxForUnknownEntity() {
        store.boxFor(getClass());
    }

    @Test
    public void testRegistration() {
        assertEquals("TestEntity", store.getDbName(TestEntity.class));
        assertEquals(TestEntity.class, store.getEntityInfo(TestEntity.class).getEntityClass());
    }

    @Test
    public void testCloseThreadResources() {
        Box<TestEntity> box = store.boxFor(TestEntity.class);
        Cursor<TestEntity> reader = box.getReader();
        box.releaseReader(reader);

        Cursor<TestEntity> reader2 = box.getReader();
        box.releaseReader(reader2);
        assertSame(reader, reader2);

        store.closeThreadResources();
        Cursor<TestEntity> reader3 = box.getReader();
        box.releaseReader(reader3);
        assertNotSame(reader, reader3);
    }

    @Test(expected = DbException.class)
    public void testPreventTwoBoxStoresWithSameFileOpenend() {
        createBoxStore();
    }

    @Test
    public void testOpenSameBoxStoreAfterClose() {
        store.close();
        createBoxStore();
    }

    @Test
    public void testOpenTwoBoxStoreTwoFiles() {
        File boxStoreDir2 = new File(boxStoreDir.getAbsolutePath() + "-2");
        BoxStoreBuilder builder = new BoxStoreBuilder(createTestModel(null)).directory(boxStoreDir2);
        builder.entity(new TestEntity_());
    }

    @Test
    public void testDeleteAllFiles() {
        closeStoreForTest();
    }

    @Test
    public void testDeleteAllFiles_staticDir() {
        closeStoreForTest();
        File boxStoreDir2 = new File(boxStoreDir.getAbsolutePath() + "-2");
        BoxStoreBuilder builder = new BoxStoreBuilder(createTestModel(null)).directory(boxStoreDir2);
        BoxStore store2 = builder.build();
        store2.close();

        assertTrue(boxStoreDir2.exists());
        assertTrue(BoxStore.deleteAllFiles(boxStoreDir2));
        assertFalse(boxStoreDir2.exists());
    }

    @Test
    public void testDeleteAllFiles_baseDirName() {
        closeStoreForTest();
        File basedir = new File("test-base-dir");
        String name = "mydb";
        if (!basedir.exists()) {
            if (!basedir.mkdir()) {
                fail("Failed to create test directory.");
            }
        }
        assertTrue(basedir.isDirectory());
        File dbDir = new File(basedir, name);
        assertFalse(dbDir.exists());

        BoxStoreBuilder builder = new BoxStoreBuilder(createTestModel(null)).baseDirectory(basedir).name(name);
        BoxStore store2 = builder.build();
        store2.close();

        assertTrue(dbDir.exists());
        assertTrue(BoxStore.deleteAllFiles(basedir, name));
        assertFalse(dbDir.exists());
        assertTrue(basedir.delete());
    }

    @Test(expected = IllegalStateException.class)
    public void testDeleteAllFiles_openStore() {
        BoxStore.deleteAllFiles(boxStoreDir);
    }

    @Test
    public void removeAllObjects() {
        // Insert at least two different kinds.
        store.close();
        store.deleteAllFiles();
        store = createBoxStoreBuilderWithTwoEntities(false).build();
        putTestEntities(5);
        Box<TestEntityMinimal> minimalBox = store.boxFor(TestEntityMinimal.class);
        minimalBox.put(new TestEntityMinimal(0, "Sally"));
        assertEquals(5, getTestEntityBox().count());
        assertEquals(1, minimalBox.count());

        store.removeAllObjects();
        assertEquals(0, getTestEntityBox().count());
        assertEquals(0, minimalBox.count());

        // Assert inserting is still possible.
        putTestEntities(1);
        assertEquals(1, getTestEntityBox().count());
    }

    private void closeStoreForTest() {
        assertTrue(boxStoreDir.exists());
        store.close();
        assertTrue(store.deleteAllFiles());
        assertFalse(boxStoreDir.exists());
    }

    @Test
    public void testCallInReadTxWithRetry() {
        final int[] countHolder = {0};
        String value = store.callInReadTxWithRetry(createTestCallable(countHolder), 5, 0, true);
        assertEquals("42", value);
        assertEquals(5, countHolder[0]);
    }

    @Test(expected = DbException.class)
    public void testCallInReadTxWithRetry_fail() {
        final int[] countHolder = {0};
        store.callInReadTxWithRetry(createTestCallable(countHolder), 4, 0, true);
    }

    @Test
    public void testCallInReadTxWithRetry_callback() {
        closeStoreForTest();
        final int[] countHolder = {0};
        final int[] countHolderCallback = {0};

        BoxStoreBuilder builder = new BoxStoreBuilder(createTestModel(null)).directory(boxStoreDir)
                .failedReadTxAttemptCallback((result, error) -> {
                    assertNotNull(error);
                    countHolderCallback[0]++;
                });
        store = builder.build();
        String value = store.callInReadTxWithRetry(createTestCallable(countHolder), 5, 0, true);
        assertEquals("42", value);
        assertEquals(5, countHolder[0]);
        assertEquals(4, countHolderCallback[0]);
    }

    private Callable<String> createTestCallable(final int[] countHolder) {
        return () -> {
            int count = ++countHolder[0];
            if (count < 5) {
                throw new DbException("This exception IS expected. Count: " + count);
            }
            return "42";
        };
    }

    @Test
    public void testSizeOnDisk() {
        long size = store.sizeOnDisk();
        assertTrue(size >= 8192);
    }

    @Test
    public void validate() {
        putTestEntities(100);

        // No limit.
        long validated = store.validate(0, true);
        assertEquals(9, validated);

        // With limit.
        validated = store.validate(1, true);
        // 2 because the first page doesn't contain any actual data?
        assertEquals(2, validated);
    }

    @Test
    public void testIsObjectBrowserAvailable() {
        assertFalse(BoxStore.isObjectBrowserAvailable());
    }

    @Test
    public void testSysProc() {
        long vmRss = BoxStore.sysProcStatusKb("VmRSS");
        long memAvailable = BoxStore.sysProcMeminfoKb("MemAvailable");

        final String osName = System.getProperty("os.name");
        if (osName.toLowerCase().contains("linux")) {
            System.out.println("VmRSS: " + vmRss);
            System.out.println("MemAvailable: " + memAvailable);
            assertTrue(vmRss > 0);
            assertTrue(memAvailable > 0);
        } else {
            assertEquals(0, vmRss);
            assertEquals(0, memAvailable);
        }
    }

}