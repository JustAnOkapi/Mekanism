package mekanism.client.model.baked;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.client.render.lib.QuadUtils;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.tile.qio.TileEntityQIODriveArray;
import mekanism.common.tile.qio.TileEntityQIODriveArray.DriveStatus;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.IModelData;

public class DriveArrayBakedModel extends ExtensionBakedModel<byte[]> {

    private static final float[][] DRIVE_PLACEMENTS = new float[][]{
          {0, 6F / 16}, {-2F / 16, 6F / 16}, {-4F / 16, 6F / 16}, {-7F / 16, 6F / 16}, {-9F / 16, 6F / 16}, {-11F / 16, 6F / 16},
          {0, 0}, {-2F / 16, 0}, {-4F / 16, 0}, {-7F / 16, 0}, {-9F / 16, 0}, {-11F / 16, 0}
    };

    public DriveArrayBakedModel(BakedModel original) {
        super(original);
    }

    @Override
    public List<BakedQuad> createQuads(QuadsKey<byte[]> key) {
        byte[] driveStatus = key.getData();
        List<BakedQuad> ret = key.getQuads();
        Direction side = key.getSide();
        BlockState blockState = key.getBlockState();
        if (blockState != null && side == Attribute.getFacing(blockState)) {
            ret = new ArrayList<>(ret);
            List<BakedQuad> driveQuads = new ArrayList<>();
            for (int i = 0; i < driveStatus.length; i++) {
                DriveStatus status = DriveStatus.STATUSES[driveStatus[i]];
                if (status != DriveStatus.NONE) {
                    driveQuads.addAll(getDriveQuads(i, status, key));
                }
            }
            ret.addAll(QuadUtils.transformBakedQuads(driveQuads, QuadTransformation.rotate(side)));
        }
        return ret;
    }

    private List<BakedQuad> getDriveQuads(int index, DriveStatus status, QuadsKey<byte[]> key) {
        List<BakedQuad> ret = MekanismModelCache.INSTANCE.QIO_DRIVES[status.ordinal()].getBakedModel().getQuads(key.getBlockState(), null, key.getRandom());
        float[] translation = DRIVE_PLACEMENTS[index];
        return QuadUtils.transformBakedQuads(ret, QuadTransformation.translate(new Vec3(translation[0], translation[1], 0)));
    }

    @Nullable
    @Override
    public QuadsKey<byte[]> createKey(QuadsKey<byte[]> key, IModelData data) {
        byte[] driveStatus = data.getData(TileEntityQIODriveArray.DRIVE_STATUS_PROPERTY);
        if (driveStatus == null) {
            return null;
        }
        return key.data(driveStatus, Arrays.hashCode(driveStatus), Arrays::equals);
    }

    @Override
    protected DriveArrayBakedModel wrapModel(BakedModel model) {
        return new DriveArrayBakedModel(model);
    }
}
