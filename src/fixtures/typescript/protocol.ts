import { BinaryCodec } from "@replit/river/codec";
import { TransportOptions } from "@replit/river/transport";

export const transportOptions: Partial<TransportOptions> = {
    // wire format
    codec: BinaryCodec,

    // options for adjusting behaviour when retrying a broken connection
    retryIntervalMs: 250,
    retryJitterMs: 500,
    retryAttemptsMax: 5,
  
    // options for heartbeat
    heartbeatIntervalMs: 2_500, // lower heartbeat helps with lower memory usage (send buff is smaller)
    heartbeatsUntilDead: 4,
  
    // how long to wait after a connection disconnect to consider
    // it a session disconnect
    sessionDisconnectGraceMs: 15_000,
  };