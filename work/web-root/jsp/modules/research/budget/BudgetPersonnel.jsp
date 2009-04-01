<%--
 Copyright 2006-2007 The Kuali Foundation.
 
 Licensed under the Educational Community License, Version 1.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.opensource.org/licenses/ecl1.php
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
--%>
<!-- BEGIN budgetPersonnel.jsp -->
<%@ include file="/jsp/kfs/kfsTldHeader.jsp"%>

<c:set var="viewOnly" value="${!KualiForm.documentActions[Constants.KUALI_ACTION_CAN_EDIT]}" />

<kul:documentPage showDocumentInfo="true"
	documentTypeName="BudgetDocument"
	htmlFormAction="researchBudgetPersonnel" showTabButtons="true"
	headerDispatch="savePersonnel" headerTabActive="personnel" renderMultipart="true"
	auditCount="${AuditErrors['personnelAuditErrors'].size}">

	<kul:errors keyMatch="${Constants.DOCUMENT_ERRORS}" />
	
	<div align="right">
		<kul:help documentTypeName="${DataDictionary.BudgetDocument.documentTypeName}" pageName="${CGConstants.PERSONNEL_HEADER_TAB}" altText="page help"/>
	</div>

  <c:if test="${! viewOnly}">
    <cg:budgetPersonnelAdd />
  </c:if>
	<cg:budgetPersonnel />

  <c:if test="${! viewOnly}">
  	<c:set var="extraButtonSource" value="${ConfigProperties.externalizable.images.url}buttonsmall_deletesel.gif"/>
  	<c:set var="extraButtonProperty" value="methodToCall.deletePersonnel"/>
  	<c:set var="extraButtonAlt" value="delete"/>
  </c:if>  
  
  	<p>
	<sys:documentControls 
		transactionalDocument="false" 
		saveButtonOverride="savePersonnel" 
		suppressRoutingControls="true"
		extraButtonSource="${extraButtonSource}"
		extraButtonProperty="${extraButtonProperty}"
		extraButtonAlt="${extraButtonAlt}"
		viewOnly="${viewOnly}"
		/>
	</p>


</kul:documentPage>
<!-- END budgetPersonnel.jsp -->
