package com.pdfhow.diff;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import org.apache.pdfbox.tools.diff.DiffLogger;
import org.apache.pdfbox.tools.diff.PDFDiffTool;

public class DiffWorker extends Thread {

	private File baseFile;
	private File testFile;
	private File reportDir;
	private ProgressedDiffLogger logger;

	public DiffWorker(File baseFile, File testFile, File reportDir, BlockingQueue<String> messageQueue) {
		this.baseFile = baseFile;
		this.testFile = testFile;
		this.reportDir = reportDir;
		this.logger = new ProgressedDiffLogger(messageQueue);
	}

	@Override
	public void run() {
		PDFDiffTool.diff(this.baseFile, this.testFile, this.reportDir, null, this.logger);
	}

	public float getProgress() {
		// return (int) (this.logger.getProgress() + 0.5);
		return this.logger.getProgress();
	}

	class ProgressedDiffLogger extends DiffLogger {

		private BlockingQueue<String> messageQueue;

		public ProgressedDiffLogger(BlockingQueue<String> messageQueue) {
			this.messageQueue = messageQueue;
		}

		@Override
		public void info(String msg) {
			try {
				String cMessage = BEGIN_SCRIPT_TAG + toJsonp("info", msg) + END_SCRIPT_TAG;
				messageQueue.put(cMessage);
			} catch (Exception ex) {
				ex.printStackTrace(System.out);
			}
		}

		@Override
		public void error(String msg) {
		}

		@Override
		public void warn(String msg) {
		}

		@Override
		public void error(Throwable t) {
		}

		private static final String BEGIN_SCRIPT_TAG = "<script type='text/javascript'>\n";

		private static final String END_SCRIPT_TAG = "</script>\n";
		
		private String escape(String orig) {
			StringBuffer buffer = new StringBuffer(orig.length());

			for (int i = 0; i < orig.length(); i++) {
				char c = orig.charAt(i);
				switch (c) {
				case '\b':
					buffer.append("\\b");
					break;
				case '\f':
					buffer.append("\\f");
					break;
				case '\n':
					buffer.append("<br />");
					break;
				case '\r':
					// ignore
					break;
				case '\t':
					buffer.append("\\t");
					break;
				case '\'':
					buffer.append("\\'");
					break;
				case '\"':
					buffer.append("\\\"");
					break;
				case '\\':
					buffer.append("\\\\");
					break;
				case '<':
					buffer.append("&lt;");
					break;
				case '>':
					buffer.append("&gt;");
					break;
				case '&':
					buffer.append("&amp;");
					break;
				default:
					buffer.append(c);
				}
			}

			return buffer.toString();
		}

		private String toJsonp(String name, String message) {
			return "window.parent.app.update({ name: \"" + escape(name) + "\", message: \"" + escape(message) + "\" });\n";
		}
	}
}
