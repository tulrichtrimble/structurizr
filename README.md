# Structurizr

Structurizr is a "models as code" tool designed for the [C4 model](https://c4model.com) - you write [Structurizr DSL](https://docs.structurizr.com/dsl) to create multiple software architecture diagrams from a single model. Structurizr was created by [the author of the C4 model](https://simonbrown.je) and remains the reference implementation, making it the best choice if you are looking for compliance and compatibility with the C4 model.

- [Structurizr DSL - Example](https://docs.structurizr.com/dsl/example)
- [Structurizr DSL - Tutorial](https://docs.structurizr.com/dsl/tutorial)

## Quickstart

The two quickest ways to get started with the Structurizr tooling are:

1. Visit [playground.structurizr.com](https://playground.structurizr.com) to try Structurizr without installing any tooling.
2. Use Structurizr [local](https://docs.structurizr.com/local) via the pre-built Docker image to create diagrams on your computer - see [local - Quickstart](https://docs.structurizr.com/local/quickstart).

[![Structurizr playground](https://docs.structurizr.com/images/playground.png)](https://playground.structurizr.com)

## Documentation

See [docs.structurizr.com](https://docs.structurizr.com) for documentation, getting started guides, tutorials, etc.

## Trimble Identity integration (enterprise hosting)

This repository can be configured for enterprise-hosted deployments that require:

- OIDC sign-in for the web UI.
- Token-based API authentication for workspace uploads.

The key properties are:

```properties
structurizr.authentication=oidc

# OIDC web login
structurizr.authentication.oidc.issueruri=https://id.trimble.com
structurizr.authentication.oidc.clientid=<client-id>
structurizr.authentication.oidc.clientsecret=<client-secret-optional>
structurizr.authentication.oidc.scopes=openid,profile,email
structurizr.authentication.oidc.usernameclaim=email

# API upload token validation
structurizr.authentication.api.issueruri=https://id.trimble.com
structurizr.authentication.api.jwkseturi=https://id.trimble.com/.well-known/jwks.json
structurizr.authentication.api.audience=structurizr-upload
structurizr.authentication.api.scopes=structurizr.upload

# Optional migration fallback
structurizr.authentication.api.sharedtoken=
```

When configured, API uploads can pass a client-credentials OAuth access token via `-key`, e.g.:

```bash
java -jar structurizr-1.0.0.war push -url https://structurizr-app.example.com/api -id 2 -workspace ./workspace.json -key "$ACCESS_TOKEN" -merge false -archive true
```