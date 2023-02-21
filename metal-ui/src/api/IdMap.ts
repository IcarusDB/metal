import _ from "lodash";

export function idMap<T>(obj: any): T {
    obj = _.mapKeys(obj, (val, key) => {
        return key === '_id' ? 'id' : key;
    });
    const target: T = obj;
    return target;
}
