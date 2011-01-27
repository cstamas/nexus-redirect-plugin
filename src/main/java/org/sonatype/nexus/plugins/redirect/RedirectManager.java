package org.sonatype.nexus.plugins.redirect;

public interface RedirectManager
{
    Redirect lookupRedirectForKey( String key );

    // List<Redirect> getRedirects();

    // returns true if _replace_ happened
    boolean addRedirect( String key, String target, boolean permanent );

    // return true if _removed_ (key existed)
    boolean removeRedirect( String key );
}
