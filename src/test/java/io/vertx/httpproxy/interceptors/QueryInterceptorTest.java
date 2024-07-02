package io.vertx.httpproxy.interceptors;

import io.netty.handler.codec.http.QueryStringDecoder;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.httpproxy.ProxyOptions;
import io.vertx.httpproxy.ProxyTestBase;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:wangzengyi1935@163.com">Zengyi Wang</a>
 */
public class QueryInterceptorTest extends ProxyTestBase {

  public QueryInterceptorTest(ProxyOptions options) {
    super(options);
  }

  @Test
  public void addParamTest(TestContext ctx) {
    Async latch = ctx.async();
    SocketAddress backend = startHttpBackend(ctx, 8081, req -> {
      QueryStringDecoder decoder = new QueryStringDecoder(req.uri());
      Map<String, List<String>> parameters = decoder.parameters();
      ctx.assertEquals(parameters.get("k1").getFirst(), "v1");
      ctx.assertEquals(parameters.get("k2").getFirst(), "v2");
      req.response().end("Hello");
    });

    startProxy(proxy -> proxy.origin(backend)
      .addInterceptor(QueryInterceptor.setQueryParam("k1", "v1")));

    vertx.createHttpClient().request(HttpMethod.GET, 8080, "localhost", "/hello/world?k2=v2")
      .compose(HttpClientRequest::send)
      .onComplete(ctx.asyncAssertSuccess(resp -> latch.complete()));
  }

  @Test
  public void removeParamTest(TestContext ctx) {
    Async latch = ctx.async();
    SocketAddress backend = startHttpBackend(ctx, 8081, req -> {
      QueryStringDecoder decoder = new QueryStringDecoder(req.uri());
      Map<String, List<String>> parameters = decoder.parameters();
      ctx.assertEquals(parameters.get("k1").getFirst(), "v1");
      ctx.assertTrue(parameters.get("k2") == null);
      req.response().end("Hello");
    });

    startProxy(proxy -> proxy.origin(backend)
      .addInterceptor(QueryInterceptor.removeQueryParam("k2")));

    vertx.createHttpClient().request(HttpMethod.GET, 8080, "localhost", "/hello/world?k1=v1&k2=v2")
      .compose(HttpClientRequest::send)
      .onComplete(ctx.asyncAssertSuccess(resp -> latch.complete()));
  }

}
