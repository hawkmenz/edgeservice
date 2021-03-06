/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.demo.signature;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.netflix.config.DynamicStringListProperty;

import io.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import io.servicecomb.foundation.vertx.http.HttpServletResponseEx;

public class SignatureUtils {
  private static final DynamicStringListProperty PARAM_NAMES_PROPERTY =
      new DynamicStringListProperty("servicecomb.demo.signature.param-names", Arrays.asList("userId"));

  private static List<String> paramNames = PARAM_NAMES_PROPERTY.get();
  static {
    PARAM_NAMES_PROPERTY.addCallback(() -> {
      List<String> tmpNames = PARAM_NAMES_PROPERTY.get();
      tmpNames.sort((n1, n2) -> {
        return n1.compareTo(n2);
      });
      paramNames = tmpNames;
    });
  }

  public static String genSignature(HttpServletRequestEx requestEx) {
    Hasher hasher = Hashing.sha256().newHasher();
    hasher.putString(requestEx.getRequestURI(), StandardCharsets.UTF_8);
    for (String paramName : paramNames) {
      String paramValue = requestEx.getHeader(paramName);
      if (paramValue != null) {
        hasher.putString(paramName, StandardCharsets.UTF_8);
        hasher.putString(paramValue, StandardCharsets.UTF_8);
        System.out.printf("%s %s\n", paramName, paramValue);
      }
    }

    byte[] bytes = requestEx.getBodyBytes();
    if (bytes != null) {
      hasher.putBytes(bytes, 0, requestEx.getBodyBytesLength());
    }

    return hasher.hash().toString();
  }

  public static String genSignature(HttpServletResponseEx responseEx) {
    Hasher hasher = Hashing.sha256().newHasher();
    byte[] bytes = responseEx.getBodyBytes();
    if (bytes != null) {
      hasher.putBytes(bytes, 0, responseEx.getBodyBytesLength());
    }

    return hasher.hash().toString();
  }
}
