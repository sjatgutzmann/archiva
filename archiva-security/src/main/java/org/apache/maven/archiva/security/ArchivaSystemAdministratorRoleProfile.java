package org.apache.maven.archiva.security;

/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.codehaus.plexus.rbac.profile.AbstractRoleProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * @todo why does this need to be created in the client app?
 * @todo composition instead of inheritence?
 * @plexus.component role="org.codehaus.plexus.rbac.profile.RoleProfile" role-hint="archiva-system-administrator"
 */
public class ArchivaSystemAdministratorRoleProfile
    extends AbstractRoleProfile
{
    public String getRoleName()
    {
        return ArchivaRoleConstants.SYSTEM_ADMINISTRATOR_ROLE;
    }

    public List getOperations()
    {
        List operations = new ArrayList();
        operations.add( ArchivaRoleConstants.OPERATION_MANAGE_CONFIGURATION );
        operations.add( ArchivaRoleConstants.OPERATION_MANAGE_USERS );
        operations.add( ArchivaRoleConstants.OPERATION_RUN_INDEXER );
        operations.add( ArchivaRoleConstants.OPERATION_REGENERATE_INDEX );
        operations.add( ArchivaRoleConstants.OPERATION_ACCESS_REPORT ); // TODO: does this need to be templated?
        operations.add( ArchivaRoleConstants.OPERATION_ADD_REPOSITORY );
        operations.add( ArchivaRoleConstants.OPERATION_EDIT_REPOSITORY );
        operations.add( ArchivaRoleConstants.OPERATION_DELETE_REPOSITORY );
        // we don't add access/upload repository operations. This isn't a sys-admin function, and we don't want to
        // encourage the use of the sys admin role for such operations. They can grant it as necessary.
        return operations;
    }

    public boolean isAssignable()
    {
        return false;
    }
}
