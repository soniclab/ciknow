package ciknow.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import ciknow.util.Beans;

public abstract class BaseServlet extends HttpServlet {

    public void init() throws ServletException {
        ServletContext sc = getServletContext();
        Beans.init(sc);
    }
}
