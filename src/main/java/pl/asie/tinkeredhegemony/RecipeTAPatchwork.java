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

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tinkering.TinkersItem;
import slimeknights.tconstruct.library.tools.IToolPart;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.TinkerUtil;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecipeTAPatchwork extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
    @Nullable
    private InventoryCrafting getInvPatched(InventoryCrafting inv) {
        InventoryCrafting invPatched = null;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack s = inv.getStackInSlot(i);
            if (!s.isEmpty() && s.getItemDamage() == 0) {
                for (DisabledItemClass c : TinkeredHegemony.classMap) {
                    if (!c.tconItemMatches(s.getItem())) {
                        continue;
                    }

                    if (invPatched == null) {
                        invPatched = new InventoryCraftingPatched(inv.getWidth(), inv.getHeight());

                        for (int j = 0; j < inv.getSizeInventory(); j++) {
                            invPatched.setInventorySlotContents(j, inv.getStackInSlot(j));
                        }
                    }

                    // tinker item -> vanilla item
                    // we need to get all the materials we can check for
                    Set<Material> validMaterials = new HashSet<>();

                    {
                        List<Material> materials = TinkerUtil.getMaterialsFromTagList(TagUtil.getBaseMaterialsTagList(s));
                        List<PartMaterialType> components = ((TinkersItem) s.getItem()).getRequiredComponents();

                        if (components.size() <= materials.size()) {
                            for (int j = 0; j < components.size(); j++) {
                                Material m = materials.get(j);
                                PartMaterialType cmp = components.get(j);
                                for (IToolPart part : cmp.getPossibleParts()) {
                                    if (c.getPartsMaterialMatch().contains(part)) {
                                        validMaterials.add(m);
                                    }
                                }
                            }
                        }
                    }

                    validMaterials.removeIf((m) -> {
                       IngredientTinkerTool ing = c.createIngredient(m);
                       return !ing.apply(s);
                    });

                    Item replItem = null;

                    if (validMaterials.size() > 0) {
                        for (Item replacementItem : c.getItemSet()) {
                            Collection<Material> ms = MaterialMatcher.get(replacementItem);
                            if (ms != null && ms.containsAll(validMaterials)) {
                                replItem = replacementItem;
                                break;
                            }
                        }
                    }

                    if (replItem != null) {
                        invPatched.setInventorySlotContents(i, new ItemStack(replItem));
                    }
                }

            }
        }

        return invPatched;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        if (!(inv instanceof InventoryCraftingPatched)) {
            InventoryCrafting invPatched = getInvPatched(inv);
            if (invPatched != null) {
                return CraftingManager.findMatchingRecipe(invPatched, worldIn) != null;
            }
        }
        return false;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        if (!(inv instanceof InventoryCraftingPatched)) {
            InventoryCrafting invPatched = getInvPatched(inv);
            if (invPatched != null) {
                try {
                    return CraftingManager.findMatchingResult(invPatched, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        InventoryCrafting invPatched = getInvPatched(inv);
        InventoryCrafting invPatchedClone = new InventoryCraftingPatched(inv.getWidth(), inv.getHeight());
        for (int i = 0; i < invPatched.getSizeInventory(); i++) {
            ItemStack src = invPatched.getStackInSlot(i);
            invPatchedClone.setInventorySlotContents(i, src.isEmpty() ? ItemStack.EMPTY : src.copy());
        }
        try {
            IRecipe realRecipe = CraftingManager.findMatchingRecipe(invPatched, null);
            if (realRecipe != null) {
                NonNullList<ItemStack> stacks = realRecipe.getRemainingItems(invPatched);
                NonNullList<ItemStack> result = NonNullList.create();
                for (int i = 0; i < stacks.size(); i++) {
                    if (invPatched.getStackInSlot(i) == inv.getStackInSlot(i)) {
                        result.add(stacks.get(i));
                    } else {
                        // TODO?
                        result.add(ItemStack.EMPTY);
                    }
                }
                return result;
            } else {
                return net.minecraftforge.common.ForgeHooks.defaultRecipeGetRemainingItems(inv);
            }
        } catch (Exception e) {
            return net.minecraftforge.common.ForgeHooks.defaultRecipeGetRemainingItems(inv);
        }
    }

    @Override
    public boolean canFit(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Override
    public String getGroup() {
        return "pure madness";
    }

    @Override
    public boolean isDynamic() {
        return true;
    }
}
