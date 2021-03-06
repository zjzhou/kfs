<%--
   - The Kuali Financial System, a comprehensive financial management system for higher education.
   - 
   - Copyright 2005-2014 The Kuali Foundation
   - 
   - This program is free software: you can redistribute it and/or modify
   - it under the terms of the GNU Affero General Public License as
   - published by the Free Software Foundation, either version 3 of the
   - License, or (at your option) any later version.
   - 
   - This program is distributed in the hope that it will be useful,
   - but WITHOUT ANY WARRANTY; without even the implied warranty of
   - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   - GNU Affero General Public License for more details.
   - 
   - You should have received a copy of the GNU Affero General Public License
   - along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>
<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>
<%@ taglib uri="/WEB-INF/tlds/temfunc.tld" prefix="temfunc"%>
<%@ attribute name="detail" required="false" description="The expense detail to create the form for." %>
<%@ attribute name="lineNumber" required="true" description="Line number for the record." %>
<%@ attribute name="detailLineNumber" required="false" description="Detail line number" %>

<%@ attribute name="detailObject" required="true" description="The actual object" type="org.kuali.kfs.module.tem.businessobject.ImportedExpense"%>
<%@ attribute name="parentObject" required="true" description="The actual object" type="org.kuali.kfs.module.tem.businessobject.ImportedExpense"%>

<c:set var="importedExpenseAttributes" value="${DataDictionary.ImportedExpense.attributes}" />
<jsp:useBean id="paramMap" class="java.util.HashMap" />
<c:set target="${paramMap}" property="tripType" value="${KualiForm.document.tripTypeCode}" />
<c:set target="${paramMap}" property="travelerType" value="${KualiForm.document.traveler.travelerTypeCode}" />
<c:set target="${paramMap}" property="documentType" value="${KualiForm.docTypeName}" />

								<tr>
									<c:choose>
										<c:when test="${detailLineNumber == null }">
											<th scope="row" class="infoline" rowspan="2">
												<div align="right">add:</div>
											</th>
										</c:when>
										<c:otherwise>
											<th scope="row" class="infoline" rowspan="2">
												<div align="center"> 
													${detailLineNumber+1}
												</div>
											</th>
										</c:otherwise>
									</c:choose>
									<td valign="top" class="infoline">
										<kul:htmlControlAttribute
											attributeEntry="${importedExpenseAttributes.expenseDate}"
											property="${detail}.expenseDate"
											readOnly="${!fullEntryMode}" />
									</td>
									<td valign="top" class="infoline">
										<c:out value="${detailObject.expenseTypeObjectCode.expenseType.name}" />
									</td>
									<td valign="top" nowrap class="infoline">
										<div align="center">
											<kul:htmlControlAttribute
												attributeEntry="${importedExpenseAttributes.expenseAmount}"
												property="${detail}.expenseAmount" 
												readOnly="${!fullEntryMode}" />
										</div>
									</td>
						            <td valign="top" nowrap class="infoline">
										<div align="center">
											<kul:htmlControlAttribute
												attributeEntry="${importedExpenseAttributes.convertedAmount}"
												property="${detail}.convertedAmount" 
												readOnly="true" />
										</div>
									</td>
						            <td valign="top" nowrap class="infoline">
										<div align="center">
											<kul:htmlControlAttribute
												attributeEntry="${importedExpenseAttributes.nonReimbursable}"
												property="${detail}.nonReimbursable"
												readOnly="${!fullEntryMode || !detailObject.enableNonReimbursable || detailObject.nonReimbursable}" />
						            	</div>
						            </td>
						            <td valign="top" nowrap class="infoline">
										<div align="center">
											<kul:htmlControlAttribute
												attributeEntry="${importedExpenseAttributes.taxable}"
												property="${detail}.taxable"
												readOnly="${!fullEntryMode || !expenseTaxableMode}" />
						            	</div>
						            </td>
									<td class="infoline" rowspan="2">
										<c:choose>
											<c:when test="${detailLineNumber != null}">
												<c:if test="${fullEntryMode}">
													<div align="center">
														<html:image
															src="${ConfigProperties.kr.externalizable.images.url}tinybutton-delete1.gif"
															styleClass="tinybutton"
															property="methodToCall.deleteImportedExpenseDetailLine.line${lineNumber}-${detailLineNumber}"
															alt="Delete Imported Expense Line"
															title="Delete Imported Expense Detail Line" /></div>
												</c:if>
												<c:if test="${!fullEntryMode}">
													&nbsp;
												</c:if>
											</c:when>
											<c:otherwise>
												<c:set var="tabindex" value="${KualiForm.currentTabIndex}"/>
												<c:set var="dummyIncrementVar" value="${kfunc:incrementTabIndex(KualiForm, tabKey)}" />
												<div align="center">
													<html:image src="${ConfigProperties.kr.externalizable.images.url}tinybutton-add1.gif"
														styleClass="tinybutton" 
														tabindex="${tabindex}" 
														property="methodToCall.addImportedExpenseDetailLine.line${lineNumber}"
														alt="Add Imported Expense Detail Line" 
														title="Add Imported Expense Detail Line" />
												</div>
											</c:otherwise>
										</c:choose>
					
										
									</td>
								</tr>
								<tr>
									<th>
										<div align="right">
											<kul:htmlAttributeLabel attributeEntry="${importedExpenseAttributes.description}" noColon="true" />
										</div>
									</th>
									<td valign="top" class="infoline" colspan="5">
										<kul:htmlControlAttribute
											attributeEntry="${importedExpenseAttributes.description}"
											property="${detail}.description" 
											readOnly="${!fullEntryMode}" />
									</td>
								</tr>
