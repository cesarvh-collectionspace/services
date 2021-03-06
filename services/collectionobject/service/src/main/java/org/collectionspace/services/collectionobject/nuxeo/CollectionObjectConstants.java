/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.collectionobject.nuxeo;

import org.collectionspace.services.client.CollectionSpaceClient;

/**
 * CollectionObjectConstants processes CollectionObject document
 *
 */
public class CollectionObjectConstants {

    public final static String NUXEO_DOCTYPE = "CollectionObject";
    public final static String NUXEO_SCHEMA_NAME = "collectionobject";
    public final static String NUXEO_DC_TITLE = "CollectionSpace-CollectionObject";
    
	public static final String WORKFLOW_STATE_SCHEMA_NAME = CollectionSpaceClient.COLLECTIONSPACE_CORE_SCHEMA;
	public static final String WORKFLOW_STATE_FIELD_NAME = CollectionSpaceClient.COLLECTIONSPACE_CORE_WORKFLOWSTATE; //"workflowState";    
}
