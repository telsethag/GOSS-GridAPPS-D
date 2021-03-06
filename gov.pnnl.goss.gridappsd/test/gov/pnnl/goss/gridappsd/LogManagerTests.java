/*******************************************************************************
 * Copyright (c) 2017, Battelle Memorial Institute All rights reserved.
 * Battelle Memorial Institute (hereinafter Battelle) hereby grants permission to any person or entity 
 * lawfully obtaining a copy of this software and associated documentation files (hereinafter the 
 * Software) to redistribute and use the Software in source and binary forms, with or without modification. 
 * Such person or entity may use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of 
 * the Software, and may permit others to do so, subject to the following conditions:
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 * following disclaimers.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and 
 * the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Other than as used herein, neither the name Battelle Memorial Institute or Battelle may be used in any 
 * form whatsoever without the express written consent of Battelle.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL 
 * BATTELLE OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED 
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * General disclaimer for use with OSS licenses
 * 
 * This material was prepared as an account of work sponsored by an agency of the United States Government. 
 * Neither the United States Government nor the United States Department of Energy, nor Battelle, nor any 
 * of their employees, nor any jurisdiction or organization that has cooperated in the development of these 
 * materials, makes any warranty, express or implied, or assumes any legal liability or responsibility for 
 * the accuracy, completeness, or usefulness or any information, apparatus, product, software, or process 
 * disclosed, or represents that its use would not infringe privately owned rights.
 * 
 * Reference herein to any specific commercial product, process, or service by trade name, trademark, manufacturer, 
 * or otherwise does not necessarily constitute or imply its endorsement, recommendation, or favoring by the United 
 * States Government or any agency thereof, or Battelle Memorial Institute. The views and opinions of authors expressed 
 * herein do not necessarily state or reflect those of the United States Government or any agency thereof.
 * 
 * PACIFIC NORTHWEST NATIONAL LABORATORY operated by BATTELLE for the 
 * UNITED STATES DEPARTMENT OF ENERGY under Contract DE-AC05-76RL01830
 ******************************************************************************/
package gov.pnnl.goss.gridappsd;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import gov.pnnl.goss.gridappsd.api.LogDataManager;
import gov.pnnl.goss.gridappsd.api.LogManager;
import gov.pnnl.goss.gridappsd.dto.LogMessage;
import gov.pnnl.goss.gridappsd.dto.LogMessage.LogLevel;
import gov.pnnl.goss.gridappsd.dto.LogMessage.ProcessStatus;
import gov.pnnl.goss.gridappsd.log.LogManagerImpl;
import gov.pnnl.goss.gridappsd.utils.GridAppsDConstants;

@RunWith(MockitoJUnitRunner.class)
public class LogManagerTests {
	
	@Mock
	LogDataManager logDataManager;
	
	@Captor
	ArgumentCaptor<String> argCaptor;
	@Captor
	ArgumentCaptor<Long> argLongCaptor;
	@Captor
	ArgumentCaptor<LogLevel> argLogLevelCaptor;
	@Captor
	ArgumentCaptor<ProcessStatus> argProcessStatusCaptor;
	
	
	@Test
	public void storeCalledWhen_logStoreToDBTrueInObject() throws ParseException{
		
		LogManager logManager = new LogManagerImpl(logDataManager);
		
		LogMessage message = new LogMessage();
		message.setLogLevel(LogLevel.DEBUG);
		message.setLogMessage("Process manager received message "+ message);
		message.setProcessId(this.getClass().getName());
		message.setProcessStatus(ProcessStatus.RUNNING);
		message.setStoreToDb(true);
		message.setTimestamp(GridAppsDConstants.SDF_SIMULATION_REQUEST.parse("11/11/11 11:11:11").getTime());
		
		logManager.log(message,GridAppsDConstants.username);
		
		
		
		Mockito.verify(logDataManager).store(argCaptor.capture(), 
				argLongCaptor.capture(), argCaptor.capture(),
				argLogLevelCaptor.capture(), argProcessStatusCaptor.capture(),argCaptor.capture());
		
		List<String> allStringValues = argCaptor.getAllValues();
		assertEquals(3, allStringValues.size());
		assertEquals(message.getProcessId(), allStringValues.get(0));
		//TODO: User test user for this instead of system
		assertEquals("system", allStringValues.get(2));
		assertEquals(new Long(message.getTimestamp()), argLongCaptor.getValue());
		assertEquals(message.getLogLevel(), argLogLevelCaptor.getValue());
		assertEquals(message.getLogMessage(), allStringValues.get(1));
		assertEquals(message.getProcessStatus(), argProcessStatusCaptor.getValue());
	
	}
	
