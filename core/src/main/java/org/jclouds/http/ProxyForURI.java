/**
 * Licensed to jclouds, Inc. (jclouds) under one or more
 * contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  jclouds licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jclouds.http;

import static com.google.common.collect.Iterables.getLast;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;

import javax.inject.Named;
import javax.inject.Singleton;

import org.jclouds.Constants;

import com.google.common.base.Function;
import com.google.inject.Inject;

/**
 * @author Adrian Cole
 */
@Singleton
public class ProxyForURI implements Function<URI, Proxy> {

   @Inject(optional = true)
   @Named(Constants.PROPERTY_PROXY_SYSTEM)
   private boolean systemProxies = System.getProperty("java.net.useSystemProxies") != null ? Boolean
         .parseBoolean(System.getProperty("java.net.useSystemProxies")) : false;

   @Inject(optional = true)
   @Named(Constants.PROPERTY_PROXY_HOST)
   private String proxyHost;
   @Inject(optional = true)
   @Named(Constants.PROPERTY_PROXY_PORT)
   private Integer proxyPort;
   @Inject(optional = true)
   @Named(Constants.PROPERTY_PROXY_USER)
   private String proxyUser;
   @Inject(optional = true)
   @Named(Constants.PROPERTY_PROXY_PASSWORD)
   private String proxyPassword;

   /**
    * @see org.jclouds.Constants.PROPERTY_PROXY_HOST
    */
   private String getProxyHost() {
      return proxyHost;
   }

   /**
    * @see org.jclouds.Constants.PROPERTY_PROXY_PORT
    */
   private Integer getProxyPort() {
      return proxyPort;
   }

   /**
    * @see org.jclouds.Constants.PROPERTY_PROXY_USER
    */
   private String getProxyUser() {
      return proxyUser;
   }

   /**
    * @see org.jclouds.Constants.PROPERTY_PROXY_PASSWORD
    */
   private String getProxyPassword() {
      return proxyPassword;
   }

   private boolean useSystemProxies() {
      return systemProxies;
   }

   /**
    * @param endpoint
    *           <ul>
    *           <li>http URI for http connections</li>
    *           <li>https URI for https connections</li>
    *           <li>ftp URI for ftp connections</li>
    *           <li>socket://host:port for tcp client sockets connections</li>
    *           </ul>
    */
   @Override
   public Proxy apply(URI endpoint) {
      if (useSystemProxies()) {
         System.setProperty("java.net.useSystemProxies", "true");
         Iterable<Proxy> proxies = ProxySelector.getDefault().select(endpoint);
         return getLast(proxies);
      } else if (getProxyHost() != null) {
         SocketAddress addr = new InetSocketAddress(getProxyHost(), getProxyPort());
         Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
         if (getProxyUser() != null && getProxyPassword() != null) {
            Authenticator authenticator = new Authenticator() {
               public PasswordAuthentication getPasswordAuthentication() {
                  return new PasswordAuthentication(getProxyUser(), getProxyPassword().toCharArray());
               }
            };
            Authenticator.setDefault(authenticator);
         }
         return proxy;
      } else {
         return Proxy.NO_PROXY;
      }
   }
}
