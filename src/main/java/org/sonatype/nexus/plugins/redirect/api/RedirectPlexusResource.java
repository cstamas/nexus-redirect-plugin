package org.sonatype.nexus.plugins.redirect.api;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.plugins.redirect.Redirect;
import org.sonatype.nexus.plugins.redirect.RedirectManager;
import org.sonatype.nexus.util.DigesterUtils;
import org.sonatype.plexus.rest.ReferenceFactory;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "RedirectPlexusResource" )
public class RedirectPlexusResource
    extends AbstractPlexusResource
{
    public static final String RESOURCE_URI = "/redirect";

    @Requirement
    private RedirectManager redirectManager;

    @Requirement
    private ReferenceFactory referenceFactory;

    @Override
    public boolean isModifiable()
    {
        return true;
    }

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcNxBasic,perms[nexus:status]" );
    }

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        final String key = getKey( request, false );

        if ( null == key )
        {
            // list all redirect keys?

            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
        }
        else
        {
            Redirect redirect = redirectManager.lookupRedirectForKey( key );

            if ( null == redirect )
            {
                throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, String.format( "Key %s not found!", key ) );
            }
            else
            {
                if ( redirect.isPermanent() )
                {
                    response.redirectPermanent( redirect.getTarget() );
                }
                else
                {
                    response.redirectTemporary( redirect.getTarget() );
                }
            }

            return new StringRepresentation( String.format( "Redirected to %s", redirect.getTarget() ) );
        }
    }

    @Override
    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        return put( context, request, response, payload );
    }

    @Override
    public Object put( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        final String userKey = getKey( request, false );

        final String key = StringUtils.isBlank( userKey ) ? createKey( request ) : userKey;

        final Form form = request.getResourceRef().getQueryAsForm();

        final String target = form.getFirstValue( "t" );

        final boolean permanent = Boolean.valueOf( form.getFirstValue( "p", Boolean.FALSE.toString() ) );

        // TODO: target validation?
        // It does not have to be absolute (restlet will resolve it against _this_ resource!), but still...

        if ( StringUtils.isBlank( key ) || StringUtils.isBlank( target ) )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Key and Target must be provided!" );
        }

        final boolean replaced = redirectManager.addRedirect( key, target, permanent );

        if ( replaced )
        {
            response.setStatus( Status.SUCCESS_OK, String.format( "Redirect key %s updated to target %s", key, target ) );
        }
        else
        {
            response.setStatus( Status.SUCCESS_CREATED,
                String.format( "Redirect key %s added with target %s", key, target ) );
        }

        response.setLocationRef( referenceFactory.createChildReference( request, key ) );

        return new StringRepresentation( response.getLocationRef().toString() );
    }

    @Override
    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {
        final String key = getKey( request, true );

        if ( !StringUtils.isBlank( key ) )
        {
            if ( redirectManager.removeRedirect( key ) )
            {
                response.setStatus( Status.SUCCESS_NO_CONTENT, String.format( "Key %s deleted!", key ) );
            }
            else
            {
                throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, String.format( "Key %s not found!", key ) );
            }
        }
        else
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Wrong request, key not provided!" );
        }
    }

    // ==

    protected String getKey( Request request, boolean failIfNotFound )
    {
        String remainingPart = request.getResourceRef().getRemainingPart();

        if ( !StringUtils.isBlank( remainingPart ) )
        {
            // "shave" from beginning
            if ( remainingPart.startsWith( "/" ) )
            {
                remainingPart = remainingPart.substring( 1 );
            }

            if ( !remainingPart.contains( "/" ) )
            {
                return remainingPart;
            }
        }

        return null;
    }

    protected String createKey( Request request )
    {
        return DigesterUtils.getSha1Digest( Long.toString( System.nanoTime() ) );
    }
}
