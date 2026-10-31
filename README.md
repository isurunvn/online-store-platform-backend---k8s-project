# Online Store Backend

Spring Boot application combining users, product catalog, and orders. It uses
Java 21, PostgreSQL, RabbitMQ, and Keycloak JWT authentication.

## ARM64 image publishing to the existing OCIR repository

The workflow `.github/workflows/build-image.yml` builds a `linux/arm64` image for
our OKE workers and publishes it to OCIR. It is manually triggered from the
`main` branch. Committing or pushing this workflow alone does not publish an
image or deploy anything to Kubernetes.

The Maven build stage uses `$BUILDPLATFORM` so compilation runs on the GitHub
runner's native CPU. The Java JAR is copied into an ARM64 Java runtime image;
there are no ARM64 build commands to emulate in the runtime stage. The image
defaults to `SERVER_PORT=8083`, matching its exposed port and Docker healthcheck.
Kubernetes probes are configured in the GitOps Deployment.

### Configure the GitHub prod environment

In this backend repository, open **Settings > Environments > prod**.
Add these environment variables under **Variables**:

| Name | Value |
| --- | --- |
| `OCIR_REGISTRY` | `phx.ocir.io` for the configured Phoenix region, without `https://` |
| `OCIR_NAMESPACE` | `axkouhjvgred`, the namespace returned by the configured OCI account |
| `OCIR_REPOSITORY` | `online-store/backend` |

The verified backend repository is `online-store/backend`.

Add these environment secrets under **Secrets**:

| Name | Value |
| --- | --- |
| `OCIR_USERNAME` | The complete OCIR login username, normally `<tenancy-namespace>/<username>` or `<tenancy-namespace>/<domain-name>/<username>` |
| `OCIR_AUTH_TOKEN` | An OCI auth token belonging to a user permitted to push to the existing repository |

The auth token is the registry password. It is not the OCI API signing key or
the OCI Console password. Do not put it in source files or paste it into chat.
The workflow checks that settings exist without printing credential values.

### Publish after reviewing the workflow

1. Commit and push the reviewed changes to the backend repository's `main` branch.
2. Configure the variables and secrets above.
3. Open **Actions > Build ARM64 backend image for OCIR > Run workflow**, selecting
   `main`. This action builds and publishes an image; it does not deploy it.
4. Wait for the workflow to succeed and copy the **Deployment image** from its
   run summary. It includes a SHA-256 digest for the later GitOps Deployment.

The tag has this format:

```text
phx.ocir.io/axkouhjvgred/<existing-backend-repository>:sha-<full-git-commit-sha>
```

Use the reported digest for deployment because rebuilding the same commit can
resolve newer base images. The workflow does not update the GitOps repository.
For a private OCIR repository, OKE uses the existing `online-store/ocir-pull-secret` for registry pulls.

This workflow compiles, tests and packages the application; successful image publication does not prove database,
RabbitMQ, or Keycloak connectivity. Those checks belong to the backend rollout.

## References

- [Oracle: pushing images to OCIR](https://docs.oracle.com/en-us/iaas/Content/Registry/Tasks/registrypushingimagesusingthedockercli.htm)
- [Docker: building for multiple platforms](https://docs.docker.com/build/building/multi-platform/)

## Final demo integration

The latest image must be rebuilt for checkout/user fixes, email-bearing order events and publisher
confirmations. GitHub prod environment settings are used by the publishing job. Docker builds now run
Maven tests instead of skipping them. The separate validation workflow runs on main pushes and PRs.

Runtime authentication still validates JWT_ISSUER_URI, while the Kubernetes ConfigMap supplies an
internal JWK endpoint. CUSTOMER creates orders, ADMIN manages categories/products, and customers
cannot read another user's profile or order. The checkout body is `{"items":[{"productId":1,"quantity":2}]}`.

Product stock updates lock the database row and reject negative stock. Order events are persistent,
mandatory-routed and broker-confirmed. SQL commit and RabbitMQ publish are still separate operations;
a transactional outbox would be needed for atomic publication guarantees. No payment processing is
implemented; a newly created order remains PENDING.

See platform-gitops/FINISH-PROJECT.md for image promotion, private frontend access and demo validation.
