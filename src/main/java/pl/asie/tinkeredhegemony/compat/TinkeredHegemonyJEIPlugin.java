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

package pl.asie.tinkeredhegemony.compat;

import com.google.common.collect.Lists;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.*;
import mezz.jei.plugins.vanilla.anvil.AnvilRecipeWrapper;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.OreDictionary;
import pl.asie.tinkeredhegemony.TinkeredHegemony;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@JEIPlugin
public class TinkeredHegemonyJEIPlugin implements IModPlugin {
	private IIngredientRegistry ingredientRegistry;
	private ArrayList<ItemStack> stacksToRemove;
	private boolean disableInputRecipes, disableOutputRecipes;

	@Override
	public void register(IModRegistry registry) {
		Configuration config = TinkeredHegemony.getConfig();

		disableInputRecipes = config.getBoolean("hideInputRecipes", "jei", false,
				"If hideDisabledItems is enabled, should recipes which take in the disabled tool as input also be disabled? (f.e. smelting)");

		disableOutputRecipes = config.getBoolean("hideOutputRecipes", "jei", true,
				"If hideDisabledItems is enabled, should recipes which take in the disabled tool as output also be disabled? (f.e. anvil)");

		if (config.getBoolean("hideDisabledItems", "jei", true, "Should disabled items be hidden in JEI?")) {
			IIngredientBlacklist blacklist = registry.getJeiHelpers().getIngredientBlacklist();
			ingredientRegistry = registry.getIngredientRegistry();
			stacksToRemove = new ArrayList<>();

			for (Item i : TinkeredHegemony.getDisabledItems()) {
				ItemStack wildcard = new ItemStack(i, 1, OreDictionary.WILDCARD_VALUE);
				blacklist.addIngredientToBlacklist(wildcard);
				stacksToRemove.add(wildcard);
			}
		}

		if (config.hasChanged()) {
			config.save();
		}
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		if (ingredientRegistry != null && stacksToRemove != null && !stacksToRemove.isEmpty() && (disableInputRecipes || disableOutputRecipes)) {
			for (IRecipeCategory category : Lists.newArrayList(jeiRuntime.getRecipeRegistry().getRecipeCategories())) {
				for (ItemStack stack : stacksToRemove) {
					if (disableOutputRecipes) {
						IFocus<ItemStack> focusOutput = jeiRuntime.getRecipeRegistry().createFocus(IFocus.Mode.OUTPUT, stack);

						try {
							for (Object wrapperObj : jeiRuntime.getRecipeRegistry().getRecipeWrappers(category, focusOutput)) {
								jeiRuntime.getRecipeRegistry().hideRecipe((IRecipeWrapper) wrapperObj, category.getUid());
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					if (disableInputRecipes) {
						IFocus<ItemStack> focusInput = jeiRuntime.getRecipeRegistry().createFocus(IFocus.Mode.INPUT, stack);

						try {
							for (Object wrapperObj : jeiRuntime.getRecipeRegistry().getRecipeWrappers(category, focusInput)) {
								jeiRuntime.getRecipeRegistry().hideRecipe((IRecipeWrapper) wrapperObj, category.getUid());
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

		ingredientRegistry = null;
		stacksToRemove = null;
	}
}
