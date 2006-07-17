/**
 * Copyright (c) 2005-2006 JavaGameNetworking
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'JavaGameNetworking' nor the names of its contributors 
 *   may be used to endorse or promote products derived from this software 
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Created: Jul 13, 2006
 */
package com.captiveimagination.jgn.ro;

import java.io.*;
import java.lang.reflect.*;

import com.captiveimagination.jgn.*;
import com.captiveimagination.jgn.event.*;
import com.captiveimagination.jgn.message.*;

/**
 * @author Matthew D. Hicks
 */
public class RemoteInvocationListener extends MessageAdapter {
	private Class<? extends RemoteObject> remoteClass;
	private RemoteObject object;
	private MessageServer server;
	
	protected RemoteInvocationListener(Class<? extends RemoteObject> remoteClass, RemoteObject object, MessageServer server) {
		this.remoteClass = remoteClass;
		this.object = object;
		this.server = server;
		server.addMessageListener(this);
	}
	
	public void messageReceived(Message message) {
		if (message instanceof RemoteObjectRequest) {
			RemoteObjectRequest m = (RemoteObjectRequest)message;
			if (m.getRemoteObjectName().equals(remoteClass.getName())) {
				RemoteObjectResponse response = new RemoteObjectResponse();
				response.setMethodName(m.getMethodName());
				response.setRemoteObjectName(m.getRemoteObjectName());
				try {
					Class[] classes;
					if (m.getParameters() != null) {
						classes = new Class[m.getParameters().length];
						for (int i = 0; i < m.getParameters().length; i++) {
							if (m.getParameters()[i] != null) {
								classes[i] = m.getParameters()[i].getClass();
							}
						}
					} else {
						classes = new Class[0];
					}
					Method method = remoteClass.getMethod(m.getMethodName(), classes);
					method.setAccessible(true);
					Object obj = method.invoke(object, m.getParameters());
					response.setResponse((Serializable)obj);
				} catch(Exception exc) {
					exc.printStackTrace();
					response.setResponse(exc);
				}
				m.getMessageClient().sendMessage(response);
			}
		}
	}
	
	public void close() {
		server.removeMessageListener(this);
	}
}