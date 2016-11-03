/*
 * The Alluxio Open Foundation licenses this work under the Apache License, version 2.0
 * (the "License"). You may not use this work except in compliance with the License, which is
 * available at www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied, as more fully set forth in the License.
 *
 * See the NOTICE file distributed with this work for information regarding copyright ownership.
 */

package alluxio;

import alluxio.underfs.UnderFileSystem;
import alluxio.underfs.UnderFileSystemFactory;
import alluxio.underfs.UnderFileSystemRegistry;
import alluxio.underfs.local.LocalUnderFileSystem;

import org.mockito.Mockito;

import java.io.Closeable;
import java.io.IOException;

/**
 * {@link SpyLocalUnderFileSystem} replaces {@link LocalUnderFileSystem} in the under file system
 * registry and acts identically, but with the ability to mock ufs methods via {@link #get}.
 *
 * <pre>
 * SpyLocalUnderFileSystem spyUfs = new SpyLocalUnderFileSystem();
 * doThrow(new RuntimeException()).when(spyUfs.get()).rename(anyString(), anyString());
 * </pre>
 *
 * Objects of this class must be closed after use so that they unregister themselves from the
 * global under file system registry.
 */
public final class UnderFileSystemSpy implements Closeable {
  private final UnderFileSystemFactory mFactory;
  private final UnderFileSystem mUfsSpy;

  /**
   * Creates a new {@link SpyLocalUnderFileSystem}.
   *
   * @param prefix the path prefix to intercept UFS calls on
   * @param ufs the under file system to spy
   */
  public UnderFileSystemSpy(final String prefix, UnderFileSystem ufs) {
    mUfsSpy = Mockito.spy(ufs);
    mFactory = new UnderFileSystemFactory() {
      @Override
      public UnderFileSystem create(String path, Object ufsConf) {
        return mUfsSpy;
      }

      @Override
      public boolean supportsPath(String path) {
        return path.startsWith(prefix);
      }
    };
    UnderFileSystemRegistry.register(mFactory);
    UnderFileSystem.clearCache();
  }

  public UnderFileSystem get() {
    return mUfsSpy;
  }

  @Override
  public void close() throws IOException {
    UnderFileSystemRegistry.unregister(mFactory);
    UnderFileSystem.clearCache();
  }
}
