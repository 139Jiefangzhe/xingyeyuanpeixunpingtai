import type { EnumDictionary } from "../types/api";
import { normalizeEnumDictionary } from "../utils/enumMap";
import { fallbackExamEnums, isMockEnabled } from "./mockExamData";
import request from "../utils/request";

const pickFallbackEnums = (keys: string[]) =>
  keys.reduce<EnumDictionary>((accumulator, key) => {
    accumulator[key] = fallbackExamEnums[key] || [];
    return accumulator;
  }, {});

export const enumApi = {
  async getMany(keys: string[]): Promise<EnumDictionary> {
    if (keys.length === 0) {
      return {};
    }
    if (isMockEnabled) {
      return pickFallbackEnums(keys);
    }
    try {
      const result = await request.get<unknown>("/api/v1/enums", {
        params: { keys: keys.join(",") },
      });
      return normalizeEnumDictionary(result.data, keys);
    } catch (_error) {
      return pickFallbackEnums(keys);
    }
  },
};
