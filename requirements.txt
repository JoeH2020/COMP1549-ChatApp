# Requirements
## Non Functional Requirements
- Use a modular design
- Use JUnit testing
- Ensure Fault Tolerance (especially connection timeouts)
- Use a component based design
## Fuctional Requirements
### Client Requirements
- The client should specify:
  * Unique ID
  * The port it will listen to
  * Its own IP
  * The server's port
  * The server's IP
- The client should keep a list of other online members
- The Coordinator client should periodically check if other members are still online, and send a message to the server if they are not
### Server Requirements
- The server should tell the first client that he is the coordinator
- The server should check periodically if the coordinator is still online, and assign a new one if he is not
- The server should update all clients on members that have disconnected (normally or through a coordinator timeout)
