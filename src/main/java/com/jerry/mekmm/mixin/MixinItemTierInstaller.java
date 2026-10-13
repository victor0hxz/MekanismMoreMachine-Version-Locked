package com.jerry.mekmm.mixin;
import java.util.*;
import mekanism.common.item.ItemTierInstaller;
import mekanism.common.block.BlockBounding;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.tile.base.TileEntityMekanism;
import com.jerry.mekaf.common.block.attribute.AttributeAdvancedFactoryType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Return container-item slots that the advanced factory does not expose. */
@Mixin(value=ItemTierInstaller.class,remap=false)
public class MixinItemTierInstaller {
 @Unique private static final ThreadLocal<Snapshot> mekmm$before=new ThreadLocal<>();
 @Unique private record Snapshot(BlockPos pos,TileEntityMekanism tile,Map<ItemResource,Long> contents){}
 @Unique private static Map<ItemResource,Long> mekmm$contents(TileEntityMekanism tile){
  var map=new HashMap<ItemResource,Long>();for(var slot:tile.getInventorySlots())if(!slot.isEmpty())map.merge(slot.resource(),slot.amountAsLong(),Long::sum);return map;
 }
 @Inject(method="useOn",at=@At("HEAD")) private void mekmm$capture(UseOnContext context,CallbackInfoReturnable<InteractionResult> ci){
  mekmm$before.remove();if(context.getLevel().isClientSide()||context.getPlayer()==null)return;
  var pos=context.getClickedPos();var world=context.getLevel();
  if(world.getBlockState(pos).is(mekanism.common.registries.MekanismBlocks.BOUNDING_BLOCK)){var main=BlockBounding.getMainBlockPos(world,pos);if(main!=null)pos=main;}
  if(Attribute.has(world.getBlockState(pos),AttributeAdvancedFactoryType.class)&&world.getBlockEntity(pos) instanceof TileEntityMekanism tile)mekmm$before.set(new Snapshot(pos,tile,mekmm$contents(tile)));
 }
 @Inject(method="useOn",at=@At("RETURN")) private void mekmm$returnContainers(UseOnContext context,CallbackInfoReturnable<InteractionResult> ci){
  var before=mekmm$before.get();mekmm$before.remove();if(before==null||!(ci.getReturnValue() instanceof InteractionResult.Success))return;
  if(!(context.getLevel().getBlockEntity(before.pos()) instanceof TileEntityMekanism after)||after==before.tile())return;
  var remaining=mekmm$contents(after);var player=context.getPlayer();
  for(var entry:before.contents().entrySet()){
   long missing=entry.getValue()-remaining.getOrDefault(entry.getKey(),0L);
   while(missing>0){int count=(int)Math.min(64,missing);var stack=entry.getKey().toStack(count);player.getInventory().add(stack);if(!stack.isEmpty())player.drop(stack,false);missing-=count;}
  }
 }
}
