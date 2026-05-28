package net.sopepsi.api.plugin;

/**
 * Default grant for a permission node when no explicit grant exists.
 */
public enum PermissionDefault {

	TRUE,
	FALSE,
	OP;

	public static PermissionDefault parse(String value) {
		if(value == null || value.length() == 0) {
			return OP;
		}
		String normalized = value.trim().toLowerCase();
		if("true".equals(normalized)) {
			return TRUE;
		}
		if("false".equals(normalized)) {
			return FALSE;
		}
		if("op".equals(normalized)) {
			return OP;
		}
		return OP;
	}
}
