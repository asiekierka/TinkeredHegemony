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

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.OreDictionary;
import pl.asie.tinkeredhegemony.TinkeredHegemony;

@JEIPlugin
public class TinkeredHegemonyJEIPlugin implements IModPlugin {
	@Override
	public void register(IModRegistry registry) {
		Configuration config = TinkeredHegemony.getConfig();

		if (config.getBoolean("hideDisabledItems", "jei", true, "Should disabled items be hidden in JEI?")) {
			IIngredientBlacklist blacklist = registry.getJeiHelpers().getIngredientBlacklist();

			for (Item i : TinkeredHegemony.getDisabledItems()) {
				blacklist.addIngredientToBlacklist(new ItemStack(i, 1, OreDictionary.WILDCARD_VALUE));
			}
		}

		if (config.hasChanged()) {
			config.save();
		}
	}
}
