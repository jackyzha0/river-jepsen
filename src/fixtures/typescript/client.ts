import http from "http";
import { WebSocket, WebSocketServer } from "ws";
import { bindLogger, log, setLevel } from "@replit/river/logging";
import { serviceDefs } from "./service";
import { WebSocketClientTransport } from "@replit/river/transport/ws/client";
import { transportOptions } from "./protocol";

// bind river loggers
bindLogger(console.log);
setLevel("debug");

const port = process.env.PORT
if (!port) {
  console.error("port required");
  process.exit(1);
}

const clientTransport = new WebSocketClientTransport(
  () => Promise.resolve(new WebSocket(port)),
  "SERVER",
  transportOptions,
);

clientTransport.connect('SERVER');