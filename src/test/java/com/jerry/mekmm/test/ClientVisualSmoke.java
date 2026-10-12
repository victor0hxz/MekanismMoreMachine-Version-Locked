package com.jerry.mekmm.test;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.util.ArrayList;
import mekanism.client.model.blockstate.ConnectedGlassModel;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.lib.effect.BoltRenderer;
import mekanism.client.render.tileentity.RenderSPS;
import mekanism.common.lib.effect.BoltEffect;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = "mekmm", value = Dist.CLIENT)
public final class ClientVisualSmoke {
    private static int ticks;
    private static boolean done;

    static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        System.out.println("VISUAL PASS: " + label);
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("port.visualSmoke") || done || ++ticks < 100) return;
        var mc = Minecraft.getInstance();
        if (mc.getModelManager().getBlockStateModelSet() == null) return;
        done = true;
        try {
            var state = MekanismBlocks.STRUCTURAL_GLASS.get().defaultBlockState();
            var model = mc.getModelManager().getBlockStateModelSet().get(state);
            check(model instanceof ConnectedGlassModel, "connected glass custom model registered and baked");
            var reactorState = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.parse("mekanismgenerators:reactor_glass")).defaultBlockState();
            check(mc.getModelManager().getBlockStateModelSet().get(reactorState) instanceof ConnectedGlassModel, "reactor glass custom model registered and baked");
            var neighbors = new java.util.HashMap<BlockPos, net.minecraft.world.level.block.state.BlockState>();
            var air = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
            var level = (BlockAndTintGetter) Proxy.newProxyInstance(ClientVisualSmoke.class.getClassLoader(), new Class<?>[]{BlockAndTintGetter.class},
                    (proxy, method, args) -> method.getName().equals("getBlockState") ? neighbors.getOrDefault(args[0], air) : null);
            var isolated = new ArrayList<BlockStateModelPart>();
            var joined = new ArrayList<BlockStateModelPart>();
            model.collectParts(level, BlockPos.ZERO, state, RandomSource.create(0), isolated);
            neighbors.put(BlockPos.ZERO.relative(Direction.EAST), state);
            model.collectParts(level, BlockPos.ZERO, state, RandomSource.create(0), joined);
            check(isolated.size() == 6 && joined.size() == 6, "glass keeps all six faces");
            int changed = 0;
            for (int i = 0; i < 6; i++) if (isolated.get(i) != joined.get(i)) changed++;
            check(changed == 4, "adjacent glass removes the seam on four exposed planes");

            var renderState = new RenderSPS.SPSRenderState();
            renderState.blockPos = BlockPos.ZERO;
            renderState.center = Vec3.ZERO;
            renderState.setProcessed(1);
            renderState.gameTime = 101;
            renderState.partialTick = 0;
            renderState.bolts = new BoltRenderer();
            renderState.bolts.update("test", new BoltEffect(BoltEffect.BoltRenderInfo.ELECTRICITY, Vec3.ZERO, new Vec3(2, 2, 2), 15)
                    .size(0.1F).lifespan(8).spawn(BoltEffect.SpawnFunction.NO_DELAY), 100, 0);
            int[] vertices = new int[2];
            int[] channel = new int[1];
            var vertex = (VertexConsumer) Proxy.newProxyInstance(ClientVisualSmoke.class.getClassLoader(), new Class<?>[]{VertexConsumer.class}, (proxy, method, args) -> {
                if (method.isDefault()) return InvocationHandler.invokeDefault(proxy, method, args);
                if (method.getName().equals("addVertex")) vertices[channel[0]]++;
                return method.getReturnType() == void.class ? null : proxy;
            });
            var collector = (SubmitNodeCollector) Proxy.newProxyInstance(ClientVisualSmoke.class.getClassLoader(), new Class<?>[]{SubmitNodeCollector.class}, (proxy, method, args) -> {
                if (method.getName().equals("submitCustomGeometry")) {
                    channel[0] = args[1] == MekanismRenderType.MEK_LIGHTNING ? 0 : 1;
                    ((SubmitNodeCollector.CustomGeometryRenderer) args[2]).render(((PoseStack) args[0]).last(), vertex);
                }
                return null;
            });
            new RenderSPS(null).submit(renderState, new PoseStack(), collector, new CameraRenderState());
            check(vertices[0] > 0, "SPS submits actual lightning vertices");
            check(vertices[1] == 4, "SPS submits core billboard vertices");
            System.out.println("VISUAL FUNCTIONAL COMPLETE");
        } catch (Throwable ex) {
            System.out.println("VISUAL FUNCTIONAL FAILED");
            ex.printStackTrace();
        } finally {
            mc.stop();
        }
    }
}
