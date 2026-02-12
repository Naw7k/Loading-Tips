package net.naw.custom_loading_tips.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.naw.custom_loading_tips.Custom_loading_tips;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Mixin(LevelLoadingScreen.class)
public abstract class LoadingScreenWorldMixin extends Screen {
    @Unique private String displayTip;
    @Unique private int currentTipIndex = 0;
    @Unique private int appearanceTicks = 0;
    @Unique private float internalTime = 0.0f;
    @Unique private final Random random = new Random();
    @Unique private boolean wasMouseDown = false;
    @Unique private boolean wasRightMouseDown = false;
    @Unique private boolean wasMiddleMouseDown = false;
    @Unique private int copyFeedbackTicks = 0;

    @Unique private float tipX = -1;
    @Unique private float tipY = -1;
    @Unique private boolean isDragging = false;
    @Unique private double dragOffX, dragOffY;
    @Unique private double startMouseX, startMouseY;
    @Unique private float currentScale = 1.0f;

    @Unique private final Path posConfig = MinecraftClient.getInstance().runDirectory.toPath().resolve("config/loading_tips_pos.properties");
    @Unique private final Path seenConfig = MinecraftClient.getInstance().runDirectory.toPath().resolve("config/seen_tips.properties");
    @Unique private final Set<String> seenTipHashes = new HashSet<>();
    @Unique private int timeOnCurrentTip = 0;

    protected LoadingScreenWorldMixin(Text title) {
        super(title);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        this.loadAllData();
        this.createNewTip();
    }

    @Unique
    private void updateTipFromIndex() {
        if (!Custom_loading_tips.LOADING_TIPS.isEmpty()) {
            if (currentTipIndex >= Custom_loading_tips.LOADING_TIPS.size()) currentTipIndex = 0;
            if (currentTipIndex < 0) currentTipIndex = Custom_loading_tips.LOADING_TIPS.size() - 1;
            this.displayTip = formatTip(Custom_loading_tips.LOADING_TIPS.get(currentTipIndex));
            this.appearanceTicks = 0;
            this.timeOnCurrentTip = 0;
        }
    }

    @Unique
    private String formatTip(String original) {
        String t = original;
        String[] rainbow = {"§c", "§6", "§e", "§a", "§b", "§9", "§d"};
        String rainbowColor = rainbow[(int)(internalTime * 2.5f) % rainbow.length];

        t = t.replaceAll("(?i)Mace", "§f§lMace§r");
        t = t.replaceAll("(?i)Wind Charge(s)?", "§bWind Charge$1§r");
        t = t.replaceAll("(?i)Heavy Core(s)?", "§dHeavy Core$1§r");
        t = t.replaceAll("(?i)Breeze", "§bBreeze§r");
        t = t.replaceAll("(?i)Diamond(s)?", "§bDiamond$1§r");
        t = t.replaceAll("(?i)Emerald(s)?", "§2Emerald$1§r");
        t = t.replaceAll("(?i)Netherite", "§5Netherite§r");
        t = t.replaceAll("(?i)Gold(en)?", "§6Gold$1§r");
        t = t.replaceAll("(?i)Copper", "§6Copper§r");
        t = t.replaceAll("(?i)TNT", "§c§lTNT§r");
        t = t.replaceAll("(?i)Nether\\b", "§4Nether§r");
        t = t.replaceAll("(?i)The End\\b", "§dThe End§r");
        t = t.replaceAll("(?i)Warden", "§3§lWarden§r");
        t = t.replaceAll("(?i)Sculk", "§3Sculk§r");
        t = t.replaceAll("(?i)Enchanted Golden Apple(s)?", rainbowColor + "§lEnchanted Golden Apple$1§r");
        t = t.replaceAll("(?i)Redstone", "§cRedstone§r");
        t = t.replaceAll("(?i)Crafter(s)?", "§eCrafter$1§r");
        return t;
    }

    @Unique
    private void createNewTip() {
        if (!Custom_loading_tips.LOADING_TIPS.isEmpty()) {
            this.currentTipIndex = random.nextInt(Custom_loading_tips.LOADING_TIPS.size());
            this.updateTipFromIndex();
        }
    }

