package com.pdfhow.diff;

public class ScriptMessage {

	private static final String BEGIN_SCRIPT_TAG = "<script type='text/javascript'>\n";

	private static final String END_SCRIPT_TAG = "</script>\n";

	private String sessionID;
	private String scriptMessage;
	private SCRIPT_METHOD method;

	public enum SCRIPT_METHOD {
		update, timeout
	};

	public ScriptMessage(String sessionID, String message, SCRIPT_METHOD method) {
		this.sessionID = sessionID;
		this.method = method;
		this.scriptMessage = this.createScript(method, message);
	}

	public String getSessionID() {
		return this.sessionID;
	}

	public SCRIPT_METHOD getMethod() {
		return this.method;
	}
	
	public String getScriptMessage() {
		return this.scriptMessage;
	}

	public String createScript(SCRIPT_METHOD method, String msg) {
		String cMessage = BEGIN_SCRIPT_TAG + toJsonp("info", msg, method) + END_SCRIPT_TAG;
		return cMessage;
	}

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

	private String toJsonp(String name, String message, SCRIPT_METHOD method) {
		if (method == SCRIPT_METHOD.update) {
			return "window.parent.app.update({ name: \"" + escape(name) + "\", message: \"" + escape(message)
					+ "\" });\n";
		} else if (method == SCRIPT_METHOD.timeout) {
			return "window.parent.app.timeout({ name: \"" + escape(name) + "\", message: \"" + escape(message)
					+ "\" });\n";
		}
		return "";
	}
}
