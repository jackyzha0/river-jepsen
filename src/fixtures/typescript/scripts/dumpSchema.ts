import { serializeService } from "@replit/river";
import { serviceDefs } from "../service";

console.log(
    JSON.stringify(Object.values(serviceDefs).map(serializeService), null, 2),
  );
process.exit(0);