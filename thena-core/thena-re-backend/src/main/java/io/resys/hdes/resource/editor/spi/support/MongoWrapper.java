package io.resys.hdes.resource.editor.spi.support;

/*-
 * #%L
 * hdes-re-backend
 * %%
 * Copyright (C) 2020 - 2021 Copyright 2020 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.function.Function;

import org.immutables.value.Value;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

@Value.Immutable
public interface MongoWrapper {
  MongoClient getClient();
  MongoDbConfig getConfig();
  MongoDatabase getDb();

  @Value.Immutable
  interface MongoDbConfig {
    String getDb();
    String getProjects();
  }
  
  @FunctionalInterface
  interface MongoTransaction {
    <T> T accept(Function<MongoClient, T> action);
  }
}
