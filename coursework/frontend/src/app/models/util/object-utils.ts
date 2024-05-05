import {DateTimeFormatter, LocalDateTime} from 'js-joda';

export class ObjectUtils {
  private constructor() {

  }

  static replaceEmptyWithNull<T>(obj: T): T {
    if (typeof obj !== 'object' || obj === null) {
      return obj;
    }

    if (Array.isArray(obj)) {
      return obj.map((item) => this.replaceEmptyWithNull(item)) as any;
    }

    const result: Partial<T> = {};

    for (const key in obj) {
      if (obj.hasOwnProperty(key)) {
        const value = obj[key];
        if (value === '' || value === undefined) {
          result[key] = null;
        } else {
          result[key] = this.replaceEmptyWithNull(value);
        }
      }
    }

    return result as T;
  }

  static formatTimestamp(timestamp: string): string {
    const dateTime = LocalDateTime.parse(timestamp);
    const formatter = DateTimeFormatter.ofPattern('yyyy-MM-dd HH:mm:ss');
    const formattedDateTime = dateTime.format(formatter);
    return formattedDateTime;
  }

  static formatDate(data: Date): string {
    const timestamp = data.toString();
    const dateTime = LocalDateTime.parse(timestamp);
    const formatter = DateTimeFormatter.ofPattern('yyyy-MM-dd HH:mm:ss');
    const formattedDateTime = dateTime.format(formatter);
    return formattedDateTime;
  }
}
