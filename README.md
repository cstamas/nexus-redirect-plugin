Simple Redirect Plugin for Nexus
================================

Usage:

GETting a "key" will redirect you to the target of "key" (if exists, 404 otherwise):

GET http://localhost:8081/nexus/service/local/redirect/foo

key = "foo"

PUTting to a "key" will create/update redirect of the "key" (201 if creates, 200 if updates):

PUT http://localhost:8081/nexus/service/local/redirect/foo?t=http://www.sonatype.com/

Parameters:

1. t - target URL, if relative will be resolved against Nexus base
2. p (optional) - a boolean if "true", redirect will be permanent, otherwise temporary

Optionally, if you PUT to /redirect resource directly (omit the "/foo"), a key will be created for you automatically, and returned in Location header but also in body of response.

DELETING a redirect will just remove the key (204 response, or 404 if there is no such key)

DELETE http://localhost:8081/nexus/service/local/redirect/foo