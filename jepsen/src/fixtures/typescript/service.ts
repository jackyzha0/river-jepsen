import { ServiceBuilder, Ok, buildServiceDefs } from '@replit/river';
import { Type } from '@sinclair/typebox';

export class Observable<T> {
  value: T;
  private listeners: Set<(val: T) => void>;

  constructor(initialValue: T) {
    this.value = initialValue;
    this.listeners = new Set();
  }

  get() {
    return this.value;
  }

  set(tx: (preValue: T) => T) {
    const newValue = tx(this.value);
    this.value = newValue;
    this.listeners.forEach((listener) => listener(newValue));
  }

  observe(listener: (val: T) => void) {
    this.listeners.add(listener);
    listener(this.get());
    return () => this.listeners.delete(listener);
  }
}

const ExampleServiceConstructor = () =>
  ServiceBuilder.create('example')
    // initializer for shared state
    .initialState({
      count: 0,
    })
    .defineProcedure('add', {
      type: 'rpc',
      input: Type.Object({ n: Type.Number() }),
      output: Type.Object({ result: Type.Number() }),
      errors: Type.Never(),
      async handler(ctx, { n }) {
        ctx.state.count += n;
        return Ok({ result: ctx.state.count });
      },
    })
    .finalize();

// export a listing of all the services that we have
export const serviceDefs = buildServiceDefs([ExampleServiceConstructor()]);