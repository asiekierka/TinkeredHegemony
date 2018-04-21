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

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import slimeknights.tconstruct.library.materials.Material;

import javax.annotation.Nullable;
import java.util.Collection;

public class RecipeReplacement {
	public RecipeReplacement() {

	}

	private void checkTo(Object to) {
		if (!(to instanceof Item || to instanceof ItemStack || to instanceof String)) {
			throw new RuntimeException("Invalid RecipeReplacement target type: " + to.getClass().getName() + "!");
		}
	}

	@Nullable
	private Ingredient replaceIngredient(Ingredient ing) {
		if (ing.getClass() == Ingredient.class) {
			ItemStack[] matchingStacks = ing.getMatchingStacks();
			if (matchingStacks.length == 1) {
				ItemStack s = matchingStacks[0];
				if (TinkeredHegemony.itemSet.contains(s.getItem())) {
					for (DisabledItemClass c : TinkeredHegemony.classMap) {
						if (c.getItemPredicate().test(s.getItem())) {
							Collection<Material> ms = MaterialMatcher.get(s.getItem());
							if (ms != null) {
								return c.createIngredient(ms);
							}
						}
					}
				}
			}
		}

		return null;
	}

	public void process(Iterable<IRecipe> registry) {
		for (IRecipe recipe : registry) {
			ResourceLocation recipeName = recipe.getRegistryName();
			boolean dirty = false;

			if (recipe instanceof ShapedRecipes || recipe instanceof ShapelessRecipes || recipe instanceof ShapedOreRecipe || recipe instanceof ShapelessOreRecipe) {
				NonNullList<Ingredient> ingredients = recipe.getIngredients();
				for (int i = 0; i < ingredients.size(); i++) {
					Ingredient ing = ingredients.get(i);
					Ingredient ingNew = replaceIngredient(ing);
					if (ingNew != null) {
						ingredients.set(i, ingNew);
						dirty = true;
					}
				}
			}

			if (dirty) {
				TinkeredHegemony.logger.info("Successfully edited " + recipeName + "!");
			}
		}
	}
}
