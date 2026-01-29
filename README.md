<table>
  <tr>
    <td><h1>Watt🗲son</h1></td>
    <td><img src="src/main/resources/META-INF/resources/wattson_dark.png" alt="Wattson Logo" height="48px" align="center"></td>
  </tr>
</table>


Java-based implementation of an Open Charge Point Protocol (OCPP) 1.6 compliant CSMS.
Also implements the necessary WebSocket specification (parsing and sending WebSocket frames) manually (for fun).

## Goal

The goal is to develop a CSMS that optimizes EV charging by considering, e.g., solar energy production, solar-battery
state of charge, electricity pricing, as well as time of day, with support for user-defined custom rules.

## Quickstart

Clone this repo and set all properties in `src/main/resources/application-secret.properties`.
Then run using docker compose:

```shell
docker compose up
```

TODO ...





--------------------------------------------------------------------------------------------

### Attributions

- https://www.flaticon.com/free-icons/sherlock-holmes
- https://www.flaticon.com/free-icons/clean-energy