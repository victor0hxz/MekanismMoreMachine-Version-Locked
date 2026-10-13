package com.jerry.mekmm.test;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
public class TierInstallerSmoke {
 public static void run(ServerLevel level) {
  var player=FakePlayerFactory.getMinecraft(level);
  int index=0;
  for(String name:new String[]{"recycler","cnc_stamper","chemical_oxidizer","chemical_washer","chemical_dissolution_chamber","chemical_crystallizer","isotopic_centrifuge","pressurized_reaction_chamber","nutritional_liquifier","pigment_extractor","painting_machine"}) {
   String ns=(name.equals("recycler")||name.equals("cnc_stamper"))?"mekmm":"mekanism";
   var pos=new BlockPos(160,150,index++*5);level.removeBlock(pos,false);
   var block=BuiltInRegistries.BLOCK.getValue(Identifier.parse(ns+":"+name));level.setBlockAndUpdate(pos,block.defaultBlockState());
   player.getInventory().clearContent();
   var beforeTile=(mekanism.common.tile.base.TileEntityMekanism)level.getBlockEntity(pos);
   beforeTile.getEnergyContainer().setEnergy(123,null);
   for(var tank:beforeTile.getChemicalTanks())tank.setContents(mekanism.api.chemical.ChemicalResource.of(mekanism.common.registries.MekanismChemicals.HYDROGEN),17,null);
   for(var tank:beforeTile.getFluidTanks())tank.setContents(net.neoforged.neoforge.transfer.fluid.FluidResource.of(net.minecraft.world.level.material.Fluids.WATER),23,null);
   long chemicalBefore=beforeTile.getChemicalTanks().stream().mapToLong(tank->tank.amountAsLong()).sum();
   long fluidBefore=beforeTile.getFluidTanks().stream().mapToLong(tank->tank.amountAsLong()).sum();
   var seeded=beforeTile.getInventorySlots().getFirst();
   try(var tx=net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()){seeded.setContents(net.neoforged.neoforge.transfer.item.ItemResource.of(net.minecraft.world.item.Items.DIAMOND),3,tx);tx.commit();}
   var installer=BuiltInRegistries.ITEM.getValue(Identifier.parse("mekanism:basic_tier_installer"));
   player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(installer,2));
   var result=installer.useOn(new UseOnContext(player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false)));
   MoreMachineSmoke.check(level.getBlockState(pos).getBlock()!=block,name+" upgrades with tier installer ("+result+")");
   MoreMachineSmoke.check(player.getMainHandItem().getCount()==1,name+" consumes exactly one installer");
   var afterTile=(mekanism.common.tile.base.TileEntityMekanism)level.getBlockEntity(pos);
   MoreMachineSmoke.check(afterTile.getEnergyContainer().getAmountAsLong()==123,name+" preserves stored energy during installation");
   MoreMachineSmoke.check(afterTile.getChemicalTanks().stream().mapToLong(tank->tank.amountAsLong()).sum()==chemicalBefore,name+" preserves stored chemicals");
   MoreMachineSmoke.check(afterTile.getFluidTanks().stream().mapToLong(tank->tank.amountAsLong()).sum()==fluidBefore,name+" preserves stored fluids");
   long diamonds=afterTile.getInventorySlots().stream().filter(slot->slot.resource().value()==net.minecraft.world.item.Items.DIAMOND).mapToLong(slot->slot.amountAsLong()).sum();
   for(int slot=0;slot<player.getInventory().getContainerSize();slot++){var item=player.getInventory().getItem(slot);if(item.is(net.minecraft.world.item.Items.DIAMOND))diamonds+=item.getCount();}
   MoreMachineSmoke.check(diamonds==3,name+" preserves inventory during installation");
   for(String next:new String[]{"advanced","elite","ultimate"}) {
    var nextInstaller=BuiltInRegistries.ITEM.getValue(Identifier.parse("mekanism:"+next+"_tier_installer"));
    player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(nextInstaller,2));
    var old=level.getBlockState(pos).getBlock();nextInstaller.useOn(new UseOnContext(player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false)));
    MoreMachineSmoke.check(level.getBlockState(pos).getBlock()!=old && player.getMainHandItem().getCount()==1,name+" upgrades to "+next);
   }
  }
 }
}
