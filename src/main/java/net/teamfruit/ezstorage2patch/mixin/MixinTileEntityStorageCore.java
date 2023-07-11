package net.teamfruit.ezstorage2patch.mixin;

import com.zerofall.ezstorage.tileentity.TileEntityBase;
import com.zerofall.ezstorage.tileentity.TileEntityStorageCore;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TileEntityStorageCore.class)
public abstract class MixinTileEntityStorageCore extends TileEntityBase {

    @Redirect(method = "updateInventory", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/network/simpleimpl/SimpleNetworkWrapper;sendToDimension(Lnet/minecraftforge/fml/common/network/simpleimpl/IMessage;I)V", remap = false), remap = false)
    private void redirectUpdateInventorySendToDimension(SimpleNetworkWrapper snw, IMessage message, int dimensionId) {
        if (!this.world.isRemote) {
            snw.sendToDimension(message, dimensionId);
        }
    }
}