	@Test
	public void storeCalledWhen_logStoreToDBTrueInString() throws ParseException{
		
		
		LogManager logManager = new LogManagerImpl(logDataManager);
		String message = "{"
				+ "\"processId\":\"app_123\","
				+ "\"processStatus\":\"STARTED\","
				+ "\"logLevel\":\"DEBUG\","
				+ "\"logMessage\":\"Testing LogManager\","
				+ "\"timestamp\": "+GridAppsDConstants.SDF_SIMULATION_REQUEST.parse("8/14/17 2:22:22").getTime()+"}";
		
		logManager.log(LogMessage.parse(message), GridAppsDConstants.username);
		
		Mockito.verify(logDataManager).store(argCaptor.capture(), 
				argLongCaptor.capture(), argCaptor.capture(),
				argLogLevelCaptor.capture(), argProcessStatusCaptor.capture(),argCaptor.capture());
		
		List<String> allStringValues = argCaptor.getAllValues();
		assertEquals(3, allStringValues.size());
		assertEquals("app_123", allStringValues.get(0));
		//TODO: User test user for this instead of system
		assertEquals("system", allStringValues.get(2));
		assertEquals(new Long(GridAppsDConstants.SDF_SIMULATION_REQUEST.parse("8/14/17 2:22:22").getTime()), argLongCaptor.getValue());
		assertEquals(LogLevel.DEBUG, argLogLevelCaptor.getValue());
		assertEquals("Testing LogManager", allStringValues.get(1));
		assertEquals(ProcessStatus.STARTED, argProcessStatusCaptor.getValue());
		

	}
	
	@Test
	public void queryCalledWhen_getLogCalledWithObject() throws ParseException{
		
		LogManager logManager = new LogManagerImpl(logDataManager);
		
		LogMessage message = new LogMessage();
		message.setLogLevel(LogLevel.DEBUG);
		message.setProcessId(this.getClass().getName());
		message.setProcessStatus(ProcessStatus.RUNNING);
		message.setTimestamp(GridAppsDConstants.SDF_SIMULATION_REQUEST.parse("11/11/11 11:11:11").getTime());
		
		String restultTopic = "goss.gridappsd.data.output";
		String logTopic = "goss.gridappsd.data.log";
		
		logManager.get(message, restultTopic, logTopic);
		
		
//		Mockito.verify(logDataManager).query(argCaptor.capture(), argCaptor.capture(),
//				argCaptor.capture(), argCaptor.capture(), argCaptor.capture());
//		
//		List<String> allValues = argCaptor.getAllValues();
//		assertEquals(5, allValues.size());
//		assertEquals(message.getProcess_id(), allValues.get(0));
//		assertEquals(message.getTimestamp(), allValues.get(1));
//		assertEquals(message.getLog_level(), allValues.get(2));
//		assertEquals(message.getProcess_status(), allValues.get(3));
//		//TODO: User test user for this instead of system
//		assertEquals("system", allValues.get(4));
	}
	
	@Test
	public void queryCalledWhen_getLogCalledWithString() throws ParseException{
		
		
		LogManager logManager = new LogManagerImpl(logDataManager);
		String message = "{"
				+ "\"process_id\":\"app_123\","
				+ "\"process_status\":\"started\","
				+ "\"log_level\":\"debug\","
				+ "\"log_message\":\"something happened\","
				+ "\"timestamp\": "+GridAppsDConstants.SDF_SIMULATION_REQUEST.parse("8/14/17 2:22:22").getTime()+"}";
		
		String restultTopic = "goss.gridappsd.data.output";
		String logTopic = "goss.gridappsd.data.log";
		
//		logManager.get(LogMessage.parse(message),restultTopic,logTopic);
		
		
//		Mockito.verify(logDataManager).query(argCaptor.capture(), argCaptor.capture(),
//				argCaptor.capture(), argCaptor.capture(), argCaptor.capture());
//		
//		List<String> allValues = argCaptor.getAllValues();
//		assertEquals(5, allValues.size());
//		assertEquals("app_123", allValues.get(0));
//		assertEquals("8\14\17 2:22:22", allValues.get(1));
//		assertEquals("debug", allValues.get(2));
//		assertEquals("started", allValues.get(3));
//		//TODO: User test user for this instead of system
//		assertEquals("system", allValues.get(4));
				

	}
	
	

}
