package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line = br.readLine();
            if(line == null) return;
            log.debug("request line : {}", line);

            String[] tokens = line.split(" ");

            byte[] body = "hello world".getBytes();
            File file = new File("./webapp" + tokens[1]);
            if(file.exists()){
                body = Files.readAllBytes(new File("./webapp" + tokens[1]).toPath());
            }

            String url = tokens[1];
            log.debug("url : {}", url);
            if( url.contains("?") ){
                int index = url.indexOf("?");
                String requestPath = url.substring(0, index);
                String params = url.substring(index+1);

                Map<String, String> paramaters = HttpRequestUtils.parseQueryString(params);
                String userId = paramaters.get("userId");
                String password = paramaters.get("password");
                String name = paramaters.get("name");
                String email = paramaters.get("email");
                User user = new User(userId, password, name, email);
                log.debug("user : {}", user);
            }

            String[] contentLength = new String[2];
            while(!"".equals(line)){
                line = br.readLine();
                log.debug("header : {}", line);
                if(line.startsWith("Content-Length:")){
                    contentLength = line.split(" ");
                }
            }

            if("POST".equals(tokens[0])){
                String params = IOUtils.readData(br, Integer.parseInt(contentLength[1]));
                log.debug("post params {}", params);

                Map<String, String> paramaters = HttpRequestUtils.parseQueryString(params);
                String userId = paramaters.get("userId");
                String password = paramaters.get("password");
                String name = paramaters.get("name");
                String email = paramaters.get("email");
                User user = new User(userId, password, name, email);
                log.debug("user : {}", user);
            }

            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            DataOutputStream dos = new DataOutputStream(out);
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
