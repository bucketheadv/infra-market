package enums

// PostType POST请求类型枚举
type PostType string

const (
	PostTypeApplicationJSON               PostType = "application/json"
	PostTypeApplicationXWWWFormURLEncoded PostType = "application/x-www-form-urlencoded"
)

func (p PostType) Code() string {
	return string(p)
}

func PostTypeFromCode(code string) *PostType {
	types := map[string]PostType{
		"application/json":                  PostTypeApplicationJSON,
		"application/x-www-form-urlencoded": PostTypeApplicationXWWWFormURLEncoded,
	}
	if postType, ok := types[code]; ok {
		return &postType
	}
	return nil
}
