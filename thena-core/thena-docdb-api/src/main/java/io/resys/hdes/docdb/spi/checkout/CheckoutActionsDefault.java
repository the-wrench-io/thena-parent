package io.resys.hdes.docdb.spi.checkout;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import io.resys.hdes.docdb.api.actions.CheckoutActions;
import io.resys.hdes.docdb.spi.ClientState;

public class CheckoutActionsDefault implements CheckoutActions {

  private final ClientState state;
  
  public CheckoutActionsDefault(ClientState state) {
    super();
    this.state = state;
  }

  @Override
  public CommitCheckout commit() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TagCheckout tag() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public HeadCheckout head() {
    // TODO Auto-generated method stub
    return null;
  }


}
