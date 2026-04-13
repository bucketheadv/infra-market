package enums

// DataType 数据类型枚举
type DataType string

const (
	// 基础数据类型
	DataTypeSTRING     DataType = "STRING"
	DataTypeINTEGER    DataType = "INTEGER"
	DataTypeLONG       DataType = "LONG"
	DataTypeDOUBLE     DataType = "DOUBLE"
	DataTypeBOOLEAN    DataType = "BOOLEAN"
	DataTypeDATE       DataType = "DATE"
	DataTypeDATETIME   DataType = "DATETIME"
	DataTypeJSON       DataType = "JSON"
	DataTypeJSONObject DataType = "JSON_OBJECT"
	DataTypeARRAY      DataType = "ARRAY"

	// 编程语言类型（用于代码编辑器）
	DataTypeTEXT       DataType = "TEXT"
	DataTypeXML        DataType = "XML"
	DataTypeHTML       DataType = "HTML"
	DataTypeCSS        DataType = "CSS"
	DataTypeJAVASCRIPT DataType = "JAVASCRIPT"
	DataTypeTYPESCRIPT DataType = "TYPESCRIPT"
	DataTypeJAVA       DataType = "JAVA"
	DataTypeKOTLIN     DataType = "KOTLIN"
	DataTypeSQL        DataType = "SQL"
	DataTypeYAML       DataType = "YAML"
)

func (d DataType) Code() string {
	return string(d)
}

func DataTypeFromCode(code string) *DataType {
	types := map[string]DataType{
		"STRING":      DataTypeSTRING,
		"INTEGER":     DataTypeINTEGER,
		"LONG":        DataTypeLONG,
		"DOUBLE":      DataTypeDOUBLE,
		"BOOLEAN":     DataTypeBOOLEAN,
		"DATE":        DataTypeDATE,
		"DATETIME":    DataTypeDATETIME,
		"JSON":        DataTypeJSON,
		"JSON_OBJECT": DataTypeJSONObject,
		"ARRAY":       DataTypeARRAY,
		"TEXT":        DataTypeTEXT,
		"XML":         DataTypeXML,
		"HTML":        DataTypeHTML,
		"CSS":         DataTypeCSS,
		"JAVASCRIPT":  DataTypeJAVASCRIPT,
		"TYPESCRIPT":  DataTypeTYPESCRIPT,
		"JAVA":        DataTypeJAVA,
		"KOTLIN":      DataTypeKOTLIN,
		"SQL":         DataTypeSQL,
		"YAML":        DataTypeYAML,
	}
	if dataType, ok := types[code]; ok {
		return &dataType
	}
	return nil
}
