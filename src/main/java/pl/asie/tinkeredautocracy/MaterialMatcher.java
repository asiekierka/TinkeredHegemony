/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
 *
 * This file is part of TinkeredAutocracy.
 *
 * TinkeredAutocracy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TinkeredAutocracy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TinkeredAutocracy.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.tinkeredautocracy;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import slimeknights.mantle.util.RecipeMatch;
import slimeknights.mantle.util.RecipeMatchRegistry;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.tools.TinkerMaterials;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class MaterialMatcher {
	private static final Map<Item, Collection<Material>> materialMap = new HashMap<>();

	private static void add(Item i, Material... materials) {
		materialMap.put(i, ImmutableSet.copyOf(materials));
	}

	private static Material get(Configuration config, String key, String name) {
		name = config.getString(key, "materialMap", name, "");
		for (Material m : TinkerMaterials.materials) {
			if (m.identifier.equals(name)) {
				return m;
			}
		}

		TinkeredAutocracy.logger.warn("Could not find material '"+name+"'!");
		return null;
	}

	public static void init(Configuration config) {
		materialMap.clear();

		Material woodLike = get(config, "wood", "wood");
		Material stoneLike = get(config, "stone", "stone");
		Material ironLike = get(config, "iron", "iron");
		Material goldLike = get(config, "gold", "cobalt");
		Material diamondLike = get(config, "diamond", "manyullyn");

		add(Items.WOODEN_AXE, woodLike);
		add(Items.WOODEN_HOE, woodLike);
		add(Items.WOODEN_PICKAXE, woodLike);
		add(Items.WOODEN_SHOVEL, woodLike);
		add(Items.WOODEN_SWORD, woodLike);
		add(Items.BOW, woodLike);

		add(Items.STONE_AXE, stoneLike);
		add(Items.STONE_HOE, stoneLike);
		add(Items.STONE_PICKAXE, stoneLike);
		add(Items.STONE_SHOVEL, stoneLike);
		add(Items.STONE_SWORD, stoneLike);

		add(Items.IRON_AXE, ironLike);
		add(Items.IRON_HOE, ironLike);
		add(Items.IRON_PICKAXE, ironLike);
		add(Items.IRON_SHOVEL, ironLike);
		add(Items.IRON_SWORD, ironLike);

		add(Items.GOLDEN_AXE, goldLike);
		add(Items.GOLDEN_HOE, goldLike);
		add(Items.GOLDEN_PICKAXE, goldLike);
		add(Items.GOLDEN_SHOVEL, goldLike);
		add(Items.GOLDEN_SWORD, goldLike);

		add(Items.DIAMOND_AXE, diamondLike);
		add(Items.DIAMOND_HOE, diamondLike);
		add(Items.DIAMOND_PICKAXE, diamondLike);
		add(Items.DIAMOND_SHOVEL, diamondLike);
		add(Items.DIAMOND_SWORD, diamondLike);
	}

	@Nullable
	public static Collection<Material> get(Item i) {
		return materialMap.computeIfAbsent(i, (item) -> {
			ItemStack[] ingredients = ForgeRegistries.RECIPES.getValuesCollection().stream()
					.filter((r) -> r.getRecipeOutput().getItem() == item)
					.flatMap((r) -> r.getIngredients().stream())
					.flatMap((r) -> Arrays.stream(r.getMatchingStacks()))
					.filter((r) -> !r.isEmpty())
					.toArray(ItemStack[]::new);

			// let's try to heuristically determine!
			List<Material> mats = new ArrayList<>();

			for (Material m : TinkerMaterials.materials) {
				Optional<RecipeMatch.Match> match = m.matches(ingredients);
				if (match.isPresent()) {
					mats.add(m);
				}
			}

			if (!mats.isEmpty()) {
				return mats;
			}

			TinkeredAutocracy.logger.warn("Could not find material for item " + i.getRegistryName() + "!");
			return null;
		});
	}
}
