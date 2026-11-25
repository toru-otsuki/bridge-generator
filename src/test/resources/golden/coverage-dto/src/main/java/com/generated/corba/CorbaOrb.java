package com.generated.corba;

import java.time.Duration;
import java.util.Properties;
import org.omg.CORBA.ORB;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class CorbaOrb implements DisposableBean {
  private static final String GMBAL_PROPERTY = "org.glassfish.gmbal.no.multipleUpperBoundsException";
  private static final String REGISTER_MBEANS_PROPERTY = "com.sun.corba.ee.ORBRegisterMBeans";
  private static final String WAIT_FOR_RESPONSE_PROPERTY = "com.sun.corba.ee.transport.ORBWaitForResponseTimeout";
  private static final String CONNECT_TIMEOUTS_PROPERTY = "com.sun.corba.ee.transport.ORBTCPConnectTimeouts";

  private final ORB orb;

  public CorbaOrb(Environment env) {
    if (System.getProperty(GMBAL_PROPERTY) == null) {
      System.setProperty(GMBAL_PROPERTY, "true");
    }
    Properties properties = new Properties();
    properties.setProperty(REGISTER_MBEANS_PROPERTY, "false");
    Binder binder = Binder.get(env);
    binder.bind("corba.response-timeout", Duration.class)
        .ifBound(d -> properties.setProperty(WAIT_FOR_RESPONSE_PROPERTY, millis(d, "corba.response-timeout")));
    binder.bind("corba.connect-timeout", Duration.class)
        .ifBound(d -> properties.setProperty(CONNECT_TIMEOUTS_PROPERTY, "250:" + millis(d, "corba.connect-timeout") + ":100:5000"));
    binder.bind("corba.orb", Bindable.mapOf(String.class, String.class)).ifBound(properties::putAll);
    this.orb = ORB.init(new String[0], properties);
    com.generated.mapper.AnyMapper.useOrb(orb);
  }

  private static String millis(Duration d, String name) {
    long ms = d.toMillis();
    if (ms <= 0 || ms > Integer.MAX_VALUE) {
      throw new IllegalArgumentException(name + " must be between 1 ms and about 24 days: " + d);
    }
    return Long.toString(ms);
  }

  public ORB orb() {
    return orb;
  }

  @Override
  public void destroy() {
    com.generated.mapper.AnyMapper.releaseOrb(orb);
    orb.shutdown(false);
    orb.destroy();
  }
}
