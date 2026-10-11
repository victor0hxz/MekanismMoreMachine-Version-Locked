package com.jerry.mekmm.test;
import com.jerry.mekmm.Mekmm;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
@EventBusSubscriber(modid="mekmm",value=Dist.CLIENT)
public class ClientGuiSmoke {
 static int ticks;static java.util.Set<String> screens=new java.util.HashSet<>();
 @SubscribeEvent public static void tick(ClientTickEvent.Post e) {
  if(!Boolean.getBoolean("port.clientIntegration"))return;
  var mc=Minecraft.getInstance();ticks++;
  if(mc.level!=null && mc.screen!=null && mc.screen.getClass().getName().startsWith("com.jerry")) {
   if(screens.add(mc.screen.getClass().getName()))Mekmm.LOGGER.info("CLIENT GUI PASS: {}",mc.screen.getClass().getName());
   if(screens.size()>=3){Mekmm.LOGGER.info("CLIENT GUI FUNCTIONAL COMPLETE");mc.stop();}
  }
  if(ticks>2400){Mekmm.LOGGER.error("CLIENT GUI FUNCTIONAL FAILED: timeout; screens={}",screens);mc.stop();}
 }
}
