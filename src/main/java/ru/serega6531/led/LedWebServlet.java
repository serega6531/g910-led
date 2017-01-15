package ru.serega6531.led;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

@WebServlet("/led")
public class LedWebServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        Main.initLibUsb();
    }

    @Override
    public void destroy() {
        Main.freeLibUsb();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        if(request.getParameter("test") != null){
            Keyboard keyboard = new Keyboard();
            keyboard.setFXColorCycleKeys((byte) 0x40);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/index.html")));

        String line;
        while ((line = reader.readLine()) != null){
            out.println(line);
        }
    }
}
