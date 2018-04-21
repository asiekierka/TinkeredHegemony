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

import com.google.common.collect.ImmutableSet;
import net.minecraft.item.Item;
import org.apache.commons.lang3.tuple.Pair;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tools.IToolPart;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;

public class DisabledItemClass {
	private final String name;
	private final Predicate<Item> itemPredicate;
	private final Item tconItem;
	private final Class<? extends Item> tconItemClass;
	private final Collection<IToolPart> partsMaterialMatch;

	public DisabledItemClass(String name, Predicate<Item> itemPredicate, Item tconItem, Class<? extends Item> tconItemClass, IToolPart... partsMaterialMatch) {
		this.name = name;
		this.itemPredicate = itemPredicate;
		this.tconItem = tconItem;
		this.tconItemClass = tconItemClass;
		this.partsMaterialMatch = ImmutableSet.copyOf(partsMaterialMatch);
	}

	public Collection<IToolPart> getPartsMaterialMatch() {
		return partsMaterialMatch;
	}

	public boolean tconItemMatches(Item i) {
		return tconItemClass.isAssignableFrom(i.getClass());
	}

	public Predicate<Item> getItemPredicate() {
		return itemPredicate;
	}

	public String getName() {
		return name;
	}

	public IngredientTinkerTool createIngredient(Material... materials) {
		return createIngredient(Arrays.asList(materials));
	}

	public IngredientTinkerTool createIngredient(Collection<Material> materials) {
		ImmutableSet.Builder<Pair<Material, IToolPart>> data = new ImmutableSet.Builder<>();
		for (Material m : materials) {
			for (IToolPart p : partsMaterialMatch) {
				data.add(Pair.of(m, p));
			}
		}
		return new IngredientTinkerTool(tconItem, tconItemClass, data.build());
	}
}
