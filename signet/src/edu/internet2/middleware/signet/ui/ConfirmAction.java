/*--
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/ui/ConfirmAction.java,v 1.27 2007-07-31 09:22:08 ddonn Exp $

Copyright 2006 Internet2, Stanford University

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package edu.internet2.middleware.signet.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.hibernate.Session;
import org.hibernate.Transaction;
import edu.internet2.middleware.signet.Assignment;
import edu.internet2.middleware.signet.DateOnly;
import edu.internet2.middleware.signet.DateRangeValidator;
import edu.internet2.middleware.signet.Function;
import edu.internet2.middleware.signet.History;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.SignetAuthorityException;
import edu.internet2.middleware.signet.dbpersist.HibernateDB;
import edu.internet2.middleware.signet.subjsrc.SignetSubject;
import edu.internet2.middleware.signet.tree.TreeNode;

/**
* <p>
* Confirm required resources are available. If a resource is missing,
* forward to "failure". Otherwise, forward to "success", where
* success is usually the "welcome" page.
* </p>
* <p>
* Since "required resources" includes the application MessageResources
* the failure page must not use the standard error or message tags.
* Instead, it display the Strings stored in an ArrayList stored under
* the request attribute "ERROR".
* </p>
*
*/
public final class ConfirmAction extends BaseAction
{

  /**
   * This method expects to find the following attributes in the Session:
   * 
   *   Name: "signet"
   *   Type: Signet
   *   Use:  A handle to the current Signet environment.
   * 
   * This method expects to receive the following HTTP parameters:
   * 
   *   Name: ASSIGNMENT_HTTPPARAMNAME
   *   Use:  The String representation of a Signet Assignment's ID.
   * 
   * This method updates the followiing attributes in the Session:
   * 
   *   Name: Constants.ASSIGNMENT_ATTRNAME
   *   Type: Assignment
   *   Use:  The Assignment which is currently being examined or edited by
   *         the Signet user.
   */
public ActionForward execute
  (ActionMapping        mapping,
   ActionForm           form,
   HttpServletRequest   request,
   HttpServletResponse response)
throws Exception
{
  // Confirm message resources loaded
  if (null == getResources(request))
  {
	// Setup message array in case there are errors
	ArrayList messages = new ArrayList();
    messages.add(Constants.ERROR_MESSAGES_NOT_LOADED);
    request.setAttribute(Constants.ERROR_KEY,messages);
    return findFailure(mapping);
  }

  HttpSession session = request.getSession(); 

  Signet signet = (Signet)(session.getAttribute(Constants.SIGNET_ATTRNAME));
  if (signet == null)
  {
    return (mapping.findForward("notInitialized"));
  }

// debug
//Common.dumpHttpParams("ConfirmAction.execute()", signet.getLogger(), request);

	// currentAssignment is present in the session only if we are editing
	// an existing Assignment. Otherwise, we're attempting to create a new one.
	Assignment assignment = (Assignment)(session.getAttribute(Constants.ASSIGNMENT_ATTRNAME));

	ActionMessages actionMsgs = new ActionMessages();

	Date defaultEffectiveDate = Common.getDefaultEffectiveDate(assignment);
	Date effectiveDate = Common.getDateParam(request, Constants.EFFECTIVE_DATE_PREFIX,
			defaultEffectiveDate, actionMsgs);
	Date expirationDate = Common.getDateParam(request, Constants.EXPIRATION_DATE_PREFIX,
			null, actionMsgs);

    DateRangeValidator drv;
	if (null == assignment)
		drv = new DateRangeValidator(new DateOnly(), effectiveDate, expirationDate);
	else
		drv = new DateRangeValidator(effectiveDate, expirationDate);
	if ( !drv.getStatus())
	{
		ActionMessage msg = new ActionMessage(drv.getErrMsgKey());
		actionMsgs.add(drv.getErrField(), msg);
	}

	// If we've detected any data-entry errors, bail out now
	if ( !actionMsgs.isEmpty())
	{
		saveMessages(request, actionMsgs);
		return findDataEntryErrors(mapping);
	}
	actionMsgs = null;

	Set limitValues = LimitRenderer.getAllLimitValues(signet, request);

	SignetSubject grantor = (SignetSubject)session.getAttribute(Constants.LOGGEDINUSER_ATTRNAME);
	boolean canGrant = Common.paramIsPresent(request.getParameter(Constants.CAN_GRANT_HTTPPARAMNAME));
	boolean canUse = Common.paramIsPresent(request.getParameter(Constants.CAN_USE_HTTPPARAMNAME));

	SignetSubject grantee = (SignetSubject)session.getAttribute(Constants.CURRENTPSUBJECT_ATTRNAME);

	// Creating a new Assignment or editing an existing one?
	if (assignment == null)
	{
		TreeNode scope = (TreeNode)(session.getAttribute(Constants.SCOPE_ATTRNAME));
		Function function = (Function)(session.getAttribute(Constants.FUNCTION_ATTRNAME));

		assignment = createAssignment(grantor, grantee, scope, function, limitValues,
				canGrant, canUse, effectiveDate, expirationDate);
	}
	else
		editAssignment(assignment, grantor, limitValues, canGrant, canUse, effectiveDate, expirationDate);

  // Let's see whether or not the Assignment we want to save has any
  // duplicates. If it does, we'll sidetrack the user with a warning
  // screen.

  Set duplicateAssignments = assignment.findDuplicates();
  if ( !duplicateAssignments.isEmpty())
  {
    session.setAttribute(Constants.ASSIGNMENT_ATTRNAME, assignment);
    session.setAttribute(Constants.LIMITVALUE_ATTRNAME, limitValues);
    session.setAttribute(Constants.DUP_ASSIGNMENTS_ATTRNAME, duplicateAssignments);
    return findDuplicateAssignments(mapping);
  }

	// Persist the Assignment

	HibernateDB hibr = signet.getPersistentDB();
	Session hs = hibr.openSession();
	Transaction tx = hs.beginTransaction();

	try
	{
		hibr.save(hs, assignment.getGrantor());
		hibr.save(hs, assignment.getGrantee());
		hibr.save(hs, assignment.getRevoker());
		hibr.save(hs, assignment.getProxy());
		hibr.save(hs, assignment);

		tx.commit();

		hs.refresh(assignment);
		hs.refresh(assignment.getGrantor());
		SignetSubject tmpSubject;
		if (null != (tmpSubject = assignment.getProxy()))
			hs.refresh(tmpSubject);
		if (null != (tmpSubject = assignment.getRevoker()))
			hs.refresh(tmpSubject);
		hs.refresh(grantor);
		hs.refresh(grantee);

		hibr.closeSession(hs);
	}
	catch (Exception e)
	{
		log.error("ConfirmAction.execute: Problem saving Assignment, Id=" + assignment.getId() + ". The exception was: " + e.toString());
		tx.rollback();
		hibr.closeSession(hs);
		return findFailure(mapping);
	}

	// return the new/edited Assignment to caller
	session.setAttribute(Constants.ASSIGNMENT_ATTRNAME, assignment);

	// Forward to our success page
	return findSuccess(mapping);
}


