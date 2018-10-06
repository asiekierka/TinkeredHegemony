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
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tinkering.TinkersItem;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TinkerReflectionWrapper {
	private static final Map<Class, Method> rcMap = new HashMap<>();

	public static List<PartMaterialType> getRequiredComponents(Item i) {
		if (i instanceof TinkersItem) {
			return ((TinkersItem) i).getRequiredComponents();
		} else {
			Method m = rcMap.computeIfAbsent(i.getClass(), (c) -> {
				try {
					//noinspection unchecked
					return c.getMethod("getRequiredComponents");
				} catch (NoSuchMethodException e) {
					TinkeredHegemony.logger.warn("Could not find getRequiredComponents method for class " + i.getClass() + "! This is a bug!");
					return null;
				}
			});

			if (m != null) {
				try {
					//noinspection unchecked
					return (List<PartMaterialType>) m.invoke(i);
				} catch (Exception e) {
					e.printStackTrace();
					return Collections.emptyList();
				}
			} else {
				return Collections.emptyList();
			}
		}
	}
}
