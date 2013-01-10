package org.collectionspace.services.batch.nuxeo;

import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;
import org.collectionspace.services.batch.AbstractBatchInvocable;
import org.collectionspace.services.client.AbstractCommonListUtils;
import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.MovementClient;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.dom4j.DocumentException;
import org.jboss.resteasy.specimpl.UriInfoImpl;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateObjectLocationBatchJob extends AbstractBatchInvocable {

    // FIXME: Where appropriate, get from existing constants rather than local declarations
    private final static String CSID_ELEMENT_NAME = "csid";
    private final static String CURRENT_LOCATION_ELEMENT_NAME = "currentLocation";
    private final static String LIFECYCLE_STATE_ELEMENT_NAME = "currentLifeCycleState";
    private final static String LOCATION_DATE_ELEMENT_NAME = "locationDate";
    private final static String OBJECT_NUMBER_ELEMENT_NAME = "objectNumber";
    private final static String WORKFLOW_COMMON_SCHEMA_NAME = "workflow_common";
    private final static String WORKFLOW_COMMON_NAMESPACE_PREFIX = "ns2";
    private final static String WORKFLOW_COMMON_NAMESPACE_URI =
            "http://collectionspace.org/services/workflow";
    private final static Namespace WORKFLOW_COMMON_NAMESPACE =
            Namespace.getNamespace(
            WORKFLOW_COMMON_NAMESPACE_PREFIX,
            WORKFLOW_COMMON_NAMESPACE_URI);
    private final static String COLLECTIONOBJECTS_COMMON_SCHEMA_NAME = "collectionobjects_common";
    private final static String COLLECTIONOBJECTS_COMMON_NAMESPACE_PREFIX = "ns2";
    private final static String COLLECTIONOBJECTS_COMMON_NAMESPACE_URI =
            "http://collectionspace.org/services/collectionobject";
    private final static Namespace COLLECTIONOBJECTS_COMMON_NAMESPACE =
            Namespace.getNamespace(
            COLLECTIONOBJECTS_COMMON_NAMESPACE_PREFIX,
            COLLECTIONOBJECTS_COMMON_NAMESPACE_URI);
    private final static String NONDELETED_QUERY_COMPONENT =
            "&" + WorkflowClient.WORKFLOW_QUERY_NONDELETED + "=false";
    private final String CLASSNAME = this.getClass().getSimpleName();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // Initialization tasks
    public UpdateObjectLocationBatchJob() {
        setSupportedInvocationModes(Arrays.asList(INVOCATION_MODE_SINGLE, INVOCATION_MODE_LIST));
    }

    /**
     * The main work logic of the batch job. Will be called after setContext.
     */
    @Override
    public void run() {

        setCompletionStatus(STATUS_MIN_PROGRESS);

        try {

            List<String> csids = new ArrayList<String>();

            // Build a list of CollectionObject records to process via this
            // batch job, depending on the invocation mode requested.
            if (requestIsForInvocationModeSingle()) {
                String singleCsid = getInvocationContext().getSingleCSID();
                if (Tools.isBlank(singleCsid)) {
                    throw new Exception(CSID_VALUES_NOT_PROVIDED_IN_INVOCATION_CONTEXT);
                } else {
                    csids.add(singleCsid);
                }
            } else if (requestIsForInvocationModeList()) {
                List<String> listCsids = getListCsids();
                if (listCsids.isEmpty()) {
                    throw new Exception(CSID_VALUES_NOT_PROVIDED_IN_INVOCATION_CONTEXT);
                }
                csids.addAll(listCsids);
            } else if (requestIsForInvocationModeGroup()) {
                // Currently not supported
                // FIXME: Get individual CSIDs, if any, from the group
                // String groupCsid = getInvocationContext().getGroupCSID();
                // ...
            } else if (requestIsForInvocationModeNoContext()) {
                // Currently not supported
                // FIXME: Add code to invoke batch job on every active CollectionObject
            }

            // Update the value of the computed current location field for each CollectionObject
            setResults(updateComputedCurrentLocations(csids));
            setCompletionStatus(STATUS_COMPLETE);

        } catch (Exception e) {
            String errMsg = "Error encountered in " + CLASSNAME + ": " + e.getLocalizedMessage();
            setErrorResult(errMsg);
        }

    }

    private InvocationResults updateComputedCurrentLocations(List<String> csids) {

        ResourceMap resourcemap = getResourceMap();
        ResourceBase collectionObjectResource = resourcemap.get(CollectionObjectClient.SERVICE_NAME);
        ResourceBase movementResource = resourcemap.get(MovementClient.SERVICE_NAME);
        String computedCurrentLocation;
        PoxPayloadOut payloadOut;
        int numAffected = 0;

        try {

            // For each CollectionObject record
            for (String collectionObjectCsid : csids) {

                // Skip over soft-deleted CollectionObject records
                if (isRecordDeleted(collectionObjectResource, collectionObjectCsid)) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Skipping soft-deleted CollectionObject record with CSID " + collectionObjectCsid);
                    }
                    continue;
                }

                // Get the Movement records related to this record
                AbstractCommonList relatedMovements = getRelatedMovements(movementResource, collectionObjectCsid);

                // Skip over CollectionObject records that have no related Movement records
                if (relatedMovements.getListItem().isEmpty()) {
                    continue;
                }

                // Based on data in its related Movement records, compute the
                // current location of this CollectionObject
                computedCurrentLocation = computeCurrentLocation(relatedMovements);

                // Skip over CollectionObject records where no computed current
                // location value can be obtained from related Movement records.
                //
                // FIXME: Clarify: it ever necessary to 'unset' a computed
                // current location value, by setting it to a null or empty value,
                // if that value is no longer obtainable from related Movement records?
                if (Tools.isBlank(computedCurrentLocation)) {
                    continue;
                }

                // Update the value of the computed current location field
                // in the CollectionObject record
                numAffected = updateComputedCurrentLocationValue(collectionObjectResource,
                        collectionObjectCsid, computedCurrentLocation, resourcemap, numAffected);
            }

        } catch (Exception e) {
            String errMsg = "Error encountered in " + CLASSNAME + ": " + e.getLocalizedMessage() + " ";
            errMsg = errMsg + "Successfully updated " + numAffected + " CollectionObject record(s) prior to error.";
            logger.error(errMsg);
            setErrorResult(errMsg);
            getResults().setNumAffected(numAffected);
            return getResults();
        }

        logger.info("Updated computedCurrentLocation values in " + numAffected + " CollectionObject record(s).");
        getResults().setNumAffected(numAffected);
        return getResults();
    }

    private AbstractCommonList getRelatedMovements(ResourceBase movementResource, String csid)
            throws URISyntaxException, DocumentException {

        // Get movement records related to a record, specified by its CSID,
        // where the record is the object of the relation
        // FIXME: Get query string(s) from constant(s), where appropriate
        String queryString = "rtObj=" + csid + NONDELETED_QUERY_COMPONENT;
        UriInfo uriInfo = createRelatedRecordsUriInfo(queryString);
        AbstractCommonList relatedMovements = movementResource.getList(uriInfo);
        if (logger.isTraceEnabled()) {
            logger.trace("Identified " + relatedMovements.getTotalItems()
                    + " Movement record(s) related to the object CollectionObject record " + csid);
        }

        // Get movement records related to a record, specified by its CSID,
        // where the record is the subject of the relation
        // FIXME: Get query string(s) from constant(s), where appropriate
        queryString = "rtSbj=" + csid + NONDELETED_QUERY_COMPONENT;
        uriInfo = createRelatedRecordsUriInfo(queryString);
        AbstractCommonList reverseRelatedMovements = movementResource.getList(uriInfo);
        if (logger.isTraceEnabled()) {
            logger.trace("Identified " + reverseRelatedMovements.getTotalItems()
                    + " Movement record(s) related to the subject CollectionObject record " + csid);
        }

        // If the second list contains any related movement records,
        // merge it into the first list
        if (reverseRelatedMovements.getListItem().size() > 0) {
            relatedMovements.getListItem().addAll(reverseRelatedMovements.getListItem());
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Identified a total of " + relatedMovements.getListItem().size()
                    + " Movement record(s) related to the subject CollectionObject record " + csid);
        }

        return relatedMovements;
    }

    private String computeCurrentLocation(AbstractCommonList relatedMovements) {
        String computedCurrentLocation;
        String movementCsid;
        Set<String> alreadyProcessedMovementCsids = new HashSet<String>();
        computedCurrentLocation = "";
        String currentLocation;
        String locationDate;
        String mostRecentLocationDate = "";
        for (AbstractCommonList.ListItem movementRecord : relatedMovements.getListItem()) {
            movementCsid = AbstractCommonListUtils.ListItemGetElementValue(movementRecord, CSID_ELEMENT_NAME);
            if (Tools.isBlank(movementCsid)) {
                continue;
            }
            // Avoid processing any related Movement record more than once,
            // regardless of the directionality of its relation(s) to this
            // CollectionObject record.
            if (alreadyProcessedMovementCsids.contains(movementCsid)) {
                continue;
            } else {
                alreadyProcessedMovementCsids.add(movementCsid);
            }
            locationDate = AbstractCommonListUtils.ListItemGetElementValue(movementRecord, LOCATION_DATE_ELEMENT_NAME);
            if (Tools.isBlank(locationDate)) {
                continue;
            }
            currentLocation = AbstractCommonListUtils.ListItemGetElementValue(movementRecord, CURRENT_LOCATION_ELEMENT_NAME);
            if (Tools.isBlank(currentLocation)) {
                continue;
            }
            if (logger.isTraceEnabled()) {
                logger.trace("Location date value = " + locationDate);
                logger.trace("Current location value = " + currentLocation);
            }
            // If this record's location date value is more recent than that of other
            // Movement records processed so far, set the computed current location
            // to its current location value.
            //
            // Assumes that all values for this element/field will be consistent ISO 8601
            // date/time representations, each of which can be ordered via string comparison.
            //
            // If this is *not* the case, we can instead parse and convert these values
            // to date/time objects.
            if (locationDate.compareTo(mostRecentLocationDate) > 0) {
                mostRecentLocationDate = locationDate;
                // FIXME: Add optional validation here that the currentLocation value
                // parses successfully as an item refName. (We might make that validation
                // dependent on the value of a parameter passed in during batch job invocation.)
                computedCurrentLocation = currentLocation;
            }

        }
        return computedCurrentLocation;
    }

    private int updateComputedCurrentLocationValue(ResourceBase collectionObjectResource,
            String collectionObjectCsid, String computedCurrentLocation, ResourceMap resourcemap, int numAffected)
            throws DocumentException, URISyntaxException {
        PoxPayloadOut collectionObjectPayload;
        String objectNumber;

        collectionObjectPayload = findByCsid(collectionObjectResource, collectionObjectCsid);
        if (Tools.notBlank(collectionObjectPayload.toXML())) {
            if (logger.isTraceEnabled()) {
                logger.trace("Payload: " + "\n" + collectionObjectPayload);
            }
            objectNumber = getFieldElementValue(collectionObjectPayload,
                    COLLECTIONOBJECTS_COMMON_SCHEMA_NAME, COLLECTIONOBJECTS_COMMON_NAMESPACE,
                    OBJECT_NUMBER_ELEMENT_NAME);
            if (logger.isTraceEnabled()) {
                logger.trace("Object number: " + objectNumber);
            }
            if (Tools.notBlank(objectNumber)) {
                String collectionObjectUpdatePayload =
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                        + "<document name=\"collectionobject\">"
                        + "  <ns2:collectionobjects_common "
                        + "      xmlns:ns2=\"http://collectionspace.org/services/collectionobject\">"
                        + "    <objectNumber>" + objectNumber + "</objectNumber>"
                        + "    <computedCurrentLocation>" + computedCurrentLocation + "</computedCurrentLocation>"
                        + "  </ns2:collectionobjects_common>"
                        + "</document>";
                if (logger.isTraceEnabled()) {
                    logger.trace("Update payload: " + "\n" + collectionObjectUpdatePayload);
                }
                byte[] response = collectionObjectResource.update(resourcemap, null, collectionObjectCsid,
                        collectionObjectUpdatePayload);
                numAffected++;
                if (logger.isTraceEnabled()) {
                    logger.trace("Computed current location value for CollectionObject " + collectionObjectCsid
                            + " was set to " + computedCurrentLocation);
                }
            }

        }
        return numAffected;
    }

    // #################################################################
    // Ray's convenience methods from his AbstractBatchJob class for the
    // UC Berkeley Botanical Garden v2.4 implementation.
    // #################################################################
    protected PoxPayloadOut findByCsid(String serviceName, String csid) throws URISyntaxException, DocumentException {
        ResourceBase resource = getResourceMap().get(serviceName);
        return findByCsid(resource, csid);
    }

    protected PoxPayloadOut findByCsid(ResourceBase resource, String csid) throws URISyntaxException, DocumentException {
        byte[] response = resource.get(null, createUriInfo(), csid);
        PoxPayloadOut payload = new PoxPayloadOut(response);
        return payload;
    }

    protected UriInfo createUriInfo() throws URISyntaxException {
        return createUriInfo("");
    }

    protected UriInfo createUriInfo(String queryString) throws URISyntaxException {
        URI absolutePath = new URI("");
        URI baseUri = new URI("");
        return new UriInfoImpl(absolutePath, baseUri, "", queryString, Collections.<PathSegment>emptyList());
    }

    // #################################################################
    // Other convenience methods
    // #################################################################
    protected UriInfo createRelatedRecordsUriInfo(String query) throws URISyntaxException {
        URI uri = new URI(null, null, null, query, null);
        return createUriInfo(uri.getRawQuery());
    }

    protected String getFieldElementValue(PoxPayloadOut payload, String partLabel, Namespace partNamespace, String fieldPath) {
        String value = null;
        SAXBuilder builder = new SAXBuilder();
        try {
            Document document = builder.build(new StringReader(payload.toXML()));
            Element root = document.getRootElement();
            // The part element is always expected to have an explicit namespace.
            Element part = root.getChild(partLabel, partNamespace);
            // Try getting the field element both with and without a namespace.
            // Even though a field element that lacks a namespace prefix
            // may yet inherit its namespace from a parent, JDOM may require that
            // the getChild() call be made without a namespace.
            Element field = part.getChild(fieldPath, partNamespace);
            if (field == null) {
                field = part.getChild(fieldPath);
            }
            if (field != null) {
                value = field.getText();
            }
        } catch (Exception e) {
            logger.error("Error getting value from field path " + fieldPath
                    + " in schema part " + partLabel);
            return null;
        }
        return value;
    }

    private boolean isRecordDeleted(ResourceBase resource, String collectionObjectCsid) throws URISyntaxException, DocumentException {
        boolean isDeleted = false;
        byte[] workflowResponse = resource.getWorkflow(createUriInfo(), collectionObjectCsid);
        if (workflowResponse != null) {
            PoxPayloadOut payloadOut = new PoxPayloadOut(workflowResponse);
            String workflowState =
                    getFieldElementValue(payloadOut, WORKFLOW_COMMON_SCHEMA_NAME,
                    WORKFLOW_COMMON_NAMESPACE, LIFECYCLE_STATE_ELEMENT_NAME);
            if (Tools.notBlank(workflowState) && workflowState.contentEquals(WorkflowClient.WORKFLOWSTATE_DELETED)) {
                isDeleted = true;
            }
        }
        return isDeleted;
    }
}
