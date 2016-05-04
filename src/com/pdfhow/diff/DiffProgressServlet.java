package com.pdfhow.diff;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

@WebServlet("/DiffProgressServlet")
public class DiffProgressServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public DiffProgressServlet() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.getWriter().append("Served at: ").append(request.getContextPath());

		try {
			JSONObject json = new JSONObject();

			if (request.getSession().getAttribute("DiffWorker") != null) {
				DiffWorker worker = (DiffWorker) request.getSession().getAttribute("DiffWorker");
				float progress = worker.getProgress();

				json.put("progress", progress);

				response.setContentType("application/json");
				response.getWriter().write(json.toString());
			} else {
				json.put("progress", -1);
				response.setContentType("application/json");
				response.getWriter().write(json.toString());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
