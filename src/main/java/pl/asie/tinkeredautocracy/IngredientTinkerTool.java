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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import org.apache.commons.lang3.tuple.Pair;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tinkering.TinkersItem;
import slimeknights.tconstruct.library.tools.IToolPart;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.TinkerUtil;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiPredicate;

public class IngredientTinkerTool extends Ingredient {
    private final Class<?> itemClass;
    private final Collection<Pair<Material, IToolPart>> requiredParts;

    protected IngredientTinkerTool(Item tconItem, Class itemClass, Collection<Pair<Material, IToolPart>> requiredParts) {
        super(getExampleStacks(tconItem, itemClass, requiredParts));
        this.itemClass = itemClass;
        this.requiredParts = requiredParts;
    }

    private static ItemStack[] getExampleStacks(Item i, Class itemClass, Collection<Pair<Material, IToolPart>> requiredParts) {
        NonNullList<ItemStack> stacks = NonNullList.create();
        i.getSubItems(CreativeTabs.SEARCH, stacks);
        return stacks.stream().filter((a) -> apply(a, itemClass, requiredParts)).toArray(ItemStack[]::new);
    }

    private static boolean apply(@Nullable ItemStack stack, Class<?> itemClass, Collection<Pair<Material, IToolPart>> requiredParts) {
        // I'm not touching TCon code again.
        if (stack == null || stack.isEmpty() || stack.getItemDamage() != 0) {
            return false;
        }

        if (stack.getItem() instanceof TinkersItem && itemClass.isAssignableFrom(stack.getItem().getClass()) && stack.hasTagCompound()) {
            List<Material> materials = TinkerUtil.getMaterialsFromTagList(TagUtil.getBaseMaterialsTagList(stack));
            List<PartMaterialType> components = ((TinkersItem) stack.getItem()).getRequiredComponents();

            Set<IToolPart> partsSeen = new HashSet<>();
            Set<IToolPart> partsFound = new HashSet<>();

            if (components.size() <= materials.size()) {
                Collection<Pair<Material, IToolPart>> rp = Sets.newHashSet(requiredParts);

                for (int i = 0; i < components.size(); i++) {
                    Material m = materials.get(i);
                    PartMaterialType c = components.get(i);
                    for (IToolPart part : c.getPossibleParts()) {
                        partsSeen.add(part);
                        if (rp.remove(Pair.of(m, part))) {
                            partsFound.add(part);
                        }
                    }
                }

                rp.removeIf(p -> !partsSeen.contains(p.getRight()) || partsFound.contains(p.getRight()));
                if (rp.isEmpty()) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean apply(@Nullable ItemStack stack) {
        return apply(stack, itemClass, requiredParts);
    }
}
