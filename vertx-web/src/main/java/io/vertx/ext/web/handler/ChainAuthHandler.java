/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.ext.web.handler;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.handler.impl.AndChainAuthHandlerImpl;
import io.vertx.ext.web.handler.impl.OrChainAuthHandlerImpl;

/**
 * An auth handler that chains to a sequence of handlers.
 *
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
@VertxGen
public interface ChainAuthHandler extends AuthHandler {

  /**
   * Same as {@link ChainAuthHandler#or()}
   *
    * @return
   */
  static ChainAuthHandler create() {
    return new OrChainAuthHandlerImpl();
  }

  /**
   * Returns a ChainAuthHandler that executes every of the appended auth handlers till returns valid credentials
   *
   * @return
   */
  static ChainAuthHandler or() {
    return new OrChainAuthHandlerImpl();
  }

  /**
   * Returns a ChainAuthHandler that does th
   *
   * @return
   */
  static ChainAuthHandler and() {
    return new AndChainAuthHandlerImpl();
  }

  /**
   * Appends a auth provider to the chain.
   *
   * @param authHandler auth handler
   * @return self
   *
   */
  @Fluent
  ChainAuthHandler append(AuthHandler authHandler);

  /**
   * Removes a provider from the chain.
   *
   * @param authHandler provider to remove
   * @return true if provider was removed, false if non existent in the chain.
   */
  boolean remove(AuthHandler authHandler);

  /**
   * Clears the chain.
   */
  void clear();
}
