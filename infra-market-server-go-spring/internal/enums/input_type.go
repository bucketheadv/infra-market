package enums

// InputType 输入类型枚举
type InputType string

const (
	InputTypeTEXT        InputType = "TEXT"
	InputTypeSELECT      InputType = "SELECT"
	InputTypeMULTISELECT InputType = "MULTI_SELECT"
	InputTypeDATE        InputType = "DATE"
	InputTypeDATETIME    InputType = "DATETIME"
	InputTypeNUMBER      InputType = "NUMBER"
	InputTypeTEXTAREA    InputType = "TEXTAREA"
	InputTypeCODE        InputType = "CODE"
	InputTypePASSWORD    InputType = "PASSWORD"
	InputTypeEMAIL       InputType = "EMAIL"
	InputTypeURL         InputType = "URL"
)

func (i InputType) Code() string {
	return string(i)
}

func InputTypeFromCode(code string) *InputType {
	types := map[string]InputType{
		"TEXT":         InputTypeTEXT,
		"SELECT":       InputTypeSELECT,
		"MULTI_SELECT": InputTypeMULTISELECT,
		"DATE":         InputTypeDATE,
		"DATETIME":     InputTypeDATETIME,
		"NUMBER":       InputTypeNUMBER,
		"TEXTAREA":     InputTypeTEXTAREA,
		"CODE":         InputTypeCODE,
		"PASSWORD":     InputTypePASSWORD,
		"EMAIL":        InputTypeEMAIL,
		"URL":          InputTypeURL,
	}
	if inputType, ok := types[code]; ok {
		return &inputType
	}
	return nil
}
