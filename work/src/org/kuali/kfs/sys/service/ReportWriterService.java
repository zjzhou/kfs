/*
 * Copyright 2007 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.sys.service;

import java.util.List;

import org.kuali.kfs.sys.Message;
import org.kuali.rice.kns.bo.BusinessObject;

/**
 * Service interface defines methods that are relevant to writing a report
 */
public interface ReportWriterService {
    /**
     * Writes an error message for the passed in business object. If the business object is the first or different then the
     * previous one a table header is printed per definition in the implementation of this service
     * @param businessObject controlling the table header and values to be printed
     * @param message associated with the businessObject
     */
    public void writeError(BusinessObject businessObject, Message message);
    
    /**
     * Same as writeError except that it provides for multiple messages for the BO. BO values are only printed once and then messages each
     * row below that.
     * @param businessObject controlling the table header and values to be printed
     * @param messages associated with the businessObject
     */
    public void writeError(BusinessObject businessObject, List<Message> messages);
    
    /**
     * Writes statistics usually placed at the end of the report. If this is the first time this method is called then a statistics header
     * is written. All messages are indented per STATISTICS_LEFT_PADDING. If multiple lines are needed, call this method multiple times to
     * assure pagination works properly
     * @param message to write
     * @param args for the message per standard String.format
     */
    public void writeStatisticLine(String message, Object ... args);
    
    /**
     * Pass through to PrintStream.printf except that it also handles pagination. If multiple lines are needed, call this method multiple
     * times to assure pagination works properly
     * @param format
     */
    public void writeFormattedMessageLine(String format);
    
    /**
     * Pass through to PrintStream.printf except that it also handles pagination. If multiple lines are needed, call this method multiple
     * times to assure pagination works properly
     * @param format
     * @param args
     */
    public void writeFormattedMessageLine(String format, Object ... args);
}
