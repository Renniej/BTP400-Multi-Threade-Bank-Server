package com.btp400.packets;

import java.io.Serializable;

public class ResponsePacket implements Serializable{

	private int m_code;
	private String m_message;
	
	
	public ResponsePacket(int code, String message) {
		// TODO Auto-generated constructor stub
		
		m_code = code;
		m_message = message;
		
		
	}
	
	
	public String getMessage() {
		return m_message;
	}

	
	public int getCode() {
		return m_code;
	}
}
