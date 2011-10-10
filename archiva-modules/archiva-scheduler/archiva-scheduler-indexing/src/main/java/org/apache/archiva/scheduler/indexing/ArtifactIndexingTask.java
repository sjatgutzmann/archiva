package org.apache.archiva.scheduler.indexing;

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

import org.apache.archiva.admin.model.beans.ManagedRepository;
import org.apache.maven.index.NexusIndexer;
import org.apache.maven.index.context.IndexCreator;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.context.UnsupportedExistingLuceneIndexException;
import org.codehaus.plexus.taskqueue.Task;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ArtifactIndexingTask
    implements Task
{
    public enum Action
    {
        ADD,
        DELETE,
        FINISH
    }

    private final ManagedRepository repository;

    private final File resourceFile;

    private final Action action;

    private final IndexingContext context;

    private boolean executeOnEntireRepo = true;

    /**
     * @since 1.4-M1
     */
    private boolean onlyUpdate = false;

    public ArtifactIndexingTask( ManagedRepository repository, File resourceFile, Action action,
                                 IndexingContext context )
    {
        this.repository = repository;
        this.resourceFile = resourceFile;
        this.action = action;
        this.context = context;
    }

    public ArtifactIndexingTask( ManagedRepository repository, File resourceFile, Action action,
                                 IndexingContext context, boolean executeOnEntireRepo )
    {
        this( repository, resourceFile, action, context );
        this.executeOnEntireRepo = executeOnEntireRepo;
    }

    public ArtifactIndexingTask( ManagedRepository repository, File resourceFile, Action action,
                                 IndexingContext context, boolean executeOnEntireRepo, boolean onlyUpdate )
    {
        this( repository, resourceFile, action, context, executeOnEntireRepo );
        this.onlyUpdate = onlyUpdate;
    }

    public boolean isExecuteOnEntireRepo()
    {
        return executeOnEntireRepo;
    }

    public void setExecuteOnEntireRepo( boolean executeOnEntireRepo )
    {
        this.executeOnEntireRepo = executeOnEntireRepo;
    }

    public long getMaxExecutionTime()
    {
        return 0;
    }

    public File getResourceFile()
    {
        return resourceFile;
    }

    public Action getAction()
    {
        return action;
    }

    public ManagedRepository getRepository()
    {
        return repository;
    }

    public IndexingContext getContext()
    {
        return context;
    }

    public boolean isOnlyUpdate()
    {
        return onlyUpdate;
    }

    public void setOnlyUpdate( boolean onlyUpdate )
    {
        this.onlyUpdate = onlyUpdate;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + action.hashCode();
        result = prime * result + repository.getId().hashCode();
        result = prime * result + ( ( resourceFile == null ) ? 0 : resourceFile.hashCode() );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( getClass() != obj.getClass() )
        {
            return false;
        }
        ArtifactIndexingTask other = (ArtifactIndexingTask) obj;
        if ( !action.equals( other.action ) )
        {
            return false;
        }
        if ( !repository.getId().equals( other.repository.getId() ) )
        {
            return false;
        }
        if ( resourceFile == null )
        {
            if ( other.resourceFile != null )
            {
                return false;
            }
        }
        else if ( !resourceFile.equals( other.resourceFile ) )
        {
            return false;
        }
        return true;
    }


    @Override
    public String toString()
    {
        return "ArtifactIndexingTask [action=" + action + ", repositoryId=" + repository.getId() + ", resourceFile="
            + resourceFile + "]";
    }

    /**
     * FIXME remove this static somewhere else !
     * @param repository
     * @param indexer
     * @param indexCreators
     * @return
     * @throws IOException
     * @throws UnsupportedExistingLuceneIndexException
     */
    public static IndexingContext createContext( ManagedRepository repository, NexusIndexer indexer,
                                                 List<? extends IndexCreator> indexCreators )
        throws IOException, UnsupportedExistingLuceneIndexException
    {
        IndexingContext context = indexer.getIndexingContexts().get( repository.getId() );

        if ( context != null )
        {
            LoggerFactory.getLogger( ArtifactIndexingTask.class ).debug(
                "skip adding repository with id {} as already exists", repository.getId() );
            return context;
        }


        String indexDir = repository.getIndexDirectory();
        File managedRepository = new File( repository.getLocation() );

        File indexDirectory = null;
        if ( indexDir != null && !"".equals( indexDir ) )
        {
            indexDirectory = new File( repository.getIndexDirectory() );
        }
        else
        {
            indexDirectory = new File( managedRepository, ".indexer" );
        }


        context = indexer.addIndexingContext( repository.getId(), repository.getId(), managedRepository, indexDirectory,
                                              managedRepository.toURI().toURL().toExternalForm(),
                                              indexDirectory.toURI().toURL().toString(), indexCreators );

        context.setSearchable( repository.isScanned() );
        return context;
    }
}
