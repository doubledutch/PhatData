package me.doubledutch.phat.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class StatusServlet extends HttpServlet{
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		response.getWriter().append("{\"data\":42}");
		return;
	}
}