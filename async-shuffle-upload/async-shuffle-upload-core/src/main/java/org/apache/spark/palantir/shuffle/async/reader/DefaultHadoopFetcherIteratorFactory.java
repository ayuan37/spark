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

package org.apache.spark.palantir.shuffle.async.reader;

import java.util.Collection;

import org.apache.spark.palantir.shuffle.async.client.ShuffleClient;
import org.apache.spark.palantir.shuffle.async.metrics.HadoopFetcherIteratorMetrics;
import org.apache.spark.shuffle.api.ShuffleBlockInfo;

public final class DefaultHadoopFetcherIteratorFactory implements HadoopFetcherIteratorFactory {

  private final ShuffleClient shuffleClient;
  private final HadoopFetcherIteratorMetrics metrics;

  public DefaultHadoopFetcherIteratorFactory(
      ShuffleClient shuffleClient,
      HadoopFetcherIteratorMetrics metrics) {
    this.shuffleClient = shuffleClient;
    this.metrics = metrics;
  }

  @Override
  public HadoopFetcherIterator createFetcherIteratorForBlocks(
      Collection<ShuffleBlockInfo> blocks) {
    DefaultHadoopFetcherIterator iterator = new DefaultHadoopFetcherIterator(
        shuffleClient,
        blocks,
        metrics);
    iterator.fetchDataFromS3();
    return iterator;
  }
}