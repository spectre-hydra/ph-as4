/**
 * Copyright (C) 2015-2017 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helger.as4.util;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.concurrent.SimpleReadWriteLock;
import com.helger.commons.io.file.FileIOError;
import com.helger.commons.io.stream.StreamHelper;

public class AS4ResourceManager implements Closeable
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (AS4ResourceManager.class);

  private final SimpleReadWriteLock m_aRWLock = new SimpleReadWriteLock ();
  private final AtomicBoolean m_aInClose = new AtomicBoolean (false);
  private final ICommonsList <File> m_aTempFiles = new CommonsArrayList<> ();
  private final ICommonsList <Closeable> m_aCloseables = new CommonsArrayList<> ();

  public AS4ResourceManager ()
  {}

  @Nonnull
  public File createTempFile () throws IOException
  {
    if (m_aInClose.get ())
      throw new IllegalStateException ("ResourceManager is already closing/closed!");

    // Create
    final File ret = File.createTempFile ("as4-res-", ".tmp");
    // And remember
    m_aRWLock.writeLocked ( () -> m_aTempFiles.add (ret));
    return ret;
  }

  public void addCloseable (@Nonnull final Closeable aCloseable)
  {
    ValueEnforcer.notNull (aCloseable, "Closeable");

    if (m_aInClose.get ())
      throw new IllegalStateException ("ResourceManager is already closing/closed!");

    m_aCloseables.add (aCloseable);
  }

  public void close ()
  {
    m_aInClose.set (true);

    // Close all closeables before deleting files, because the closables might
    // be the files to be deleted :)
    final ICommonsList <Closeable> aCloseables = m_aRWLock.writeLocked ( () -> {
      final ICommonsList <Closeable> ret = m_aCloseables.getClone ();
      m_aCloseables.clear ();
      return ret;
    });
    if (aCloseables.isNotEmpty ())
    {
      s_aLogger.info ("Closing " + aCloseables.size () + " stream handles");
      for (final Closeable aCloseable : aCloseables)
        StreamHelper.close (aCloseable);
    }

    // Get and delete all temp files
    final ICommonsList <File> aFiles = m_aRWLock.writeLocked ( () -> {
      final ICommonsList <File> ret = m_aTempFiles.getClone ();
      m_aTempFiles.clear ();
      return ret;
    });
    if (aFiles.isNotEmpty ())
    {
      s_aLogger.info ("Deleting " + aFiles.size () + " temporary files");
      for (final File aFile : aFiles)
      {
        if (s_aLogger.isDebugEnabled ())
          s_aLogger.debug ("Deleting temporary file " + aFile.getAbsolutePath ());
        final FileIOError aError = AS4IOHelper.getFileOperationManager ().deleteFileIfExisting (aFile);
        if (aError.isFailure ())
          s_aLogger.warn ("  Failed to delete " + aFile.getAbsolutePath () + ": " + aError.toString ());
      }
    }
  }
}
