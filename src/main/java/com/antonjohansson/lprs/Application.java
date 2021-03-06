/**
 * Copyright (c) Anton Johansson <antoon.johansson@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.antonjohansson.lprs;

import static java.util.EnumSet.allOf;
import static org.eclipse.jetty.servlet.ServletContextHandler.SESSIONS;

import javax.servlet.DispatcherType;

import org.apache.logging.log4j.jul.LogManager;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.antonjohansson.lprs.controller.configuration.Configuration;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;

/**
 * Defines the entry point.
 */
public class Application
{
    /**
     * The application main entry-point.
     */
    public static void main(String[] args) throws Exception
    {
        System.setProperty("java.util.logging.manager", LogManager.class.getName());

        try
        {
            Injector injector = Guice.createInjector(new ServiceModule());
            Configuration configuration = injector.getInstance(Configuration.class);

            ServletContextHandler handler = new ServletContextHandler(SESSIONS);
            handler.setContextPath("/");
            handler.addFilter(GuiceFilter.class, "/*", allOf(DispatcherType.class));
            handler.addServlet(DefaultServlet.class, "/");

            Server server = new Server(configuration.getPort());
            server.setHandler(handler);
            server.start();
            server.join();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }
}
