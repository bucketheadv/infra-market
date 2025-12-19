package enums

// HttpMethod HTTP请求方法枚举
type HttpMethod string

const (
	HttpMethodGET     HttpMethod = "GET"
	HttpMethodPOST    HttpMethod = "POST"
	HttpMethodPUT     HttpMethod = "PUT"
	HttpMethodDELETE  HttpMethod = "DELETE"
	HttpMethodPATCH   HttpMethod = "PATCH"
	HttpMethodHEAD    HttpMethod = "HEAD"
	HttpMethodOPTIONS HttpMethod = "OPTIONS"
)

func (m HttpMethod) Code() string {
	return string(m)
}

func HttpMethodFromCode(code string) *HttpMethod {
	methods := map[string]HttpMethod{
		"GET":     HttpMethodGET,
		"POST":    HttpMethodPOST,
		"PUT":     HttpMethodPUT,
		"DELETE":  HttpMethodDELETE,
		"PATCH":   HttpMethodPATCH,
		"HEAD":    HttpMethodHEAD,
		"OPTIONS": HttpMethodOPTIONS,
	}
	if method, ok := methods[code]; ok {
		return &method
	}
	return nil
}
