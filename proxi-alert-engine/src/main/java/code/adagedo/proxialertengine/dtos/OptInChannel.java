package code.adagedo.proxialertengine.dtos;

public enum OptInChannel {
    EMAIL,
    SMS,
    BOTH;
    public static boolean isValid(String name) {
        if (name == null) return false;
        for (OptInChannel channel : values()) {
            if (channel.name().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}