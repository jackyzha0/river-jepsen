Jepsen tests for `@replit/river`

## running tests

- relies on docker + a bit of perl + bash for setup

```bash
make tests
```

seeing results

```bash
make results
```

## adding tests

### kv
1. rpc set(k,v) -> v
2. subscribe val(k) -> updates to v | err if k does not exist

### repeat
1. stream echo(s) -> s
2. stream echo_prefix(init: prefix, s) -> prefix + s

### upload
1. upload send(part) -> total str after EOF received

## adding clients
- all clients should have their own transport client id describing itself (e.g. `bun-client`)
- should listen on stdio for instructions

simplified instruction DSL
- id is generated per rpc to distinguish responses
- inputs are `id -- svc proc -> ...args`
- outputs are `id -- ok:resp | err:code`

```
# kv
id -- kv set -> k v
id -- kv subscribe -> k

# echo
id -- repeat echo -> s
id -- repeat echo_prefix -> prefix

# upload
id -- upload send ->

# meta
id -- -> s (push to existing stream id)
```

## adding servers

- always assume transport client id is `SERVER`

TODO
