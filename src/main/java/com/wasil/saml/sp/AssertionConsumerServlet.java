package com.wasil.saml.sp;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet implementation class AssertionConsumerServlet
 */
public class AssertionConsumerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private final static Logger logger = LoggerFactory.getLogger(AssertionConsumerServlet.class);
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AssertionConsumerServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setAttribute("originalResponse", request.getParameter("SAMLResponse"));
		request.getRequestDispatcher("sp.jsp").forward(request,response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setAttribute("originalResponse", request.getParameter("SAMLResponse"));
		request.getRequestDispatcher("sp.jsp").forward(request,response);
	}
	
}
