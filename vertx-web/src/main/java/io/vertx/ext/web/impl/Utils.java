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

package io.vertx.ext.web.impl;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;


/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 */
public class Utils extends io.vertx.core.impl.Utils {

  public static ClassLoader getClassLoader() {
    ClassLoader tccl = Thread.currentThread().getContextClassLoader();
    return tccl == null ? Utils.class.getClassLoader() : tccl;
  }

  public static Buffer readResourceToBuffer(String resource) {
    ClassLoader cl = getClassLoader();
    try {
      Buffer buffer = Buffer.buffer();
      try (InputStream in = cl.getResourceAsStream(resource)) {
        if (in == null) {
          return null;
        }
        int read;
        byte[] data = new byte[4096];
        while ((read = in.read(data, 0, data.length)) != -1) {
          if (read == data.length) {
            buffer.appendBytes(data);
          } else {
            byte[] slice = new byte[read];
            System.arraycopy(data, 0, slice, 0, slice.length);
            buffer.appendBytes(slice);
          }
        }
      }
      return buffer;
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  /**
   * Reads from file or classpath using UTF-8
   */
  public static String readFileToString(Vertx vertx, String resource) {
    return readFileToString(vertx, resource, StandardCharsets.UTF_8);
  }

  /**
   * Reads from file or classpath using the provided charset
   */
  private static String readFileToString(Vertx vertx, String resource, Charset charset) {
    try {
      Buffer buff = vertx.fileSystem().readFileBlocking(resource);
      return buff.toString(charset);
    } catch (Exception e) {
      throw new VertxException(e);
    }
  }

  private static final ZoneId ZONE_GMT = ZoneId.of("GMT");

  public static String formatRFC1123DateTime(final long time) {
    return DateTimeFormatter.RFC_1123_DATE_TIME.format(Instant.ofEpochMilli(time).atZone(ZONE_GMT));
  }

  public static long parseRFC1123DateTime(final String header) {
    try {
      return header == null || header.isEmpty() ? -1 :
        LocalDateTime.parse(header, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant(ZoneOffset.UTC).toEpochMilli();
    } catch (DateTimeParseException ex) {
      return -1;
    }
  }

  public static String pathOffset(String path, RoutingContext context) {
    int prefixLen = 0;
    String mountPoint = context.mountPoint();
    if (mountPoint != null) {
      prefixLen = mountPoint.length();
      // special case we need to verify if a trailing slash  is present and exclude
      if (mountPoint.charAt(mountPoint.length() - 1) == '/') {
        prefixLen--;
      }
    }
    String routePath = context.currentRoute().getPath();
    if (routePath != null) {
      prefixLen += routePath.length();
      // special case we need to verify if a trailing slash  is present and exclude
      if (routePath.charAt(routePath.length() - 1) == '/') {
        prefixLen--;
      }
    }
    return prefixLen != 0 ? path.substring(prefixLen) : path;
  }

  public static long secondsFactor(long millis) {
    return millis - (millis % 1000);
  }

  public static boolean isJsonContentType(String contentType) {
    return contentType.contains("application/json") || contentType.contains("+json");
  }

  public static boolean isXMLContentType(String contentType) {
    return contentType.contains("application/xml") || contentType.contains("text/xml") || contentType.contains("+xml");
  }

  public static void addToMapIfAbsent(MultiMap map, CharSequence key, String value) {
    if (!map.contains(key)) {
      map.set(key, value);
    }
  }

  /**
   * RegExp to check for no-cache token in Cache-Control.
   */
  private static final Pattern CACHE_CONTROL_NO_CACHE_REGEXP = Pattern.compile("(?:^|,)\\s*?no-cache\\s*?(?:,|$)");

  public static boolean fresh(RoutingContext ctx) {
    return fresh(ctx, -1);
  }

  public static boolean fresh(RoutingContext ctx, long lastModified) {

    final HttpServerRequest req = ctx.request();
    final HttpServerResponse res = ctx.response();


    // fields
    final String modifiedSince = req.getHeader(HttpHeaders.IF_MODIFIED_SINCE);
    final String noneMatch = req.getHeader(HttpHeaders.IF_NONE_MATCH);

    // unconditional request
    if (modifiedSince == null && noneMatch == null) {
      return false;
    }

    // Always return stale when Cache-Control: no-cache
    // to support end-to-end reload requests
    // https://tools.ietf.org/html/rfc2616#section-14.9.4
    final String cacheControl = req.getHeader(HttpHeaders.CACHE_CONTROL);
    if (cacheControl != null && CACHE_CONTROL_NO_CACHE_REGEXP.matcher(cacheControl).find()) {
      return false;
    }

    // if-none-match
    if (noneMatch != null && !"*".equals(noneMatch)) {
      final String etag = res.headers().get(HttpHeaders.ETAG);

      if (etag == null) {
        return false;
      }

      boolean etagStale = true;

      // lookup etags
      int end = 0;
      int start = 0;

      loop: for (int i = 0; i < noneMatch.length(); i++) {
        switch (noneMatch.charAt(i)) {
          case ' ':
            if (start == end) {
              start = end = i + 1;
            }
            break;
          case ',':
            String match = noneMatch.substring(start, end);
            if (match.equals(etag) || match.equals("W/" + etag) || ("W/" + match).equals(etag)) {
              etagStale = false;
              break loop;
            }
            start = end = i + 1;
            break;
          default:
            end = i + 1;
            break;
        }
      }

      if (etagStale) {
        return false;
      }
    }

    // if-modified-since
    if (modifiedSince != null) {
      if (lastModified == -1) {
        // no custom last modified provided, will use the response headers if any
        lastModified = parseRFC1123DateTime(res.headers().get(HttpHeaders.LAST_MODIFIED));
      }

      boolean modifiedStale = lastModified == -1 || !(lastModified <= parseRFC1123DateTime(modifiedSince));

      if (modifiedStale) {
        return false;
      }
    }

    return true;
  }
}
