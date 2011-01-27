package org.sonatype.nexus.plugins.redirect;

public class Redirect
{
    private final String key;

    private final String target;

    private final boolean permanent;

    public Redirect( final String key, final String target, final boolean permanent )
    {
        this.key = key;
        this.target = target;
        this.permanent = permanent;
    }

    public String getKey()
    {
        return key;
    }

    public String getTarget()
    {
        return target;
    }

    public boolean isPermanent()
    {
        return permanent;
    }
}
