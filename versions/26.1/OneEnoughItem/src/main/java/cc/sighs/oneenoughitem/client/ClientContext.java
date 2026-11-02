package cc.sighs.oneenoughitem.client;

public final class ClientContext {
    private static final ThreadLocal<Integer> BUILD_DEPTH = ThreadLocal.withInitial(() -> 0);

    public static void beginBuilding() {
        BUILD_DEPTH.set(BUILD_DEPTH.get() + 1);
    }

    public static void endBuilding() {
        int depth = BUILD_DEPTH.get() - 1;
        if (depth <= 0) BUILD_DEPTH.remove();
        else BUILD_DEPTH.set(depth);
    }

    public static boolean isBuilding() {
        return BUILD_DEPTH.get() > 0;
    }
}
