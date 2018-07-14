package controller;

import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import model.User;

public class LoginController extends AbstractController{

    @Override
    protected void doPost(HttpRequest request, HttpResponse response) {
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
    }

}
