#!/usr/bin/env bash

# all fixtures start at /jepsen/src/fixtures
cd typescript
bun install
bun run server.ts