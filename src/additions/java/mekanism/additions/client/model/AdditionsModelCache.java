package mekanism.additions.client.model;

import mekanism.additions.common.MekanismAdditions;
import mekanism.client.model.BaseModelCache;

import mekanism.client.model.BaseModelCache.JSONModelData;

public class AdditionsModelCache extends BaseModelCache {

    public static final AdditionsModelCache INSTANCE = new AdditionsModelCache();

    public final JSONModelData BALLOON = registerJSON(MekanismAdditions.rl("item/balloon_latched"));
    public final JSONModelData BALLOON_FREE = registerJSON(MekanismAdditions.rl("item/balloon_free"));
}
