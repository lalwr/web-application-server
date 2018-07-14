package controller;

import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import util.HttpRequestUtils;

import java.util.Collection;
import java.util.Map;

public class ListUserController extends AbstractController{

    @Override
    protected void doGet(HttpRequest request, HttpResponse response) {
        if (!isLogin(request.getHeader("Cookie"))) {
            response.sendRedirect("/user/login.html");
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
    }

    private boolean isLogin(String cookieValue) {
        Map<String, String> cookies = HttpRequestUtils.parseCookies(cookieValue);
        String value = cookies.get("logined");
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

}
