/*
 * Copyright 2017 Masahiro Ide
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.linecorp.armeria.sample;

import java.util.concurrent.CompletableFuture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.context.annotation.Bean;

import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.common.http.HttpRequest;
import com.linecorp.armeria.common.http.HttpResponseWriter;
import com.linecorp.armeria.common.http.HttpStatus;
import com.linecorp.armeria.server.PathMapping;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.http.AbstractHttpService;
import com.linecorp.armeria.server.logging.LoggingService;
import com.linecorp.armeria.spring.HttpServiceRegistrationBean;

@SpringBootApplication
public class SampleApplication {

    @Bean
    HttpServiceRegistrationBean httpService(final EmbeddedWebApplicationContext applicationContext) {
        CompletableFuture<Object> blockedFuture = new CompletableFuture<>();
        return new HttpServiceRegistrationBean()
                .setServiceName("buggy-service")
                .setService(new AbstractHttpService() {
                    @Override
                    protected void doGet(ServiceRequestContext ctx, HttpRequest req, HttpResponseWriter res)
                            throws Exception {
                        blockedFuture.get(); // XXXX This is bug!!! Don't block armeria event loop.
                        res.respond(HttpStatus.OK, MediaType.ANY_TEXT_TYPE, "OK");
                    }
                }.decorate(LoggingService.newDecorator()))
                .setPathMapping(PathMapping.ofPrefix("/my-buggy-service"));
    }

    public static void main(String[] args) {
        SpringApplication.run(SampleApplication.class, args);
    }
}