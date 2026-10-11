package com.jerry.mekmm.test;
import com.jerry.mekmm.Mekmm;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
@EventBusSubscriber(modid="mekmm")
public class ServerGuiSmoke {
 static int ticks,connected; static net.minecraft.core.BlockPos guiPos;
 @SubscribeEvent public static void tick(ServerTickEvent.Post e) {
  if(!Boolean.getBoolean("port.clientIntegration"))return;
  ticks++;
  if(!e.getServer().getPlayerList().getPlayers().isEmpty()){
   connected++;
   for(var player:e.getServer().getPlayerList().getPlayers()) {
    String id=connected==200?"mekmm:recycler":connected==300?"mekmm:ultimate_recycling_factory":connected==400?"mekanism_extras:absolute_recycling_factory":null;
    if(id!=null){var level=player.level();var pos=player.blockPosition().offset(2,0,0);level.removeBlock(pos,false);level.setBlockAndUpdate(pos,BuiltInRegistries.BLOCK.getValue(Identifier.parse(id)).defaultBlockState());guiPos=pos;Mekmm.LOGGER.info("SERVER GUI PLACED: {}",id);}
    if(connected==240 || connected==340 || connected==440){((TileEntityMekanism)player.level().getBlockEntity(guiPos)).openGui(player);Mekmm.LOGGER.info("SERVER GUI OPEN: {}",guiPos);}
   }
   if(connected>600)e.getServer().halt(false);
  }
  if(ticks>3000)e.getServer().halt(false);
 }
}
