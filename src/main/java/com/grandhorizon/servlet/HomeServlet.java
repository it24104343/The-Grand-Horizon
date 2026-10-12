package com.grandhorizon.servlet;

import com.grandhorizon.dao.RoomDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/rooms")
public class HomeServlet extends HttpServlet {
    private final RoomDAO roomDAO = new RoomDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("rooms", roomDAO.findAvailableRooms());
            request.getRequestDispatcher("/index.jsp").forward(request, response);
        } catch (IOException e) {
            throw new ServletException("Unable to load available rooms.", e);
        }
    }
}
