/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributeDefNameInheritance;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributeDefNameInheritanceResponse;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindAttributeDefNames;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindAttributeDefNamesResponse;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAssignAttributeDefNameInheritanceResults;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsFindAttributeDefNamesResults;


/**
 * @author mchyzer
 *
 */
public class WsSampleAssignAttributeDefNameInheritance implements WsSampleGenerated {
    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        assignAttributeDefNameInheritance(WsSampleGeneratedType.soap);
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
      assignAttributeDefNameInheritance(wsSampleGeneratedType);
    }

    /**
     *
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void assignAttributeDefNameInheritance(WsSampleGeneratedType wsSampleGeneratedType) {
        try {
            //URL, e.g. http://localhost:8091/grouper-ws/services/GrouperService
            GrouperServiceStub stub = new GrouperServiceStub(GeneratedClientSettings.URL);
            Options options = stub._getServiceClient().getOptions();
            HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
            auth.setUsername(GeneratedClientSettings.USER);
            auth.setPassword(GeneratedClientSettings.PASS);
            auth.setPreemptiveAuthentication(true);

            options.setProperty(HTTPConstants.AUTHENTICATE, auth);
            options.setProperty(HTTPConstants.SO_TIMEOUT, new Integer(3600000));
            options.setProperty(HTTPConstants.CONNECTION_TIMEOUT,
                new Integer(3600000));

            AssignAttributeDefNameInheritance assignAttributeDefNameInheritance = null;
            AssignAttributeDefNameInheritanceResponse assignAttributeDefNameInheritanceResponse = null;
            WsAssignAttributeDefNameInheritanceResults wsAssignAttributeDefNameInheritanceResults = null;

            assignAttributeDefNameInheritance = AssignAttributeDefNameInheritance.class.newInstance();

            //version, e.g. v1_3_000
            assignAttributeDefNameInheritance.setClientVersion(GeneratedClientSettings.VERSION);
            
            //this is the parent of the relation
            {
              WsAttributeDefNameLookup wsAttributeDefNameLookup = new WsAttributeDefNameLookup();
              wsAttributeDefNameLookup.setName("aStem:permissionDefName");
              assignAttributeDefNameInheritance.setWsAttributeDefNameLookup(wsAttributeDefNameLookup);
            }
            
            //we are doing an assignment
            assignAttributeDefNameInheritance.setAssign("T");
            
            {
              //these are the children of the relation
              WsAttributeDefNameLookup relatedAttributeDefNameLookup = new WsAttributeDefNameLookup();
              relatedAttributeDefNameLookup.setName("aStem:permissionDefName3");
              assignAttributeDefNameInheritance.addRelatedWsAttributeDefNameLookups(relatedAttributeDefNameLookup);
              relatedAttributeDefNameLookup = new WsAttributeDefNameLookup();
              relatedAttributeDefNameLookup.setName("aStem:permissionDefName4");
              assignAttributeDefNameInheritance.addRelatedWsAttributeDefNameLookups(relatedAttributeDefNameLookup);
            }            
            
            assignAttributeDefNameInheritanceResponse = stub.assignAttributeDefNameInheritance(assignAttributeDefNameInheritance);
            wsAssignAttributeDefNameInheritanceResults = assignAttributeDefNameInheritanceResponse.get_return();
            System.out.println(ToStringBuilder.reflectionToString(
                    wsAssignAttributeDefNameInheritanceResults));
            
            if (!StringUtils.equals("T", 
                wsAssignAttributeDefNameInheritanceResults.getResultMetadata().getSuccess())) {
              throw new RuntimeException("didnt get success! ");
            }
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}