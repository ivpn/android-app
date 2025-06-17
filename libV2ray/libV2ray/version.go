package libV2ray

import (
	"fmt"

	core "github.com/v2fly/v2ray-core/v5"
)

// CheckVersionX returns the library and v2fly versions
func CheckVersionX() string {
	var version = 31
	return fmt.Sprintf("Lib v%d, V2fly-core v%s", version, core.Version())
}