    @Unique
    private void loadAllData() {
        try {
            if (Files.exists(posConfig)) {
                Properties p = new Properties();
                try (InputStream in = Files.newInputStream(posConfig)) { p.load(in); }
                tipX = Float.parseFloat(p.getProperty("tipX", "-1"));
                tipY = Float.parseFloat(p.getProperty("tipY", "-1"));
            }
            if (Files.exists(seenConfig)) {
                Properties s = new Properties();
                try (InputStream in = Files.newInputStream(seenConfig)) { s.load(in); }
                for (String key : s.stringPropertyNames()) { seenTipHashes.add(key); }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Unique
    private void saveAllData() {
        try {
            Properties p = new Properties();
            p.setProperty("tipX", String.valueOf(tipX));
            p.setProperty("tipY", String.valueOf(tipY));
            try (OutputStream out = Files.newOutputStream(posConfig)) { p.store(out, "Pos"); }
            Properties s = new Properties();
            for (String hash : seenTipHashes) { s.setProperty(hash, "true"); }
            try (OutputStream out = Files.newOutputStream(seenConfig)) { s.store(out, "Seen"); }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount > 0) currentTipIndex--;
        else if (verticalAmount < 0) currentTipIndex++;
        this.updateTipFromIndex();
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (appearanceTicks < 40) appearanceTicks++;
        if (copyFeedbackTicks > 0) copyFeedbackTicks--;
        internalTime += 0.03f;

        if (this.displayTip == null) return;

        String rawText = Custom_loading_tips.LOADING_TIPS.get(currentTipIndex);
        String tipHash = String.valueOf(rawText.hashCode());
        boolean isNew = !seenTipHashes.contains(tipHash);

        if (isNew) {
            timeOnCurrentTip++;
            if (timeOnCurrentTip > 100) { seenTipHashes.add(tipHash); saveAllData(); }
        }

        this.displayTip = formatTip(rawText);

        if (tipX == -1) { tipX = this.width / 2f; tipY = this.height - 80f; }

        int maxWidth = (int)(this.width * 0.7);
        List<OrderedText> wrappedLines = this.textRenderer.wrapLines(Text.literal(this.displayTip), maxWidth);
        int maxLineWidth = 0;
        for (OrderedText line : wrappedLines) maxLineWidth = Math.max(maxLineWidth, this.textRenderer.getWidth(line));

        int halfW = (maxLineWidth / 2) + 15;
        boolean isOver = (mouseX >= tipX - halfW && mouseX <= tipX + halfW) && (mouseY >= tipY - 25 && mouseY <= tipY + (wrappedLines.size() * 10) + 10);

        float targetScale = isOver ? 1.015f : 1.0f;
        currentScale = MathHelper.lerp(delta * 0.45f, currentScale, targetScale);

        long handle = MinecraftClient.getInstance().getWindow().getHandle();
        boolean isLeftDown = org.lwjgl.glfw.GLFW.glfwGetMouseButton(handle, 0) == 1;
        boolean isRightDown = org.lwjgl.glfw.GLFW.glfwGetMouseButton(handle, 1) == 1;
        boolean isMiddleDown = org.lwjgl.glfw.GLFW.glfwGetMouseButton(handle, 2) == 1;

        if (isMiddleDown && !wasMiddleMouseDown && isOver) {
            MinecraftClient.getInstance().keyboard.setClipboard(rawText);
            copyFeedbackTicks = 40;
            MinecraftClient.getInstance().getSoundManager().play(new PositionedSoundInstance(SoundEvents.ITEM_BUNDLE_DROP_CONTENTS, SoundCategory.PLAYERS, 0.1f, 1.0f, net.minecraft.util.math.random.Random.create(), 0,0,0));
        }
        if (isRightDown && !wasRightMouseDown && isOver) { tipX = this.width / 2f; tipY = this.height - 80f; saveAllData(); }

        // Dragging Logic
        if (isLeftDown) {
            if (!wasMouseDown && isOver) { isDragging = false; startMouseX = mouseX; startMouseY = mouseY; dragOffX = mouseX - tipX; dragOffY = mouseY - tipY; }
            if (Math.abs(mouseX - startMouseX) > 3 || Math.abs(mouseY - startMouseY) > 3) {
                if (isOver || isDragging) { isDragging = true; tipX = MathHelper.clamp((float)(mouseX - dragOffX), halfW, this.width - halfW); tipY = MathHelper.clamp((float)(mouseY - dragOffY), 25, this.height - 40); }
            }
        } else if (wasMouseDown) { if (isDragging) saveAllData(); else if (isOver) createNewTip(); isDragging = false; }
        wasMouseDown = isLeftDown; wasRightMouseDown = isRightDown; wasMiddleMouseDown = isMiddleDown;

        float entranceOpacity = appearanceTicks / 40.0f;
        int tipAlpha = (int)(entranceOpacity * 255);

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(tipX, tipY - 10);
        context.getMatrices().scale(currentScale, currentScale);
        context.getMatrices().translate(-tipX, -(tipY - 10));

        // --- THE LOGIC TREE ---
        String headerTitle = "Did you know?";
        ItemStack iconStack = new ItemStack(Items.BOOK);
        String tipL = rawText.toLowerCase();

        // 1.21 & Trial Chambers
        if (tipL.contains("mace") || tipL.contains("trial") || tipL.contains("breeze") || tipL.contains("vault") || tipL.contains("heavy core")) {
            headerTitle = "1.21 Update:"; iconStack = new ItemStack(Items.TRIAL_KEY);
        }
        // Copper & Metallurgy (Specifically for your list)
        else if (tipL.contains("copper") || tipL.contains("oxidation") || tipL.contains("lightning rod") || tipL.contains("smelt")) {
            headerTitle = "Metallurgy:"; iconStack = new ItemStack(Items.COPPER_INGOT);
        }
        // Tools & Mining
        else if (tipL.contains("pickaxe") || tipL.contains("axe") || tipL.contains("shovel") || tipL.contains("silk touch") || tipL.contains("fortune") || tipL.contains("diamond") || tipL.contains("iron") || tipL.contains("gold tools")) {
            headerTitle = "Master Miner:"; iconStack = new ItemStack(Items.DIAMOND_PICKAXE);
        }
        // Redstone & Engineering (Hoppers, Comparators, Dispensers, etc.)
        else if (tipL.contains("redstone") || tipL.contains("hopper") || tipL.contains("comparator") || tipL.contains("dispenser") || tipL.contains("dropper") || tipL.contains("observer") || tipL.contains("crafter") || tipL.contains("piston")) {
            headerTitle = "Engineering:"; iconStack = new ItemStack(Items.REDSTONE);
        }
        // Workstations (Anvils, Grindstones, Stonecutters, etc.)
        else if (tipL.contains("anvil") || tipL.contains("grindstone") || tipL.contains("stonecutter") || tipL.contains("blast furnace") || tipL.contains("smoker") || tipL.contains("barrel")) {
            headerTitle = "Workstation:"; iconStack = new ItemStack(Items.ANVIL);
        }
        // Deep Dark
        else if (tipL.contains("warden") || tipL.contains("sculk") || tipL.contains("echo") || tipL.contains("ancient city")) {
            headerTitle = "Deep Dark:"; iconStack = new ItemStack(Items.RECOVERY_COMPASS);
        }
        // The End
        else if (tipL.contains("end") || tipL.contains("dragon") || tipL.contains("elytra") || tipL.contains("shulker")) {
            headerTitle = "The End:"; iconStack = new ItemStack(Items.ENDER_EYE);
        }
        // The Nether
        else if (tipL.contains("nether") || tipL.contains("piglin") || tipL.contains("ghast") || tipL.contains("soul sand") || tipL.contains("blaze")) {
            headerTitle = "Nether Guide:"; iconStack = new ItemStack(Items.NETHERRACK);
        }
        // Dangerous Mobs / TNT
        else if (tipL.contains("tnt") || tipL.contains("explode") || tipL.contains("creeper") || tipL.contains("guardian") || tipL.contains("watch out")) {
            headerTitle = "Watch Out!:"; iconStack = new ItemStack(Items.TNT);
        }
        // Villagers & Trading
        else if (tipL.contains("village") || tipL.contains("emerald") || tipL.contains("trade") || tipL.contains("villager") || tipL.contains("lectern")) {
            headerTitle = "Economics:"; iconStack = new ItemStack(Items.EMERALD);
        }
        // Animal Husbandry (Bees, Wolves, Pigs, Sheep, etc.)
        else if (tipL.contains("wolf") || tipL.contains("cat") || tipL.contains("dog") || tipL.contains("pig") || tipL.contains("sheep") || tipL.contains("bee") || tipL.contains("axolotl")) {
            headerTitle = "Husbandry:"; iconStack = new ItemStack(Items.LEAD);
        }
        // Aquatic / Travel
        else if (tipL.contains("ocean") || tipL.contains("fish") || tipL.contains("water") || tipL.contains("boat") || tipL.contains("blue ice") || tipL.contains("riptide")) {
            headerTitle = "Wayfarer:"; iconStack = new ItemStack(Items.PRISMARINE_SHARD);
        }
        // Farming / Plants
        else if (tipL.contains("honey") || tipL.contains("slime") || tipL.contains("seed") || tipL.contains("sand") || tipL.contains("glass") || tipL.contains("amethyst")) {
            headerTitle = "Botany:"; iconStack = new ItemStack(Items.MOSS_BLOCK);
        }
        // Combat
        else if (tipL.contains("sword") || tipL.contains("bow") || tipL.contains("damage") || tipL.contains("combat") || tipL.contains("looting") || tipL.contains("fire aspect")) {
            headerTitle = "Combat Tip:"; iconStack = new ItemStack(Items.DIAMOND_SWORD);
        }
        // Music & Sound
        else if (tipL.contains("note block") || tipL.contains("jukebox") || tipL.contains("music disc")) {
            headerTitle = "Musician:"; iconStack = new ItemStack(Items.MUSIC_DISC_PIGSTEP);
        }
        // Survival Basics
        else if (tipL.contains("dig") || tipL.contains("crouch") || tipL.contains("fall") || tipL.contains("shield") || tipL.contains("torch") || tipL.contains("sprinting") || tipL.contains("hungry")) {
            headerTitle = "101 Survival:"; iconStack = new ItemStack(Items.IRON_CHESTPLATE);
        }

        int hW = this.textRenderer.getWidth("§l" + headerTitle);
        int iconX = (int)tipX - (hW / 2) - 14;
        int iconY = (int)tipY - 11;

        // --- THE STAR INDICATOR ---
        if (isNew) {
            float pulse = (MathHelper.sin(internalTime * 6f) + 1f) / 2f;
            int starC = (tipAlpha << 24) | (255 << 16) | ((int)(200 * pulse + 55) << 8) | 50;
            context.getMatrices().pushMatrix();
            context.getMatrices().translate(iconX - 3, iconY - 7);
            context.getMatrices().scale(0.75f, 0.75f);
            context.drawText(this.textRenderer, "*", 0, 0, starC, true);
            context.getMatrices().popMatrix();
        }

        // Draw Icon
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(iconX, iconY);
        context.getMatrices().scale(entranceOpacity, entranceOpacity);
        context.drawItem(iconStack, -8, -8);
        context.getMatrices().popMatrix();

        // Draw Header
        context.drawText(this.textRenderer, "§l" + headerTitle, (int)tipX - (hW / 2), (int)tipY - 15, (tipAlpha << 24) | 0xFFFFFF, false);

        // Draw Tip ID on hover
        if (isOver) {
            String ghostID = "§7#" + (currentTipIndex + 1);
            context.drawText(this.textRenderer, ghostID, (int)tipX + (hW / 2) + 4, (int)tipY - 15, (tipAlpha << 24) | 0xAAAAAA, false);
        }

        context.getMatrices().popMatrix();

        // Copy Feedback
        if (copyFeedbackTicks > 0) {
            String copyText = "§a§oCopied to Clipboard!";
            context.drawText(this.textRenderer, copyText, (int)tipX - (this.textRenderer.getWidth(copyText) / 2), (int)tipY - 30, (255 << 24) | 0x55FF55, false);
        }

        // Render the actual Wrapped Tip Lines
        int currentY = (int)tipY;
        for (OrderedText line : wrappedLines) {
            context.drawText(this.textRenderer, line, (int)tipX - (this.textRenderer.getWidth(line) / 2), currentY, (tipAlpha << 24) | 0xFFFFFF, false);
            currentY += 10;
        }
    }
}