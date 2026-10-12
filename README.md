# NixTP

Teleport requests between players.

- `/tpa <player>` sends a request
- `/tpaccept` and `/tpdeny` answer it
- Requests expire after the time set in `config.yml`

Build: `mvn clean package` -> `target/NixTP-1.0.0.jar`
Requires Java 21 and Paper 1.21.4.
