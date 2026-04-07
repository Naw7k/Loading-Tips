package net.naw.loading_tips;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

// --- TIP CATEGORY LOGIC ---
// Determines the header title and icon based on tip content keywords.
// Also handles keyword colorization for tip text.
public class TipCategory {

    public record Category(String title, ItemStack icon) {}

    // --- CATEGORY DETECTION ---
    // Matches tip keywords to a category header and Minecraft item icon.
    // ORDER MATTERS — more specific categories must come before general ones.
    public static Category getCategory(String tipLower) {
        try {
            // --- TINY TAKEOVER / 26.1 (most specific, check first) ---
            if (tipLower.contains("golden dandelion") || tipLower.contains("tiny takeover")
                    || tipLower.contains("baby mob") || tipLower.contains("trumpet")
                    || tipLower.contains("age lock") || tipLower.contains("polar bear")
                    || tipLower.contains("baby mobs") || tipLower.contains("26.1")) {
                return new Category("§6§lTiny Takeover 26.1:", new ItemStack(Items.DANDELION));
            }

            // --- TRICKY TRIALS / 1.21 ---
            if (tipLower.contains("mace") || tipLower.contains("trial") || tipLower.contains("breeze")
                    || tipLower.contains("vault") || tipLower.contains("heavy core") || tipLower.contains("trial chamber")) {
                return new Category("1.21 Update:", new ItemStack(Items.TRIAL_KEY));
            }

            // --- DEEP DARK (before Nether/End to avoid false matches) ---
            if (tipLower.contains("warden") || tipLower.contains("sculk") || tipLower.contains("echo shard")
                    || tipLower.contains("ancient city") || tipLower.contains("deep dark")) {
                return new Category("Deep Dark:", new ItemStack(Items.RECOVERY_COMPASS));
            }

            // --- NETHER ---
            if (tipLower.contains("nether") || tipLower.contains("piglin") || tipLower.contains("ghast")
                    || tipLower.contains("soul sand") || tipLower.contains("blaze") || tipLower.contains("strider")
                    || tipLower.contains("magma") || tipLower.contains("bastion")) {
                return new Category("Nether Guide:", new ItemStack(Items.NETHERRACK));
            }

            // --- THE END (use "the end" to avoid matching words like "blend", "extend") ---
            if (tipLower.contains("the end") || tipLower.contains("dragon") || tipLower.contains("elytra")
                    || tipLower.contains("shulker") || tipLower.contains("end portal") || tipLower.contains("end city")
                    || tipLower.contains("chorus") || tipLower.contains("ender pearl") || tipLower.contains("enderman")
                    || tipLower.contains("end ship") || tipLower.contains("eyes of ender")) {
                return new Category("The End:", new ItemStack(Items.ENDER_EYE));
            }

            // --- BREWING & POTIONS ---
            if (tipLower.contains("potion") || tipLower.contains("brewing") || tipLower.contains("fermented")
                    || tipLower.contains("blaze powder") || tipLower.contains("splash") || tipLower.contains("lingering")
                    || tipLower.contains("cauldron") || tipLower.contains("beacon")) {
                return new Category("Brewing:", new ItemStack(Items.BREWING_STAND));
            }

            // --- ENCHANTING ---
            if (tipLower.contains("enchant") || tipLower.contains("enchantment") || tipLower.contains("lapis")
                    || tipLower.contains("mending") || tipLower.contains("unbreaking") || tipLower.contains("looting")
                    || tipLower.contains("silk touch") || tipLower.contains("fortune") || tipLower.contains("fire aspect")
                    || tipLower.contains("sharpness") || tipLower.contains("efficiency")) {
                return new Category("Enchanting:", new ItemStack(Items.ENCHANTING_TABLE));
            }

            // --- REDSTONE / ENGINEERING ---
            if (tipLower.contains("redstone") || tipLower.contains("hopper") || tipLower.contains("comparator")
                    || tipLower.contains("dispenser") || tipLower.contains("dropper") || tipLower.contains("observer")
                    || tipLower.contains("crafter") || tipLower.contains("piston") || tipLower.contains("repeater")
                    || tipLower.contains("daylight detector") || tipLower.contains("lever") || tipLower.contains("tripwire")) {
                return new Category("Engineering:", new ItemStack(Items.REDSTONE));
            }

            // --- WORKSTATION ---
            if (tipLower.contains("anvil") || tipLower.contains("grindstone") || tipLower.contains("stonecutter")
                    || tipLower.contains("blast furnace") || tipLower.contains("smoker") || tipLower.contains("barrel")
                    || tipLower.contains("loom") || tipLower.contains("cartography") || tipLower.contains("fletching")) {
                return new Category("Workstation:", new ItemStack(Items.ANVIL));
            }

            // --- METALLURGY ---
            // "smelt" removed to avoid "Smelt sand to get glass" false match
            // "ore" uses word boundary to avoid "more", "before" etc
            if (tipLower.contains("copper") || tipLower.contains("oxidation") || tipLower.contains("lightning rod")
                    || tipLower.contains("netherite") || tipLower.contains("alloy")
                    || tipLower.contains("ingot") || tipLower.matches(".*\\bore\\b.*")
                    || tipLower.contains("furnace") || tipLower.contains("smelt ore")
                    || tipLower.contains("ancient debris") || tipLower.contains("raw iron")
                    || tipLower.contains("raw copper") || tipLower.contains("raw gold")) {
                return new Category("Metallurgy:", new ItemStack(Items.COPPER_INGOT));
            }

            // --- MINING ---
            if (tipLower.contains("pickaxe") || tipLower.contains("shovel") || tipLower.contains("mining")
                    || tipLower.contains("cave") || tipLower.contains("deepslate") || tipLower.contains("y-level")
                    || tipLower.contains("strip mine") || tipLower.contains("branch mine") || tipLower.contains("vein")) {
                return new Category("Master Miner:", new ItemStack(Items.DIAMOND_PICKAXE));
            }

            // --- COMBAT ---
            if (tipLower.contains("sword") || tipLower.contains("bow") || tipLower.contains("crossbow")
                    || tipLower.contains("damage") || tipLower.contains("combat") || tipLower.contains("attack speed")
                    || tipLower.contains("critical hit") || tipLower.contains("knockback") || tipLower.contains("axe")
                    || tipLower.contains("shield")) {
                return new Category("Combat Tip:", new ItemStack(Items.DIAMOND_SWORD));
            }

            // --- WATCH OUT ---
            if (tipLower.contains("tnt") || tipLower.contains("explode") || tipLower.contains("creeper")
                    || tipLower.contains("guardian") || tipLower.contains("watch out") || tipLower.contains("dangerous")
                    || tipLower.contains("hostile") || tipLower.contains("spider") || tipLower.contains("skeleton")
                    || tipLower.contains("zombie") || tipLower.contains("witch") || tipLower.contains("phantom")) {
                return new Category("Watch Out!:", new ItemStack(Items.TNT));
            }

            // --- ECONOMICS / VILLAGES ---
            if (tipLower.contains("emerald") || tipLower.contains("trade") || tipLower.contains("villager")
                    || tipLower.contains("village") || tipLower.contains("lectern") || tipLower.contains("librarian")
                    || tipLower.contains("wandering trader") || tipLower.contains("reputation")) {
                return new Category("Economics:", new ItemStack(Items.EMERALD));
            }

            // --- HUSBANDRY / ANIMALS ---
            // "lead" changed to "with a lead" to avoid false matches on "lead you to" etc
            if (tipLower.contains("wolf") || tipLower.contains("cat") || tipLower.contains("dog")
                    || tipLower.contains("pig") || tipLower.contains("sheep") || tipLower.contains("bee")
                    || tipLower.contains("axolotl") || tipLower.contains("horse") || tipLower.contains("cow")
                    || tipLower.contains("chicken") || tipLower.contains("rabbit") || tipLower.contains("breed")
                    || tipLower.contains("tame") || tipLower.contains("saddle") || tipLower.contains("with a lead")
                    || tipLower.contains("leads can") || tipLower.contains("tie mobs")) {
                return new Category("Husbandry:", new ItemStack(Items.LEAD));
            }

            // --- WAYFARER / OCEAN ---
            if (tipLower.contains("ocean") || tipLower.contains("fish") || tipLower.contains("fishing")
                    || tipLower.contains("boat") || tipLower.contains("blue ice") || tipLower.contains("riptide")
                    || tipLower.contains("dolphin") || tipLower.contains("shipwreck") || tipLower.contains("coral")
                    || tipLower.contains("treasure") || tipLower.contains("trident")) {
                return new Category("Wayfarer:", new ItemStack(Items.PRISMARINE_SHARD));
            }

            // --- BOTANY / NATURE ---
            // "glass" removed — "Smelt sand to get glass" should be 101 Survival not Botany
            // "seed" kept but fine since most seed tips are genuinely about farming
            if (tipLower.contains("honey") || tipLower.contains("slime") || tipLower.contains("seed")
                    || tipLower.contains("amethyst") || tipLower.contains("flower")
                    || tipLower.contains("crop") || tipLower.contains("farm") || tipLower.contains("compost")
                    || tipLower.contains("moss") || tipLower.contains("bamboo") || tipLower.contains("mushroom")
                    || tipLower.contains("sapling") || tipLower.contains("tree") || tipLower.contains("leaf")) {
                return new Category("Botany:", new ItemStack(Items.MOSS_BLOCK));
            }

            // --- MUSICIAN ---
            if (tipLower.contains("note block") || tipLower.contains("jukebox") || tipLower.contains("music disc")
                    || tipLower.contains("music") || tipLower.contains("goat horn")
                    || tipLower.contains("copper horn")) {
                return new Category("Musician:", new ItemStack(Items.MUSIC_DISC_PIGSTEP));
            }

            // --- 101 SURVIVAL ---
            if (tipLower.contains("torch") || tipLower.contains("sprinting") || tipLower.contains("hungry")
                    || tipLower.contains("food") || tipLower.contains("sleep") || tipLower.contains("bed")
                    || tipLower.contains("respawn") || tipLower.contains("inventory") || tipLower.contains("crafting")
                    || tipLower.contains("recipe") || tipLower.contains("chest") || tipLower.contains("storage")
                    || tipLower.contains("map") || tipLower.contains("compass") || tipLower.contains("spawn")
                    || tipLower.contains("smelt") || tipLower.contains("glass") || tipLower.contains("bucket")
                    || tipLower.contains("sand") || tipLower.contains("gravel") || tipLower.contains("fall")
                    || tipLower.contains("name tag")) {
                return new Category("101 Survival:", new ItemStack(Items.IRON_CHESTPLATE));
            }

            // --- DEFAULT FALLBACK ---
            return new Category("Did you know?", new ItemStack(Items.BOOK));
        } catch (Exception e) {
            return new Category("Did you know?", ItemStack.EMPTY);
        }
    }

