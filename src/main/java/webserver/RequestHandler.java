package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
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
            HttpRequest request = new HttpRequest(in);
            HttpResponse response = new HttpResponse(out);

            String path = getDefaultUrl(request.getPath());

            if("/user/create".equals(path)){
                String userId = request.getParameter("userId");
                String password = request.getParameter("password");
                String name = request.getParameter("name");
                String email = request.getParameter("email");
                User user = new User(userId, password, name, email);
                log.debug("user : {}", user);
                DataBase.addUser(user);
                response.sendRedirect("/index.html");
            }else if("/user/login".equals(path)){
                User user = DataBase.findUserById(request.getParameter("userId"));
                String password = request.getParameter("password");
                if(user != null){
                    if(user.login(password)){
                        response.addHeader("Set-Cookie", "logined=true");
                        response.sendRedirect("/index.html");
                    }else if(password.equals(user.getPassword())){
                        response.sendRedirect("/user/login_failed.html");
                    }
                }else{
                    response.sendRedirect("/user/login_failed.html");
                }

            }else if("/user/list".equals(path)){
                if(!request.isLogin()){
                    response.sendRedirect("/ser/login.html");
                    return;
                }

                Collection<User> users = DataBase.findAll();
                StringBuffer sb = new StringBuffer();

                sb.append("<table border='1'>");
                users.forEach((user) -> {
                    sb.append("<tr>");
                    sb.append("<td> 유저 아이디 : " + user.getUserId() + "</td>");
                    sb.append("<td> 유저 이메일 : " + user.getEmail() + "</td>");
                    sb.append("<td> 유저 이름 : " + user.getName() + "</td>");
                    sb.append("</tr>");
                });
                sb.append("</table>");
                response.forwardBody(sb.toString());
            }else{
                response.forward(path);
            }
        } catch (IOException io) {
            log.error(io.getMessage());
        }
    }

    private String getDefaultUrl(String path){
        if(path.equals("/")){
            path = "/index.html";
        }
        return path;
    }
}
