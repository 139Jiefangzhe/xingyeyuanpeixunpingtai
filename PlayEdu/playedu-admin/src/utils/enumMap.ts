import type { EnumDictionary, EnumOption } from "../types/api";

const ENUM_KEY_ALIASES: Record<string, string[]> = {
  questionType: ["questionType", "QuestionType"],
  questionDifficulty: ["questionDifficulty", "Difficulty", "QuestionDifficulty"],
  examPaperStatus: ["examPaperStatus", "ExamPaperStatus"],
  examPaperType: ["examPaperType", "ExamPaperType"],
};

const normalizeOption = (item: unknown, index: number): EnumOption | null => {
  if (item == null) {
    return null;
  }

  if (typeof item === "string" || typeof item === "number") {
    return {
      label: String(item),
      value: item,
    };
  }

  if (typeof item === "object") {
    const record = item as Record<string, unknown>;
    const value =
      record.value ??
      record.code ??
      record.key ??
      record.id ??
      record.enumValue ??
      index;
    const label =
      record.label ??
      record.name ??
      record.text ??
      record.desc ??
      record.title ??
      String(value);

    return {
      label: String(label),
      value: value as string | number,
    };
  }

  return null;
};

const normalizeOptions = (source: unknown): EnumOption[] => {
  if (Array.isArray(source)) {
    return source
      .map((item, index) => normalizeOption(item, index))
      .filter((item): item is EnumOption => item !== null);
  }

  if (source && typeof source === "object") {
    return Object.entries(source as Record<string, unknown>).map(([value, label]) => ({
      value,
      label: String(label),
    }));
  }

  return [];
};

const findEnumSource = (source: Record<string, unknown>, key: string) => {
  const candidates = ENUM_KEY_ALIASES[key] || [key];
  for (const candidate of candidates) {
    if (candidate in source) {
      return source[candidate];
    }
  }
  return undefined;
};

export const normalizeEnumDictionary = (
  payload: unknown,
  keys: string[]
): EnumDictionary => {
  const source =
    payload && typeof payload === "object"
      ? (payload as Record<string, unknown>)
      : {};

  return keys.reduce<EnumDictionary>((accumulator, key) => {
    accumulator[key] = normalizeOptions(findEnumSource(source, key));
    return accumulator;
  }, {});
};

export const getEnumLabel = (
  dictionary: EnumDictionary,
  key: string,
  value: string | number | null | undefined
) => {
  if (value == null) {
    return "-";
  }
  const options = dictionary[key] || [];
  const matched = options.find((item) => String(item.value) === String(value));
  return matched?.label || String(value);
};