    // --- TIP COLORIZER ---
    // Colorizes known Minecraft keywords in the tip text using Minecraft formatting codes.
    // internalTime is passed in to animate the rainbow color for Enchanted Golden Apple.
    public static String formatTip(String original, float internalTime) {
        String t = original;
        String[] rainbow = {"§c", "§6", "§e", "§a", "§b", "§9", "§d"};
        String rainbowColor = rainbow[(int)(internalTime * 2.5f) % rainbow.length];

        // --- SPECIFIC MULTI-WORD PHRASES FIRST (before single words to avoid partial replacement) ---
        t = t.replaceAll("(?i)Enchanted Golden Apple(s)?", rainbowColor + "§lEnchanted Golden Apple$1§r");
        t = t.replaceAll("(?i)Wind Charge(s)?", "§bWind Charge$1§r");
        t = t.replaceAll("(?i)Heavy Core(s)?", "§dHeavy Core$1§r");
        t = t.replaceAll("(?i)The End\\b", "§dThe End§r");
        t = t.replaceAll("(?i)Soul Sand", "§5Soul Sand§r");

        // --- SINGLE WORDS (after multi-word to avoid breaking them) ---
        t = t.replaceAll("(?i)Netherite\\b", "§5Netherite§r");
        t = t.replaceAll("(?i)Nether\\b", "§4Nether§r");
        t = t.replaceAll("(?i)Mace\\b", "§f§lMace§r");
        t = t.replaceAll("(?i)Breeze\\b", "§bBreeze§r");
        t = t.replaceAll("(?i)Warden\\b", "§3§lWarden§r");
        t = t.replaceAll("(?i)Sculk\\b", "§3Sculk§r");
        t = t.replaceAll("(?i)Diamond(s)?\\b", "§bDiamond$1§r");
        t = t.replaceAll("(?i)Emerald(s)?\\b", "§2Emerald$1§r");
        t = t.replaceAll("(?i)Gold(en)?\\b", "§6Gold$1§r");
        t = t.replaceAll("(?i)Copper\\b", "§6Copper§r");
        t = t.replaceAll("(?i)TNT\\b", "§c§lTNT§r");
        t = t.replaceAll("(?i)Redstone\\b", "§cRedstone§r");
        t = t.replaceAll("(?i)Crafter(s)?\\b", "§eCrafter$1§r");
        t = t.replaceAll("(?i)Elytra\\b", "§dElytra§r");
        t = t.replaceAll("(?i)Trident\\b", "§bTrident§r");
        t = t.replaceAll("(?i)Beacon\\b", "§eBeacon§r");
        t = t.replaceAll("(?i)Amethyst\\b", "§5Amethyst§r");
        t = t.replaceAll("(?i)Lapis\\b", "§9Lapis§r");

        return t;
    }
}
