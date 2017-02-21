package io.github.recodex.android.users;

/**
 * Created by martin on 2/21/17.
 */

public enum LoginType {
    REGULAR, CAS_UK, UNKNOWN;

    public static String typeToString(LoginType type) {
        switch (type) {
            case REGULAR:
                return "regular";
            case CAS_UK:
                return "cas-uk";
            default:
                return "unknown";
        }
    }

    public static LoginType stringToType(String type) {
        switch (type) {
            case "regular":
                return REGULAR;
            case "cas-uk":
                return CAS_UK;
            default:
                return UNKNOWN;
        }
    }
}
