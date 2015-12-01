package io.vertx.ext.web.handler.sockjs;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * Specify a match to allow for inbound and outbound traffic using the
 * {@link BridgeOptions}.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@DataObject
public class PermittedOptions extends io.vertx.ext.bridge.PermittedOptions {

  /**
   * The default permitted required authority : {@code null}.
   */
  public static String DEFAULT_REQUIRED_AUTHORITY = null;

  private String requiredAuthority;

  public PermittedOptions() {
    super();
  }

  public PermittedOptions(PermittedOptions that) {
    super(that);
    requiredAuthority = that.requiredAuthority;
  }

  public PermittedOptions(JsonObject json) {
    super(json);
    requiredAuthority = json.getString("requiredAuthority", DEFAULT_REQUIRED_AUTHORITY);
  }

  public String getRequiredAuthority() {
    return requiredAuthority;
  }

  /**
   * Declare a specific authority that user must have in order to allow messages
   *
   * @param requiredAuthority the authority
   * @return a reference to this, so the API can be used fluently
   */
  public PermittedOptions setRequiredAuthority(String requiredAuthority) {
    this.requiredAuthority = requiredAuthority;
    return this;
  }
}
