package net.momirealms.craftengine.bukkit.util;

import java.lang.reflect.Method;

/**
 * BTC-CORE performance integration hooks.
 * <p>
 * These helpers call into {@code dev.btc.core.api.BTCCoreAPI} via reflection so
 * that craft-engine does not need a hard compile-time dependency on BTC-CORE.
 * All lookups are cached after the first successful resolution; if BTC-CORE is
 * not installed every method silently degrades to a no-op that preserves the
 * original craft-engine behaviour.
 * <p>
 * <strong>Thread-safety:</strong> lookups are performed at most once behind a
 * double-checked lock; subsequent reads of the resolved targets race but always
 * observe either {@code null} (unavailable) or the same fully-published Method /
 * API instance (safe publication is guaranteed by the {@code volatile} flags).
 */
final class BtcCoreHook {
    private BtcCoreHook() {}

    private static volatile boolean initialised = false;
    private static volatile Object apiInstance = null;
    private static volatile Method shouldCalculateCollision = null;

    private static void init() {
        if (initialised) return;
        synchronized (BtcCoreHook.class) {
            if (initialised) return;
            try {
                Class<?> apiClass = Class.forName("dev.btc.core.api.BTCCoreAPI");
                Object instance = apiClass.getMethod("instance").invoke(null);
                try {
                    shouldCalculateCollision = apiClass.getMethod(
                        "shouldCalculateCollision",
                        org.bukkit.entity.Entity.class,
                        int.class
                    );
                } catch (NoSuchMethodException ignored) {
                    /* Older BTCCoreAPI without the performance hook — leave null */
                }
                apiInstance = instance;
            } catch (Throwable ignored) {
                /* BTCCoreAPI not on the classpath (or instance() threw) — keep nulls */
            }
            initialised = true;
        }
    }

    /**
     * BTC-CORE P11 — Collision throttle hook.
     * <p>
     * Returns {@code true} when collision calculations should proceed for the
     * current tick, {@code false} when BTC-CORE has decided to throttle the work
     * (typically because the surrounding region is densely packed with entities
     * or the server is under sustained tick-time pressure).
     * <p>
     * The {@code entity} parameter of the underlying API is historical — the BTC
     * implementation does not currently consult it — so we safely pass
     * {@code null} here. Only {@code nearbyEntityCount} participates in the
     * decision.
     *
     * @param nearbyEntityCount the number of candidate entities near the shape
     *                           currently being tested for collision
     * @return {@code true} to proceed with the collision check,
     *         {@code false} to skip it; always {@code true} when BTC-CORE is
     *         unavailable or the throttle is disabled
     */
    static boolean shouldCalculateCollision(int nearbyEntityCount) {
        if (!initialised) init();
        Object api = apiInstance;
        Method method = shouldCalculateCollision;
        if (api == null || method == null) return true;
        try {
            return (boolean) method.invoke(api, (Object) null, nearbyEntityCount);
        } catch (Throwable ignored) {
            return true;
        }
    }
}