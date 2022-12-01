export class Mutable<T> {
    private inner: T;
    constructor(obj: T) {
        this.inner = obj;
    }

    public get(): T {
        return this.inner;
    }

    public set(obj: T) {
        this.inner = obj;
    }
}