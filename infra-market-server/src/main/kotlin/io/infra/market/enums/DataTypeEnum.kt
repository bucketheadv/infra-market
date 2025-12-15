package io.infra.market.enums

/**
 * 数据类型枚举
 */
enum class DataTypeEnum(val code: String, val description: String) {
    // 基础数据类型
    STRING("STRING", "字符串"),
    INTEGER("INTEGER", "整数"),
    LONG("LONG", "长整数"),
    DOUBLE("DOUBLE", "双精度浮点数"),
    BOOLEAN("BOOLEAN", "布尔值"),
    DATE("DATE", "日期"),
    DATETIME("DATETIME", "日期时间"),
    JSON("JSON", "JSON字符串"),
    JSON_OBJECT("JSON_OBJECT", "JSON对象"),
    ARRAY("ARRAY", "数组"),
    
    // 编程语言类型（用于代码编辑器）
    TEXT("TEXT", "Text"),
    XML("XML", "XML"),
    HTML("HTML", "HTML"),
    CSS("CSS", "CSS"),
    JAVASCRIPT("JAVASCRIPT", "JavaScript"),
    TYPESCRIPT("TYPESCRIPT", "TypeScript"),
    JAVA("JAVA", "Java"),
    KOTLIN("KOTLIN", "Kotlin"),
    SQL("SQL", "SQL"),
    YAML("YAML", "YAML");

    companion object {
        fun fromCode(code: String): DataTypeEnum? {
            return entries.find { it.code == code }
        }
    }
}
