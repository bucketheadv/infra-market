import * as jsonpath from 'jsonpath'

/**
 * JSONPath表达式验证工具
 */
export class JsonPathValidator {
  /**
   * 验证JSONPath表达式是否有效
   * @param jsonPath JSONPath表达式
   * @returns 验证结果
   */
  static validate(jsonPath: string): { isValid: boolean; errorMessage?: string } {
    if (!jsonPath || jsonPath.trim() === '') {
      return { isValid: true }
    }

    try {
      // 尝试编译JSONPath表达式
      jsonpath.parse(jsonPath)
      return { isValid: true }
    } catch (error: any) {
      return {
        isValid: false,
        errorMessage: `JSONPath表达式无效: ${error.message}`
      }
    }
  }

  /**
   * 获取JSONPath表达式的示例
   */
  static getExamples(): string[] {
    return [
      '$.data.result',
      '$.items[0].name',
      '$.user.profile.email',
      '$.status',
      '$.data.items[*].id',
      '$.response.data[?(@.active==true)].name'
    ]
  }
}
