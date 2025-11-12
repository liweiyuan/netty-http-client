package com.example;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TestHttpServer {
    private Server server;

    public void start(int port) throws Exception {
        server = new Server(port);

        // 创建一个处理器来处理HTTP请求
        TestServlet servlet = new TestServlet();
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.addServlet(new ServletHolder(servlet), "/*");

        // 注意：我们移除了ResourceHandler，因为它会干扰servlet的处理
        // ResourceHandler resourceHandler = new ResourceHandler();
        // resourceHandler.setDirectoriesListed(true);
        // resourceHandler.setWelcomeFiles(new String[]{"index.html"});
        // resourceHandler.setResourceBase(".");

        // HandlerList handlers = new HandlerList();
        // handlers.addHandler(resourceHandler);
        // handlers.addHandler(context);

        // 只使用servlet处理器
        server.setHandler(context);
        server.start();
        
        // 等待服务器完全启动
        while (!server.isStarted()) {
            Thread.sleep(10);
        }
    }

    public void stop() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    public boolean isRunning() {
        return server != null && server.isRunning();
    }

    public static class TestServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
            response.setContentType("text/plain");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("Hello from TestHttpServer");
        }

        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
            response.setContentType("text/plain");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("POST request received");
        }
        
        @Override
        protected void doPut(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
            response.setContentType("text/plain");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("PUT request received");
        }
        
        @Override
        protected void doDelete(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
            response.setContentType("text/plain");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("DELETE request received");
        }
    }
}