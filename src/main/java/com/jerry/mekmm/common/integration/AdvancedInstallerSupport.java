package com.jerry.mekmm.common.integration;
import com.jerry.mekaf.common.registries.AdvancedFactoryBlocks;
import com.jerry.mekaf.common.content.blocktype.AdvancedFactoryType;
import com.jerry.mekaf.common.block.attribute.AttributeAdvancedFactoryType;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeUpgradeable;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.tier.FactoryTier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
/** Add upgrade attributes by registry identity, without fragile clinit ordinals. */
public final class AdvancedInstallerSupport {
 public static void initialize() {
  String[] machines={"chemical_oxidizer","chemical_dissolution_chamber","chemical_washer","chemical_crystallizer","isotopic_centrifuge","pressurized_reaction_chamber","nutritional_liquifier","pigment_extractor","painting_machine"};
  AdvancedFactoryType[] types={AdvancedFactoryType.OXIDIZING,AdvancedFactoryType.DISSOLVING,AdvancedFactoryType.WASHING,AdvancedFactoryType.CRYSTALLIZING,AdvancedFactoryType.CENTRIFUGING,AdvancedFactoryType.PRESSURISED_REACTING,AdvancedFactoryType.LIQUIFYING,AdvancedFactoryType.PIGMENT_EXTRACTING,AdvancedFactoryType.PAINTING};
  for(int i=0;i<machines.length;i++) {
   var block=BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("mekanism",machines[i]));
   if(!(block instanceof BlockTile<?,?> tile))throw new IllegalStateException("Missing machine "+machines[i]);
   var type=types[i];
   tile.getType().add(new AttributeAdvancedFactoryType(type),new AttributeUpgradeable(()->AdvancedFactoryBlocks.getAdvancedFactory(FactoryTier.BASIC,type)));
  }
 }
}

