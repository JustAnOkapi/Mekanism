package mekanism.client.render.obj;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometryPart;
import net.minecraftforge.client.model.geometry.IMultipartModelGeometry;
import net.minecraftforge.client.model.obj.OBJModel;

public class TransmitterModel implements IMultipartModelGeometry<TransmitterModel> {

    private final OBJModel internal;
    @Nullable
    private final OBJModel glass;

    public TransmitterModel(OBJModel internalModel, @Nullable OBJModel glass) {
        this.internal = internalModel;
        this.glass = glass;
    }

    @Override
    public Collection<? extends IModelGeometryPart> getParts() {
        return internal.getParts();
    }

    @Override
    public Optional<? extends IModelGeometryPart> getPart(String name) {
        return internal.getPart(name);
    }

    @Override
    public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform,
          ItemOverrides overrides, ResourceLocation modelLocation) {
        return new TransmitterBakedModel(internal, glass, owner, bakery, spriteGetter, modelTransform, overrides, modelLocation);
    }

    @Override
    public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        Set<Material> combined = Sets.newHashSet();
        IModelConfiguration configuration = new OpaqueModelConfiguration(owner);
        for (IModelGeometryPart part : getParts()) {
            combined.addAll(part.getTextures(owner, modelGetter, missingTextureErrors));
            //Add the opaque versions of the textures as well
            combined.addAll(part.getTextures(configuration, modelGetter, missingTextureErrors));
        }
        if (glass != null) {
            for (IModelGeometryPart part : glass.getParts()) {
                combined.addAll(part.getTextures(owner, modelGetter, missingTextureErrors));
                //Add the opaque versions of the textures as well
                combined.addAll(part.getTextures(configuration, modelGetter, missingTextureErrors));
            }
        }
        return combined;
    }
}