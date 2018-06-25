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