	/**
	 * Edit an existing Assignment
	 * @param assignment
	 * @param grantor
	 * @param limitValues
	 * @param canGrant
	 * @param canUse
	 * @param effectiveDate
	 * @param expirationDate
	 */
	protected void editAssignment(Assignment assignment, SignetSubject grantor,
			Set limitValues, boolean canGrant, boolean canUse,
			Date effectiveDate, Date expirationDate)
		throws SignetAuthorityException
	{
		// We're editing an existing Assignment.
		assignment.checkEditAuthority(grantor); // throws exception, bails out on error
		assignment.setLimitValues(grantor, limitValues, false);
		assignment.setCanGrant(grantor, canGrant, false);
		assignment.setCanUse(grantor, canUse, false);
		assignment.setEffectiveDate(grantor, effectiveDate, false);
		assignment.setExpirationDate(grantor, expirationDate, false);
		assignment.evaluate();
		History histRecord = assignment.createHistoryRecord();
		assignment.addHistoryRecord(histRecord);
	}

	/**
	 * Create a new Assignment
	 * @param grantor
	 * @param grantee
	 * @param scope
	 * @param function
	 * @param limitValues
	 * @param canGrant
	 * @param canUse
	 * @param effectiveDate
	 * @param expirationDate
	 * @return
	 */
	protected Assignment createAssignment(SignetSubject grantor, SignetSubject grantee,
			TreeNode scope, Function function, Set limitValues,
			boolean canGrant, boolean canUse,
			Date effectiveDate, Date expirationDate)
		throws SignetAuthorityException
	{
		Assignment assignment = grantor.grant(grantee, scope, function, limitValues,
           canUse, canGrant, effectiveDate, expirationDate);
		return (assignment);
	}


}