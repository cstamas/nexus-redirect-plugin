package org.sonatype.nexus.plugins.redirect;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.component.annotations.Component;

/**
 * Not a real implementation, just for toying.
 * 
 * @author cstamas
 */
@Component( role = RedirectManager.class )
public class DefaultRedirectManager
    implements RedirectManager
{
    private Map<String, Redirect> redirects = new ConcurrentHashMap<String, Redirect>();

    public Redirect lookupRedirectForKey( String key )
    {
        return redirects.get( key );
    }

    public boolean addRedirect( String key, String target, boolean permanent )
    {
        final Redirect redirect = new Redirect( key, target, permanent );

        return redirects.put( key, redirect ) != null;
    }

    public boolean removeRedirect( String key )
    {
        return redirects.remove( key ) != null;
    }
}
