package net.pier.geoe;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.FrameType;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class AdvancementBuilder {


    private final Advancement.Builder builder = Advancement.Builder.advancement();
    private final String name;
    private final ItemStack displayItem;

    private FrameType frameType = FrameType.GOAL;

    private ResourceLocation background;

    private boolean showToast = true,announceToChat = true, hidden = false;


    private AdvancementBuilder(String name, ItemLike itemLike)
    {
        this.name = name;
        this.displayItem = new ItemStack(itemLike);
    }

    public static AdvancementBuilder root(String bg, Supplier<? extends ItemLike> icon)
    {
        return new AdvancementBuilder("root", icon.get()).background(new ResourceLocation(Geothermal.MODID, "textures/"+bg+".png"));
    }

    public static AdvancementBuilder child(String name, Supplier<? extends ItemLike> icon, Advancement parent)
    {
        return new AdvancementBuilder(name, icon.get()).parent(parent);
    }

    public AdvancementBuilder parent(Advancement advancement)
    {
        this.builder.parent(advancement);
        return this;
    }

    public AdvancementBuilder background(ResourceLocation background)
    {
        this.background = background;
        return this;
    }

    public AdvancementBuilder frameType(FrameType frameType)
    {
        this.frameType = frameType;
        return this;
    }

    public AdvancementBuilder addCriterion(String pKey, CriterionTriggerInstance pCriterion)
    {
        this.builder.addCriterion(pKey, pCriterion);
        return this;
    }

    public AdvancementBuilder hidden(boolean hidden)
    {
        this.hidden = hidden;
        return this;
    }


    public Advancement save(Consumer<Advancement> consumer, ExistingFileHelper existingFileHelper)
    {
        TranslatableComponent titleComponent = new TranslatableComponent("advancements.geoe." + this.name);
        TranslatableComponent descriptionComponent = new TranslatableComponent("advancements.geoe." +  this.name + ".description");

        return this.builder.display(this.displayItem, titleComponent, descriptionComponent, this.background, this.frameType, this.showToast, this.announceToChat, this.hidden)
                .save(consumer,new ResourceLocation(Geothermal.MODID, "main/" + this.name),existingFileHelper);
    }


}
