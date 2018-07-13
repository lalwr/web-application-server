package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.sql.DatabaseMetaData;
import java.util.Collection;
import java.util.Map;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import javax.xml.crypto.Data;

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
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            DataOutputStream dos = new DataOutputStream(out);

            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line = br.readLine();
            if(line == null) return;
            log.debug("request line : {}", line);

            String[] tokens = line.split(" ");

            byte[] body = "hello world".getBytes();
            String url = tokens[1];

            File file = new File("./webapp" + url);
            if(file.exists()){
                body = Files.readAllBytes(new File("./webapp" + url).toPath());
            }

            log.debug("url : {}", url);

            String[] contentLength = new String[2];
            boolean isLogin = false;
            while(!"".equals(line)){
                line = br.readLine();
                log.debug("header : {}", line);
                if(line.startsWith("Content-Length:")){
                    contentLength = line.split(" ");
                }
                if(line.contains("Cookie")){
                    String[] cookieValue = line.split(" ");
                    Map<String, String> cookie = HttpRequestUtils.parseCookies(cookieValue[1]);
                    String logined = cookie.get("logined");
                    isLogin = Boolean.parseBoolean(logined);
                }
            }

            String httpMethod = tokens[0];
            if("POST".equals(httpMethod)){
                String params = IOUtils.readData(br, Integer.parseInt(contentLength[1]));
                log.debug("post params {}", params);

                if("/user/login".equals(url)){
                    String cookie = "false";

                    Map<String, String> paramaters = HttpRequestUtils.parseQueryString(params);
                    String userId = paramaters.get("userId");
                    String password = paramaters.get("password");

                    User findUser = DataBase.findUserById(userId);
                    if(findUser == null){
                        cookie = "false";
                        body = Files.readAllBytes(new File("./webapp/user/login_failed.html").toPath());
                    }else if(!findUser.getPassword().equals(findUser.getPassword())){
                        cookie = "false";
                        body = Files.readAllBytes(new File("./webapp/user/login_failed.html").toPath());
                    }else if(findUser.getPassword().equals(findUser.getPassword())){
                        cookie = "true";
                    }
                    response200LoginHeader(dos, body.length, cookie);
                    responseBody(dos, body);
                }else if("/user/create".equals(url)){
                    Map<String, String> paramaters = HttpRequestUtils.parseQueryString(params);
                    String userId = paramaters.get("userId");
                    String password = paramaters.get("password");
                    String name = paramaters.get("name");
                    String email = paramaters.get("email");
                    User user = new User(userId, password, name, email);
                    log.debug("user : {}", user);
                    DataBase.addUser(user);

                    response302Header(dos);
                    responseBody(dos, body);
                }
            }else{

                if("/user/list".equals(url)){
                    if(!isLogin){
                        body = Files.readAllBytes(new File("./webapp/user/login.html").toPath());
                    }else{
                        Collection<User> userAll = DataBase.findAll();
                        StringBuffer sb = new StringBuffer();

                        sb.append("<table border='1'>");
                        userAll.forEach((user) -> {
                            sb.append("<tr>");
                            sb.append("<td> 유저 아이디 : " + user.getUserId() + "</td>");
                            sb.append("<td> 유저 이메일 : " + user.getEmail() + "</td>");
                            sb.append("<td> 유저 이름 : " + user.getName() + "</td>");
                            sb.append("</tr>");
                        });
                        sb.append("</table>");
                        body = sb.toString().getBytes();
                    }
                }
                response200Header(dos, body.length);
                responseBody(dos, body);
            }

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

    private void response200LoginHeader(DataOutputStream dos, int lengthOfBodyContent, String cookie) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("Set-Cookie: logined=" + cookie + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos){
        try {
            dos.writeBytes("HTTP/1.1 302 found \r\n");
            dos.writeBytes("Location: http://localhost:8080/index.html \r\n");
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
