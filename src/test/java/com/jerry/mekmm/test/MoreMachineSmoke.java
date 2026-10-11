package com.jerry.mekmm.test;
import com.jerry.mekmm.Mekmm;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
@EventBusSubscriber(modid="mekmm")
public class MoreMachineSmoke {
 static void check(boolean ok,String label){if(!ok)throw new AssertionError(label);Mekmm.LOGGER.info("MOREMACHINE PASS: {}",label);}
 @SubscribeEvent public static void start(ServerStartedEvent e){
  if(!Boolean.getBoolean("port.moreMachineSmoke"))return;
  try {
   var level=e.getServer().overworld();int count=0,extras=0;
   for(var id:BuiltInRegistries.BLOCK.keySet().stream().sorted().toList()) {
    if(!id.getNamespace().equals("mekmm") && !(id.getNamespace().equals("mekanism_extras") && id.getPath().endsWith("_factory")))continue;
    var pos=new BlockPos((count%16)*4,150,(count/16)*4);var block=BuiltInRegistries.BLOCK.getValue(id);
    level.removeBlock(pos,false);level.setBlockAndUpdate(pos,block.defaultBlockState());var tile=level.getBlockEntity(pos);
    if(tile instanceof TileEntityMekanism mek){for(int i=0;i<3;i++)TileEntityMekanism.tickServer(level,pos,mek.getBlockState(),mek);count++;if(id.getNamespace().equals("mekanism_extras"))extras++;}
   }
   check(count>50,"machine and factory tiles initialize and tick: "+count);check(extras>20,"Extras factories initialize alongside MoreMachine: "+extras);
   for(String id:new String[]{"mekmm:recycler","mekmm:basic_recycling_factory","mekmm:ultimate_recycling_factory","mekanism_extras:absolute_recycling_factory"}) {
    var block=BuiltInRegistries.BLOCK.getValue(Identifier.parse(id));var pos=new BlockPos(100,150,count++*4);level.removeBlock(pos,false);level.setBlockAndUpdate(pos,block.defaultBlockState());
    var tile=(TileEntityMekanism)level.getBlockEntity(pos);check(tile!=null,id+" exists");
    try(var tx=Transaction.openRoot()){tile.getInventorySlots().getFirst().setContents(ItemResource.of(Items.DIAMOND),1,tx);tile.getEnergyContainer().setEnergy(tile.getEnergyContainer().getCapacityAsLong(),tx);tx.commit();}
    for(int t=0;t<500;t++){tile.getEnergyContainer().setEnergy(tile.getEnergyContainer().getCapacityAsLong(),null);TileEntityMekanism.tickServer(level,pos,tile.getBlockState(),tile);}
    var scrap=BuiltInRegistries.ITEM.getValue(Identifier.parse("mekmm:scrap"));int output=0;for(var slot:tile.getInventorySlots())if(slot.resource().value()==scrap)output+=slot.amountAsInt();
    check(output==1,id+" produces exactly one scrap");check(tile.getInventorySlots().stream().noneMatch(slot->slot.resource().value()==Items.DIAMOND && slot.amountAsInt()>0),id+" consumes input once");
   }
   var slot=mekanism.common.inventory.slot.OutputInventorySlot.at(null,0,0);
   var noDrop=new com.jerry.mekmm.api.recipes.RecyclerRecipe.ChanceOutput(){
    public net.minecraft.world.item.ItemStack getMaxChanceOutput(){return new net.minecraft.world.item.ItemStack(Items.DIAMOND);}
    public net.minecraft.world.item.ItemStack getChanceOutput(){return net.minecraft.world.item.ItemStack.EMPTY;}
    public net.minecraft.world.item.ItemStack nextChanceOutput(){return net.minecraft.world.item.ItemStack.EMPTY;}
   };
   try(var tx=Transaction.openRoot()) {
    check(com.jerry.mekmm.api.recipes.outputs.MoreMachineOutputHelper.getOutputHandler(slot,mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE).handleOutput(noDrop,1,tx),"unsuccessful chance roll still completes its paid operation");
    check(slot.isEmpty(),"unsuccessful chance roll creates no item");
   }
   Mekmm.LOGGER.info("MOREMACHINE FUNCTIONAL COMPLETE");
  }catch(Throwable t){Mekmm.LOGGER.error("MOREMACHINE FUNCTIONAL FAILED",t);}finally{if(!Boolean.getBoolean("port.clientIntegration"))e.getServer().halt(false);}
 }
}
