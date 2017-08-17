# Java client for Subutai Social Hub

Example usage:

```
HubClient client = HubClients.getClient();

client.login("username", "password");

List<Environment> environments = client.getEnvironments();

List<Peer> peers = client.getPeers();

List<Template> templates = client.getTemplates();

....

EnvironmentCreationRequest request = client.createRequest("my-test-env");

request.addNode( "test-container", templateId, ContainerSize.SMALL, peerId, hostId );

client.createEnvironment(request);
```
