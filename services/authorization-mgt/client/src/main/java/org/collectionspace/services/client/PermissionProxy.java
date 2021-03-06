/**
 * PermissionProxy.java
 *
 * {Purpose of This Class}
 *
 * {Other Notes Relating to This Class (Optional)}
 *
 * $LastChangedBy: $
 * $LastChangedRevision: $
 * $LastChangedDate: $
 *
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright (C) 2009 {Contributing Institution}
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.collectionspace.services.authorization.perms.Permission;
import org.collectionspace.services.authorization.perms.PermissionsList;

/**
 * @version $Revision:$
 */
@Path("/authorization/permissions")
@Produces({"application/xml"})
@Consumes({"application/xml"})
public interface PermissionProxy extends CollectionSpaceProxy<PermissionsList> {

    @Override
	@GET
    @Produces({"application/xml"})
    Response readList();

    @GET
    Response readSearchList(@QueryParam("res") String resourceName);

    //(C)reate
    @POST
    Response create(Permission permission);

    //(R)ead
    @GET
    @Path("/{csid}")
    Response read(@PathParam("csid") String csid);
    
    //(R)read
    @GET
    Response read(@QueryParam("res") String resourceName, @QueryParam("actGrp") String actionGroup);
    
    //(U)pdate
    @PUT
    @Path("/{csid}")
    Response update(@PathParam("csid") String csid, Permission permission);
}
