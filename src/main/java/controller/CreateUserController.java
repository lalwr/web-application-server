package controller;

import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateUserController extends AbstractController{
    private static final Logger log = LoggerFactory.getLogger(CreateUserController.class);

    @Override
    protected void doPost(HttpRequest request, HttpResponse response) {
        String userId = request.getParameter("userId");
        String password = request.getParameter("password");
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        User user = new User(userId, password, name, email);
        log.debug("user : {}", user);
        DataBase.addUser(user);
        response.sendRedirect("/index.html");
    }

}
