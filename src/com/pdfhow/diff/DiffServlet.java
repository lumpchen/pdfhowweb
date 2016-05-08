
package com.pdfhow.diff;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.pdfhow.diff.ScriptMessage.SCRIPT_METHOD;

/**
 *
 * @author lim16
 */
@WebServlet(name = "DiffServlet", urlPatterns = { "/DiffServlet" }, asyncSupported = true)
@MultipartConfig
public class DiffServlet extends HttpServlet {
	
	private static final Queue<AsyncContext> queue = new ConcurrentLinkedQueue<AsyncContext>();
	
	private static final Map<String, AsyncContext> map = new ConcurrentHashMap<String, AsyncContext>();
	
	private static final BlockingQueue<ScriptMessage> messageQueue = new LinkedBlockingQueue<ScriptMessage>();

	private static final String JUNK = "<!-- Comet is a programming technique that enables web "
			+ "servers to send data to the client without having any need " + "for the client to request it. -->\n";

	private Thread notifierThread = null;

	private static final long serialVersionUID = 1278840531343993218L;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		Runnable notifierRunnable = new Runnable() {
			public void run() {
				boolean done = false;
				while (!done) {
					ScriptMessage cMessage = null;
					try {
						cMessage = messageQueue.take();
						AsyncContext ac = map.get(cMessage.getSessionID());
						if (ac == null) {
							continue;
						}
						try {
							PrintWriter acWriter = ac.getResponse().getWriter();
							acWriter.println(cMessage.getScriptMessage());
							acWriter.flush();
							
							if (SCRIPT_METHOD.timeout == cMessage.getMethod()) {
								removeCtx(ac);
							}
						} catch (IOException ex) {
							System.out.println(ex);
							queue.remove(ac);
						}
/*						for (AsyncContext ac : queue) {
							try {
								PrintWriter acWriter = ac.getResponse().getWriter();
								acWriter.println(cMessage);
								acWriter.flush();
							} catch (IOException ex) {
								System.out.println(ex);
								queue.remove(ac);
							}
						}*/
					} catch (InterruptedException iex) {
						done = true;
						System.out.println(iex);
					}
				}
			}
		};
		notifierThread = new Thread(notifierRunnable);
		notifierThread.start();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		res.setContentType("text/html");
		res.setHeader("Cache-Control", "private");
		res.setHeader("Pragma", "no-cache");

		PrintWriter writer = res.getWriter();
		// for Safari, Chrome, IE and Opera
		for (int i = 0; i < 10; i++) {
			writer.write(JUNK);
		}
		writer.flush();

		final AsyncContext ac = req.startAsync();
		
		final String uid = req.getQueryString();
//		ac.setTimeout(10 * 60 * 1000);
		ac.setTimeout(30 * 1000);
		ac.addListener(new AsyncListener() {
			public void onComplete(AsyncEvent event) throws IOException {
//				queue.remove(ac);
				removeCtx(ac);
			}

			public void onTimeout(AsyncEvent event) throws IOException {
				System.out.println(ac.toString() + " timeout");
				try {
					messageQueue.put(new ScriptMessage(uid, "timeout", ScriptMessage.SCRIPT_METHOD.timeout));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
//				queue.remove(ac);
//				removeCtx();
			}

			public void onError(AsyncEvent event) throws IOException {
//				queue.remove(ac);
				removeCtx(ac);
			}

			public void onStartAsync(AsyncEvent event) throws IOException {
			}
 		});
//		queue.add(ac);
		map.put(uid, ac);
	}
	
