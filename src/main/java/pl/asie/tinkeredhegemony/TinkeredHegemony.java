/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
 *
 * This file is part of Tinkered Hegemony.
 *
 * Tinkered Hegemony is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Tinkered Hegemony is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Tinkered Hegemony.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.tinkeredhegemony;

import com.google.common.collect.Sets;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.item.*;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.Logger;
import slimeknights.tconstruct.tools.TinkerTools;
import slimeknights.tconstruct.tools.harvest.TinkerHarvestTools;
import slimeknights.tconstruct.tools.melee.TinkerMeleeWeapons;
import slimeknights.tconstruct.tools.melee.item.BroadSword;
import slimeknights.tconstruct.tools.ranged.TinkerRangedWeapons;
import slimeknights.tconstruct.tools.ranged.item.ShortBow;
import slimeknights.tconstruct.tools.tools.Hatchet;
import slimeknights.tconstruct.tools.tools.Mattock;
import slimeknights.tconstruct.tools.tools.Pickaxe;
import slimeknights.tconstruct.tools.tools.Shovel;

import java.util.*;

@Mod(modid = "tinkeredhegemony", name = "Tinkered Hegemony", dependencies = "required-after:tconstruct", version = "@VERSION@", updateJSON = "http://asie.pl/files/minecraft/update/tinkeredhegemony.json")
public class TinkeredHegemony {
    public static Logger logger;
    private static Configuration config;

    protected static final TObjectIntMap<Item> originalDurabilities = new TObjectIntHashMap<>();
    protected static final Set<DisabledItemClass> classMap = new HashSet<>();
    protected static final Set<Item> itemSet = new HashSet<>();

    private boolean performRecipeReplacement, performRecipePatchwork, performItemDisabling, performItemNeutralizing;

    private void addIfConfigured(DisabledItemClass disabledItemClass, boolean def) {
        if (config.getBoolean("disable", disabledItemClass.getName(), def, "")) {
            classMap.add(disabledItemClass);
        }
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        config = new Configuration(event.getSuggestedConfigurationFile());

        performItemDisabling = config.getBoolean("performItemDisabling", "general", true, "Should vanilla item recipes be removed?");
        performItemNeutralizing = config.getBoolean("performItemNeutralizing", "general", true, "Should vanilla tool items be effectively nullified (maximum damage set to 1)?");
        performRecipePatchwork = config.getBoolean("performRecipePatchwork", "general", true, "Should Tinkers' Construct items be capable of replacing vanilla equivalents in recipes?");
        performRecipeReplacement = config.getBoolean("performRecipeReplacement", "general", true, "Should ingredients be replaced in compatible recipes? This will primarily affect recipe guides.");

        if (config.hasChanged()) {
            config.save();
        }

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRegisterRecipe(RegistryEvent.Register<IRecipe> event) {
        if (performRecipePatchwork) {
            event.getRegistry().register(new RecipeTAPatchwork().setRegistryName("tinkeredhegemony:replacement"));
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        addIfConfigured(new DisabledItemClass("pickaxe", (i) -> i instanceof ItemPickaxe, TinkerHarvestTools.pickaxe, Pickaxe.class, TinkerTools.pickHead), true);
        addIfConfigured(new DisabledItemClass("axe", (i) -> i instanceof ItemAxe, TinkerHarvestTools.hatchet, Hatchet.class, TinkerTools.axeHead), true);
        addIfConfigured(new DisabledItemClass("sword", (i) -> i instanceof ItemSword, TinkerMeleeWeapons.broadSword, BroadSword.class, TinkerTools.swordBlade), true);
        addIfConfigured(new DisabledItemClass("hoe", (i) -> i instanceof ItemHoe, TinkerHarvestTools.mattock, Mattock.class, TinkerTools.axeHead, TinkerTools.shovelHead), true);
        addIfConfigured(new DisabledItemClass("shovel", (i) -> i instanceof ItemSpade, TinkerHarvestTools.shovel, Shovel.class, TinkerTools.shovelHead), true);
        addIfConfigured(new DisabledItemClass("bow", (i) -> i instanceof ItemBow, TinkerRangedWeapons.shortBow, ShortBow.class, TinkerTools.bowLimb), true);

        MaterialMatcher.init(config);

        if (config.hasChanged()) {
            config.save();
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        itemSet.clear();

        for (Item i : ForgeRegistries.ITEMS) {
            Optional<DisabledItemClass> optc = classMap.stream().filter((c) -> c.getItemPredicate().test(i)).findFirst();
            if (optc.isPresent()) {
                if (config.getBoolean(i.getRegistryName().toString(), "disabledItems", true, "")) {
                    DisabledItemClass c = optc.get();
                    if (performItemNeutralizing) {
                        originalDurabilities.put(i, i.getMaxDamage(new ItemStack(i)));
                        i.setMaxDamage(1);
                    }
                    itemSet.add(i);
                }
            }
        }

        Set<Item> itemsNotified = Sets.newHashSet(itemSet);

        if (performItemDisabling) {
            Iterator<IRecipe> iterator = CraftingManager.REGISTRY.iterator();
            while (iterator.hasNext()) {
                IRecipe recipe = iterator.next();
                ItemStack output = recipe.getRecipeOutput();
                if (!output.isEmpty() && itemSet.contains(output.getItem())) {
                    ForgeRegistries.RECIPES.register(new RecipeDummy(recipe.getGroup()).setRegistryName(recipe.getRegistryName()));
                    logger.info("Disabled " + Item.REGISTRY.getNameForObject(output.getItem()).toString() + " (removed recipe)");
                    itemsNotified.remove(output.getItem());
                }
            }
        }

        if (performItemNeutralizing || performItemDisabling) {
            for (Item i : itemsNotified) {
                logger.info("Disabled " + Item.REGISTRY.getNameForObject(i).toString());
            }
        }

        for (DisabledItemClass c : classMap) {
            c.updateItemSet(itemSet);
        }

        if (performRecipeReplacement) {
            new RecipeReplacement().process(ForgeRegistries.RECIPES);
        }

        if (config.hasChanged()) {
            config.save();
        }
    }
}
