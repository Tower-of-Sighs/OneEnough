package com.flechazo.oneenoughfluid.client.gui.cache;

import com.mafuyu404.oneenoughitem.client.gui.cache.AbstractGlobalReplacementCache;

public class GlobalFluidReplacementCache extends AbstractGlobalReplacementCache {
    private static final GlobalFluidReplacementCache INSTANCE = new GlobalFluidReplacementCache();

    private GlobalFluidReplacementCache() {
        super("oef", "oef");
    }

    @Override
    protected String domainId() {
        return "oef";
    }

    public static AbstractGlobalReplacementCache get() {
        return INSTANCE;
    }

}