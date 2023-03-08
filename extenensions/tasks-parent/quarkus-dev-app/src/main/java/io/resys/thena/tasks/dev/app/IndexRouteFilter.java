package io.resys.thena.tasks.dev.app;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.vertx.web.RouteFilter;
import io.vertx.ext.web.RoutingContext;

@ApplicationScoped
public class IndexRouteFilter {
  
  @RouteFilter(400)
  void myRedirector(RoutingContext rc) {
    String uri = rc.request().uri();
    
    
    if (!uri.startsWith("/q") && !uri.startsWith("/portal/")) {
      rc.reroute("/portal/");
      return;
    }
    

    
    rc.next();
  }
}
