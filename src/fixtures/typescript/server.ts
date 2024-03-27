import http from "http";
import { WebSocketServer } from "ws";
import { WebSocketServerTransport } from "@replit/river/transport/ws/server";
import { createServer } from "@replit/river";
import { bindLogger, log, setLevel } from "@replit/river/logging";
import { serviceDefs } from "./service";
import { transportOptions } from "./protocol";

// bind river loggers
bindLogger(console.log);
setLevel("debug");

const port = process.env.PORT;
if (!port) {
  console.error("PORT env var is required");
  process.exit(1);
}

// start websocket server on port 3001
const httpServer = http.createServer();
const wss = new WebSocketServer({ server: httpServer });
const transport = new WebSocketServerTransport(wss, "SERVER", transportOptions);
export const server = createServer(transport, serviceDefs);
export type ServiceSurface = typeof server;

httpServer.listen(port, () => {
  console.log('ok');
});