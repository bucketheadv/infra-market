package enums

// Status 状态枚举
type Status string

const (
	StatusActive   Status = "active"
	StatusInactive Status = "inactive"
	StatusDeleted  Status = "deleted"
)

func (s Status) Code() string {
	return string(s)
}

func StatusFromCode(code string) *Status {
	statuses := map[string]Status{
		"active":   StatusActive,
		"inactive": StatusInactive,
		"deleted":  StatusDeleted,
	}
	if status, ok := statuses[code]; ok {
		return &status
	}
	return nil
}
