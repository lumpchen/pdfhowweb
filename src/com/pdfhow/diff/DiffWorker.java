package com.pdfhow.diff;

import java.io.File;
import java.util.concurrent.BlockingQueue;

import org.apache.pdfbox.tools.diff.DiffLogger;
import org.apache.pdfbox.tools.diff.PDFDiffTool;

public class DiffWorker extends Thread {

	private File baseFile;
	private File testFile;
	private File reportDir;
	private ProgressedDiffLogger logger;
	private String sessionID;

	public DiffWorker(File baseFile, File testFile, File reportDir, BlockingQueue<ScriptMessage> messageQueue,
			String sessionID) {
		this.baseFile = baseFile;
		this.testFile = testFile;
		this.reportDir = reportDir;
		this.logger = new ProgressedDiffLogger(messageQueue);
		this.sessionID = sessionID;
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

		private BlockingQueue<ScriptMessage> messageQueue;

		public ProgressedDiffLogger(BlockingQueue<ScriptMessage> messageQueue) {
			this.messageQueue = messageQueue;
		}

		@Override
		public void info(String msg) {
			try {
				System.out.println(msg);
				messageQueue.put(new ScriptMessage(sessionID, msg, ScriptMessage.SCRIPT_METHOD.update));
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
	}
}
