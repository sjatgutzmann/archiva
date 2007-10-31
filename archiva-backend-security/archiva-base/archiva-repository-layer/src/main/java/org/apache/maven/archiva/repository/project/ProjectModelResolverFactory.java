package org.apache.maven.archiva.repository.project;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.lang.StringUtils;
import org.apache.maven.archiva.configuration.ArchivaConfiguration;
import org.apache.maven.archiva.configuration.ConfigurationNames;
import org.apache.maven.archiva.configuration.ManagedRepositoryConfiguration;
import org.apache.maven.archiva.repository.ManagedRepositoryContent;
import org.apache.maven.archiva.repository.RepositoryContentFactory;
import org.apache.maven.archiva.repository.RepositoryException;
import org.apache.maven.archiva.repository.project.resolvers.ManagedRepositoryProjectResolver;
import org.apache.maven.archiva.repository.project.resolvers.NopProjectResolver;
import org.apache.maven.archiva.repository.project.resolvers.ProjectModelResolverStack;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.registry.Registry;
import org.codehaus.plexus.registry.RegistryListener;

import java.util.List;

/**
 * Factory for ProjectModelResolver objects
 *
 * @author <a href="mailto:joakime@apache.org">Joakim Erdfelt</a>
 * @version $Id$
 * @plexus.component role="org.apache.maven.archiva.repository.project.ProjectModelResolverFactory"
 */
public class ProjectModelResolverFactory
    extends AbstractLogEnabled
    implements RegistryListener, Initializable
{
    /**
     * @plexus.requirement
     */
    private ArchivaConfiguration archivaConfiguration;
    
    /**
     * @plexus.requirement
     */
    private RepositoryContentFactory repositoryFactory;

    /**
     * @plexus.requirement role-hint="model400"
     */
    private ProjectModelReader project400Reader;

    /**
     * @plexus.requirement role-hint="model300"
     */
    private ProjectModelReader project300Reader;
    
    private ProjectModelResolverStack currentResolverStack = new ProjectModelResolverStack();

    public void afterConfigurationChange( Registry registry, String propertyName, Object propertyValue )
    {
        if ( ConfigurationNames.isManagedRepositories( propertyName ) )
        {
            update();
        }
    }

    public void beforeConfigurationChange( Registry registry, String propertyName, Object propertyValue )
    {
        /* do nothing */
    }

    public ProjectModelResolverStack getCurrentResolverStack()
    {
        return currentResolverStack;
    }

    public void initialize()
        throws InitializationException
    {
        update();
        archivaConfiguration.addChangeListener( this );
    }

    private ManagedRepositoryProjectResolver toResolver( ManagedRepositoryConfiguration repo )
        throws RepositoryException
    {
        ManagedRepositoryContent repoContent = repositoryFactory.getManagedRepositoryContent( repo.getId() );
        ProjectModelReader reader = project400Reader;

        if ( StringUtils.equals( "legacy", repo.getLayout() ) )
        {
            reader = project300Reader;
        }

        return new ManagedRepositoryProjectResolver( repoContent, reader );
    }

    private void update()
    {
        synchronized ( currentResolverStack )
        {
            this.currentResolverStack.clearResolvers();

            List<ManagedRepositoryConfiguration> list =
                archivaConfiguration.getConfiguration().getManagedRepositories();
            for ( ManagedRepositoryConfiguration repo : list )
            {
                try
                {
                    ManagedRepositoryProjectResolver resolver = toResolver( repo );

                    // Add filesystem based resolver.
                    this.currentResolverStack.addProjectModelResolver( resolver );
                }
                catch ( RepositoryException e )
                {
                    getLogger().warn( e.getMessage(), e );
                }
            }

            // Add no-op resolver.
            this.currentResolverStack.addProjectModelResolver( NopProjectResolver.getInstance() );
        }
    }
}
