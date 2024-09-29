package top.misec.applemonitor.job;

import lombok.extern.slf4j.Slf4j;
import top.misec.applemonitor.config.AppCfg;
import top.misec.applemonitor.config.CfgSingleton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author zhuxiangxiao
 * @version 1.0
 * @date 2024/9/29 16:09
 */
@Slf4j
public class HealthCheckRunnable implements Runnable {
    @Override
    public void run() {
        AppCfg appCfg = CfgSingleton.getInstance().config;
        int port = appCfg.getAppleTaskConfig().healthCheckPort;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log.info("health check Server started on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                handleRequest(clientSocket);
            }
        } catch (IOException e) {
            log.error("HealthCheckRunnable error", e);
        }
    }

    private void handleRequest(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream out = clientSocket.getOutputStream()) {
            // 读取请求行
            String requestLine = in.readLine();
            if (requestLine != null && requestLine.startsWith("GET /health")) {
                // 发送响应
                String response = "HTTP/1.1 200 OK\r\n" +
                        "Content-Length: 2\r\n" +
                        "Content-Type: text/plain\r\n" +
                        "\r\n" +
                        "OK";
                log.info("HealthCheck success");
                out.write(response.getBytes());
            } else {
                // 发送404响应
                String response = "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "\r\n";
                out.write(response.getBytes());
            }
        } catch (Exception e) {
            log.error("HealthCheckRunnable error", e);
        }
    }
}
