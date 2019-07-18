/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.shuffle.api;

import java.io.IOException;
import java.io.OutputStream;

import java.nio.channels.Channels;
import org.apache.spark.annotation.Private;
import org.apache.spark.shuffle.sort.DefaultTransferrableWritableByteChannel;

/**
 * :: Private ::
 * An interface for opening streams to persist partition bytes to a backing data store.
 * <p>
 * This writer stores bytes for one (mapper, reducer) pair, corresponding to one shuffle
 * block.
 *
 * @since 3.0.0
 */
@Private
public interface ShufflePartitionWriter {

  /**
   * Open and return an {@link OutputStream} that can write bytes to the underlying
   * data store.
   * <p>
   * This method will only be called once on this partition writer in the map task, to write the
   * bytes to the partition. The output stream will only be used to write the bytes for this
   * partition. The map task closes this output stream upon writing all the bytes for this
   * block, or if the write fails for any reason.
   * <p>
   * Implementations that intend on combining the bytes for all the partitions written by this
   * map task should reuse the same OutputStream instance across all the partition writers provided
   * by the parent {@link ShuffleMapOutputWriter}. If one does so, ensure that
   * {@link OutputStream#close()} does not close the resource, since it will be reused across
   * partition writes. The underlying resources should be cleaned up in
   * {@link ShuffleMapOutputWriter#commitAllPartitions()} and
   * {@link ShuffleMapOutputWriter#abort(Throwable)}.
   */
  OutputStream openStream() throws IOException;

  /**
   * Opens and returns a {@link TransferrableWritableByteChannel} for transferring bytes from
   * input byte channels to the underlying shuffle data store.
   * <p>
   * This method will only be called once on this partition writer in the map task, to write the
   * bytes to the partition. The channel will only be used to write the bytes for this
   * partition. The map task closes this channel upon writing all the bytes for this
   * block, or if the write fails for any reason.
   * <p>
   * Implementations that intend on combining the bytes for all the partitions written by this
   * map task should reuse the same channel instance across all the partition writers provided
   * by the parent {@link ShuffleMapOutputWriter}. If one does so, ensure that
   * {@link TransferrableWritableByteChannel#close()} does not close the resource, since it
   * will be reused across partition writes. The underlying resources should be cleaned up in
   * {@link ShuffleMapOutputWriter#commitAllPartitions()} and
   * {@link ShuffleMapOutputWriter#abort(Throwable)}.
   * <p>
   * This method is primarily for advanced optimizations where bytes can be copied from the input
   * spill files to the output channel without copying data into memory.
   * <p>
   * The default implementation should be sufficient for most situations. Only override this
   * method if there is a very specific optimization that needs to be built.
   */
  default TransferrableWritableByteChannel openTransferrableChannel() throws IOException {
    return new DefaultTransferrableWritableByteChannel(
        Channels.newChannel(openStream()));
  }

  /**
   * Returns the number of bytes written either by this writer's output stream opened by
   * {@link #openStream()} or the byte channel opened by {@link #openTransferrableChannel()}.
   * <p>
   * This can be different from the number of bytes given by the caller. For example, the
   * stream might compress or encrypt the bytes before persisting the data to the backing
   * data store.
   */
  long getNumBytesWritten();
}