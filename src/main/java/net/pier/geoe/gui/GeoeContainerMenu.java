package net.pier.geoe.gui;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.PacketDistributor;
import net.pier.geoe.blockentity.multiblock.MultiBlockControllerEntity;
import net.pier.geoe.gui.data.DataContainerType;
import net.pier.geoe.gui.data.GenericProperty;
import net.pier.geoe.network.PacketContainerSync;
import net.pier.geoe.network.PacketManager;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

public abstract class GeoeContainerMenu<T extends BlockEntity> extends AbstractContainerMenu {


    private final Player player;
    private final T be;

    protected final List<GenericProperty<?>> properties = new LinkedList<>();

    public GeoeContainerMenu(MenuType<?> pMenuType, int pContainerId, Inventory pPlayerInventory, T entity) {
        super(pMenuType, pContainerId);
        this.be = entity;
        this.player = pPlayerInventory.player;
    }

    public T getBlockEntity() {
        return be;
    }

    @Override
    public boolean stillValid(@Nonnull Player pPlayer) {
        return !(this.getBlockEntity() instanceof MultiBlockControllerEntity<?> controller) || controller.isComplete();
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();


        if(player instanceof ServerPlayer serverPlayer)
        {
            List<Pair<Integer, DataContainerType.DataPair<?>>> toSync = new LinkedList<>();
            for(int i = 0; i < this.properties.size(); i++)
            {
                GenericProperty<?> data = this.properties.get(i);
                if(data.isDirty())
                    toSync.add(Pair.of(i, data.dataPair()));
            }
            if(!toSync.isEmpty())
                PacketManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new PacketContainerSync(toSync));


            /*
            List<ISyncProperty> dirtyProperties = new LinkedList<>();
            for (ISyncProperty syncProperty : controller.syncProperties) {
                if(syncProperty.getSyncContext() == SyncContext.GUI)
                    dirtyProperties.add(syncProperty);
            }


             */
        }
    }

    public void receiveSync(List<Pair<Integer, DataContainerType.DataPair<?>>> synced)
    {
        for(Pair<Integer, DataContainerType.DataPair<?>> syncElement : synced)
            this.properties.get(syncElement.getFirst()).processSync(syncElement.getSecond().data());

    }
}
