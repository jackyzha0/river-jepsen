import http from "http";
import { WebSocket, WebSocketServer } from "ws";
import { bindLogger, log, setLevel } from "@replit/river/logging";
import { WebSocketClientTransport } from "@replit/river/transport/ws/client";
import { transportOptions } from "./protocol";
import readline from 'node:readline';
import { createClient } from "@replit/river";
import type { ServiceSurface } from "./server";

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

const client = createClient<ServiceSurface>(clientTransport, "SERVER");

// listen for jepsen driver commands
const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout,
  terminal: false
});

const handles = new Map()
rl.on('line', async (line) => {
  const match = line.match(/(?<id>\w+) -- (?:(?<svc>\w+) (?<proc>\w+) )?-> ?(?<payload>.*)/);
  if (!match || !match.groups) {
    console.log("FATAL: invalid command", line);
    process.exit(1);
  }

  const { id, svc, proc, payload } = match.groups;
  if (svc === 'kv') {
    if (proc === 'set') {
      const [k, v] = payload.split(' ');
      const res = await client.kv.set.rpc({k, v: parseInt(v)});
      if (res.ok) {
        console.log(`${id} -- ok:${res.payload}`);
      } else {
        console.log(`${id} -- err:${res.payload.code}`);
      }
    }
  }
});
