/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pdfhow.diff;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Random;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author lim16
 */
@WebServlet(name = "DiffServlet", urlPatterns = { "/DiffServlet" })
@MultipartConfig
public class DiffServlet extends HttpServlet {

	private static final long serialVersionUID = 1278840531343993218L;

	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (!ServletFileUpload.isMultipartContent(request)) {
			throw new IllegalArgumentException(
					"Request is not multipart, please 'multipart/form-data' enctype for your form.");
		}

		OutputStream baseOut = null;
		OutputStream testOut = null;
		InputStream baseFilecontent = null;
		InputStream testFilecontent = null;
		PrintWriter responseWriter = response.getWriter();
		try {
			Part basePDF = request.getPart("base_pdf");
			String baseFileName = getFileName(basePDF);
			if (baseFileName == null) {

			}
			baseFileName += "_base";
			String path = createTempFolder(request.getServletContext().getRealPath("/") + "pdfs/");
			baseOut = new FileOutputStream(path + baseFileName);
			baseFilecontent = basePDF.getInputStream();
			copyStream(baseFilecontent, baseOut);

			Part testPDF = request.getPart("base_pdf");
			String testFileName = getFileName(testPDF);
			if (testFileName == null) {

			}
			testFileName += "_test";
			testOut = new FileOutputStream(path + testFileName);
			testFilecontent = basePDF.getInputStream();
			copyStream(testFilecontent, testOut);

			response.setContentType("text/html");
			response.setHeader("Cache-control", "no-cache, no-store");
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Expires", "-1");

			response.setHeader("Access-Control-Allow-Origin", "*");
			response.setHeader("Access-Control-Allow-Methods", "POST");
			response.setHeader("Access-Control-Allow-Headers", "Content-Type");
			response.setHeader("Access-Control-Max-Age", "86400");

			JSONObject myObj = new JSONObject();
			myObj.put("success", true);
			myObj.put("isDiff", false);
			responseWriter.println(myObj.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			if (baseOut != null) {
				baseOut.close();
			}
			if (testOut != null) {
				testOut.close();
			}
			if (responseWriter != null) {
				responseWriter.close();
			}
			if (baseFilecontent != null) {
				baseFilecontent.close();
			}
			if (testFilecontent != null) {
				testFilecontent.close();
			}
		}
	}

	private static String createTempFolder(String path) {
		String temp = "" + System.currentTimeMillis() + (new Random()).nextLong();
		return path += temp + "/";
	}

	private static void copyStream(InputStream in, OutputStream out) throws IOException {
		int read = 0;
		byte[] bytes = new byte[1024];
		while ((read = in.read(bytes)) != -1) {
			out.write(bytes, 0, read);
		}
	}

	private String getFileName(final Part part) {
		for (String content : part.getHeader("content-disposition").split(";")) {
			if (content.trim().startsWith("filename")) {
				return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
			}
		}
		return null;
	}

	protected void doPost_1(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (!ServletFileUpload.isMultipartContent(request)) {
			throw new IllegalArgumentException(
					"Request is not multipart, please 'multipart/form-data' enctype for your form.");
		}

		ServletFileUpload uploadHandler = new ServletFileUpload(new DiskFileItemFactory());
		// PrintWriter writer = response.getWriter();
		try {
			List<FileItem> items = uploadHandler.parseRequest(request);
			for (FileItem item : items) {
				System.out.print(item.getContentType());
				System.out.print(item.getFieldName());
				if (!item.isFormField()) {
					File file = new File(request.getServletContext().getRealPath("/") + "pdfs/", item.getName());
					// item.write(file);
				}
			}

			RequestDispatcher view = request.getRequestDispatcher("/index.html");
			view.forward(request, response);
		} catch (FileUploadException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			// writer.close();
		}
	}

	@Override
	public String getServletInfo() {
		return "Short description";
	}// </editor-fold>

}