	private void removeCtx(AsyncContext ac) {
		Iterator<Entry<String, AsyncContext>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			if (it.next().getValue() == ac) {
				it.remove();
				break;
			}
		}
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "private");
		response.setHeader("Pragma", "no-cache");
		
		String uid = request.getQueryString();
		
		OutputStream baseOut = null;
		OutputStream testOut = null;
		InputStream baseFilecontent = null;
		InputStream testFilecontent = null;
		PrintWriter responseWriter = response.getWriter();
		try {

			String warRoot = request.getServletContext().getRealPath("/");
			File tempFolder = createTempFolder(warRoot + "pdfs/");

			Part basePDF = request.getPart("base_pdf");
			String baseFileName = getFileName(basePDF);
			if (baseFileName == null) {
				this.reponseError(response, "", null);
			}
			baseFileName = "base_" + baseFileName;
			File baseFile = new File(tempFolder, baseFileName);
			if (!baseFile.createNewFile()) {
				this.reponseError(response, "", null);
			}
			baseOut = new FileOutputStream(baseFile);
			baseFilecontent = basePDF.getInputStream();
			copyStream(baseFilecontent, baseOut);
			baseOut.close();

			Part testPDF = request.getPart("test_pdf");
			String testFileName = getFileName(testPDF);
			if (testFileName == null) {
				this.reponseError(response, "", null);
			}
			testFileName = "test_" + testFileName;
			File testFile = new File(tempFolder, testFileName);
			if (!testFile.createNewFile()) {
				this.reponseError(response, "", null);
			}
			testOut = new FileOutputStream(testFile);
			testFilecontent = testPDF.getInputStream();
			copyStream(testFilecontent, testOut);
			testOut.close();

			File reportDir = new File(tempFolder, "report");

			DiffWorker worker = new DiffWorker(baseFile, testFile, reportDir, messageQueue, uid);
//			request.getSession().setAttribute("DiffWorker", worker);
			worker.start();
			
			response.getWriter().println("success");
		} catch (Exception e) {
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
		/*
		OutputStream baseOut = null;
		OutputStream testOut = null;
		InputStream baseFilecontent = null;
		InputStream testFilecontent = null;
		PrintWriter responseWriter = response.getWriter();
		try {

			String warRoot = request.getServletContext().getRealPath("/");
			File tempFolder = createTempFolder(warRoot + "pdfs/");
			this.tempID = tempFolder.getName();

			Part basePDF = request.getPart("base_pdf");
			String baseFileName = getFileName(basePDF);
			if (baseFileName == null) {
				this.reponseError(response, "", null);
			}
			baseFileName = "base_" + baseFileName;
			File baseFile = new File(tempFolder, baseFileName);
			if (!baseFile.createNewFile()) {
				this.reponseError(response, "", null);
			}
			baseOut = new FileOutputStream(baseFile);
			baseFilecontent = basePDF.getInputStream();
			copyStream(baseFilecontent, baseOut);
			baseOut.close();

			Part testPDF = request.getPart("test_pdf");
			String testFileName = getFileName(testPDF);
			if (testFileName == null) {
				this.reponseError(response, "", null);
			}
			testFileName = "test_" + testFileName;
			File testFile = new File(tempFolder, testFileName);
			if (!testFile.createNewFile()) {
				this.reponseError(response, "", null);
			}
			testOut = new FileOutputStream(testFile);
			testFilecontent = testPDF.getInputStream();
			copyStream(testFilecontent, testOut);
			testOut.close();

			File reportDir = new File(tempFolder, "report");

			DiffWorker worker = new DiffWorker(baseFile, testFile, reportDir);
			request.getSession().setAttribute("DiffWorker", worker);
			worker.start();

			int diffCount = this.diff(baseFile, testFile, reportDir);

			response.setContentType("text/html");
			response.setHeader("Cache-control", "no-cache, no-store");
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Expires", "-1");

			response.setHeader("Access-Control-Allow-Origin", "*");
			response.setHeader("Access-Control-Allow-Methods", "POST");
			response.setHeader("Access-Control-Allow-Headers", "Content-Type");
			response.setHeader("Access-Control-Max-Age", "86400");

			JSONObject json = new JSONObject();
			json.put("success", true);
			json.put("diffCount", diffCount);
			json.put("tempID", this.tempID);
			json.put("reportUrl", getRelReportUrl(tempFolder));

			responseWriter.println(json.toString());
		} catch (Exception e) {
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
		}*/
	}

	private void reponseError(HttpServletResponse response, String error, Exception e) {

	}

	private static File createTempFolder(String root) {
		String temp = "" + System.currentTimeMillis() + (new Random()).nextLong();
		String path = root + temp + "/";
		File tempFolder = new File(path);
		if (tempFolder.mkdirs()) {
			return tempFolder;
		}
		return null;
	}

	private static String getRelReportUrl(File tempFolder) {
		return "/pdfs/" + tempFolder.getName() + "/report/" + "report.html";

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
				String val = content.substring(content.indexOf('=') + 1).trim();
				val = val.replace("\"", "");
				val = val.replace("\\", "/");
				val = val.substring(val.lastIndexOf('/') + 1, val.length());
				return val;
			}
		}
		return null;
	}

	@Override
	public void destroy() {
		queue.clear();
		map.clear();
		notifierThread.interrupt();
	}

	@Override
	public String getServletInfo() {
		return "Short description";
	}// </editor-fold>


}
