package com.pdfhow.diff.tset;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "SyncServlet", urlPatterns = { "/SyncServlet" })
public class SyncServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public static int index = 0;

	public SyncServlet() {
		System.out.println("SyncServlet - " + index++);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String seq = req.getParameter("seq");

		try {
			Thread.sleep(500 * Integer.parseInt(seq));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		resp.getWriter().write(seq);

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
