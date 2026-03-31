package net.naw.loading_tips.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.naw.loading_tips.Loading_tips;
import net.naw.loading_tips.TipCategory;
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

    // --- RENDER POSITION DATA ---
    // Stored as percentages (0.0 - 1.0) to ensure the tip stays in the same
    // relative spot regardless of window size or GUI scale.
    @Unique private float tipPctX = -1;
    @Unique private float tipPctY = -1;
    @Unique private boolean isDragging = false;
    @Unique private double dragOffX, dragOffY;
    @Unique private double startMouseX, startMouseY;
    @Unique private float currentScale = 1.0f;

    // --- CONFIG PATHS ---
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

    // --- TIP LOGIC ---
    @Unique
    private void updateTipFromIndex() {
        if (!Loading_tips.LOADING_TIPS.isEmpty()) {
            // Bounds check for the tips list
            if (currentTipIndex >= Loading_tips.LOADING_TIPS.size()) currentTipIndex = 0;
            if (currentTipIndex < 0) currentTipIndex = Loading_tips.LOADING_TIPS.size() - 1;

            // Format and colorize the tip using TipCategory
            this.displayTip = TipCategory.formatTip(Loading_tips.LOADING_TIPS.get(currentTipIndex), internalTime);
            this.appearanceTicks = 0;
            this.timeOnCurrentTip = 0;
        }
    }

    @Unique
    private void createNewTip() {
        if (!Loading_tips.LOADING_TIPS.isEmpty()) {
            this.currentTipIndex = random.nextInt(Loading_tips.LOADING_TIPS.size());
            this.updateTipFromIndex();
        }
    }

    // --- DATA PERSISTENCE (IO) ---
    @Unique
    private void loadAllData() {
        try {
            // Load UI position percentages
            if (Files.exists(posConfig)) {
                Properties p = new Properties();
                try (InputStream in = Files.newInputStream(posConfig)) { p.load(in); }
                tipPctX = Float.parseFloat(p.getProperty("tipPctX", "-1"));
                tipPctY = Float.parseFloat(p.getProperty("tipPctY", "-1"));

                // Clamp loaded values to prevent the tip from being lost off-screen
                if (tipPctX != -1) tipPctX = MathHelper.clamp(tipPctX, 0.05f, 0.95f);
                if (tipPctY != -1) tipPctY = MathHelper.clamp(tipPctY, 0.05f, 0.95f);
            }
            // Load the list of tips the user has already seen
            if (Files.exists(seenConfig)) {
                Properties s = new Properties();
                try (InputStream in = Files.newInputStream(seenConfig)) { s.load(in); }
                seenTipHashes.addAll(s.stringPropertyNames());
            }
        } catch (Exception e) { Loading_tips.LOGGER.error("Failed to load loading_tips data", e); }
    }

    @Unique
    private void saveAllData() {
        try {
            // Save UI percentages to config
            Properties p = new Properties();
            p.setProperty("tipPctX", String.valueOf(tipPctX));
            p.setProperty("tipPctY", String.valueOf(tipPctY));
            try (OutputStream out = Files.newOutputStream(posConfig)) { p.store(out, "Loading Tips UI Position"); }

            // Save seen tips hashes
            Properties s = new Properties();
            for (String hash : seenTipHashes) { s.setProperty(hash, "true"); }
            try (OutputStream out = Files.newOutputStream(seenConfig)) { s.store(out, "Seen Tips History"); }
        } catch (Exception e) { Loading_tips.LOGGER.error("Failed to save loading_tips data", e); }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        // Resizing is handled automatically by the percentage-to-pixel conversion in render()
    }

    // Allow scrolling through tips with the mouse wheel
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
        internalTime += 0.03f; // Timer for rainbow/animated text effects

        if (this.displayTip == null) return;

        // --- SEEN TIP LOGIC ---
        String rawText = Loading_tips.LOADING_TIPS.get(currentTipIndex);
        String tipHash = String.valueOf(rawText.hashCode());
        boolean isNew = !seenTipHashes.contains(tipHash);

        if (isNew) {
            timeOnCurrentTip++;
            // Consider tip "seen" after it has been on screen for ~5 seconds (100 ticks)
            if (timeOnCurrentTip > 100) {
                seenTipHashes.add(tipHash);
                saveAllData();
            }
        }

        // --- DYNAMIC COORDINATE CALCULATION ---
        // Convert the stored percentages back into actual screen pixels
        float tipX = (tipPctX == -1) ? this.width / 2f : tipPctX * this.width;
        float tipY = (tipPctY == -1) ? this.height - 80f : tipPctY * this.height;

        // Wrap text to fit within 70% of the screen width
        int maxWidth = (int)(this.width * 0.7);
        List<OrderedText> wrappedLines = this.textRenderer.wrapLines(Text.literal(this.displayTip), maxWidth);
        int maxLineWidth = 0;
        for (OrderedText line : wrappedLines) maxLineWidth = Math.max(maxLineWidth, this.textRenderer.getWidth(line));

        // Interaction Box Calculation
        int halfW = (maxLineWidth / 2) + 15;
        int clampPad = halfW + 5;

        // Final Clamping to keep tip inside visible screen boundaries
        tipX = MathHelper.clamp(tipX, clampPad, this.width - clampPad);
        tipY = MathHelper.clamp(tipY, 25, this.height - 20);

        // Hover detection for scale animation and clicking
        boolean isOver = (mouseX >= tipX - halfW && mouseX <= tipX + halfW) &&
                (mouseY >= tipY - 25 && mouseY <= tipY + (wrappedLines.size() * 10) + 10);

        // Smoothly lerp scale for hover effect
        float targetScale = isOver ? 1.015f : 1.0f;
        currentScale = MathHelper.lerp(delta * 0.45f, currentScale, targetScale);

        // --- INPUT HANDLING ---
        long handle = MinecraftClient.getInstance().getWindow().getHandle();
        boolean isLeftDown = org.lwjgl.glfw.GLFW.glfwGetMouseButton(handle, 0) == 1;
        boolean isRightDown = org.lwjgl.glfw.GLFW.glfwGetMouseButton(handle, 1) == 1;
        boolean isMiddleDown = org.lwjgl.glfw.GLFW.glfwGetMouseButton(handle, 2) == 1;

        // Middle Click: Copy tip text to clipboard
        if (isMiddleDown && !wasMiddleMouseDown && isOver) {
            MinecraftClient.getInstance().keyboard.setClipboard(rawText);
            copyFeedbackTicks = 40;
            MinecraftClient.getInstance().getSoundManager().play(new PositionedSoundInstance(SoundEvents.ITEM_BUNDLE_DROP_CONTENTS, SoundCategory.PLAYERS, 0.1f, 1.0f, net.minecraft.util.math.random.Random.create(), 0, 0, 0));
        }

        // Right Click: Reset tip to default center-bottom position
        if (isRightDown && !wasRightMouseDown && isOver) {
            tipPctX = 0.5f;
            tipPctY = 0.75f;
            saveAllData();
        }

        // Left Click/Drag Logic: Move the tip around the screen
        if (isLeftDown) {
            if (!wasMouseDown && isOver) {
                isDragging = false;
                startMouseX = mouseX;
                startMouseY = mouseY;
                dragOffX = mouseX - tipX;
                dragOffY = mouseY - tipY;
            }
            if (Math.abs(mouseX - startMouseX) > 3 || Math.abs(mouseY - startMouseY) > 3) {
                if (isOver || isDragging) {
                    isDragging = true;
                    float newX = MathHelper.clamp((float)(mouseX - dragOffX), 80, this.width - 80);
                    float newY = MathHelper.clamp((float)(mouseY - dragOffY), 25, this.height - 20);
                    // Update and store new percentage based on drag
                    tipPctX = newX / this.width;
                    tipPctY = newY / this.height;
                }
            }
        } else if (wasMouseDown) {
            if (isDragging) saveAllData();
            else if (isOver) createNewTip(); // Single click to cycle tips
            isDragging = false;
        }
        wasMouseDown = isLeftDown;
        wasRightMouseDown = isRightDown;
        wasMiddleMouseDown = isMiddleDown;

        // --- DRAWING ---
        float entranceOpacity = appearanceTicks / 40.0f;
        int tipAlpha = (int)(entranceOpacity * 255);

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(tipX, tipY - 10);
        context.getMatrices().scale(currentScale, currentScale);
        context.getMatrices().translate(-tipX, -(tipY - 10));

        // Get Category Data (Title & Icon) from TipCategory class
        TipCategory.Category cat = TipCategory.getCategory(rawText.toLowerCase());
        String headerTitle = cat.title();
        ItemStack iconStack = cat.icon();

        int hW = this.textRenderer.getWidth("§l" + headerTitle);
        int iconX = (int)tipX - (hW / 2) - 14;
        int iconY = (int)tipY - 11;

        // "New Tip" indicator star
        if (isNew) {
            float pulse = (MathHelper.sin(internalTime * 6f) + 1f) / 2f;
            int starC = (tipAlpha << 24) | (255 << 16) | ((int)(200 * pulse + 55) << 8) | 50;
            context.getMatrices().pushMatrix();
            context.getMatrices().translate(iconX - 3, iconY - 7);
            context.getMatrices().scale(0.75f, 0.75f);
            context.drawText(this.textRenderer, "*", 0, 0, starC, true);
            context.getMatrices().popMatrix();
        }

        // Draw Category Icon
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(iconX, iconY);
        context.getMatrices().scale(entranceOpacity, entranceOpacity);
        context.drawItem(iconStack, -8, -8);
        context.getMatrices().popMatrix();

        // Draw Header
        context.drawText(this.textRenderer, "§l" + headerTitle, (int)tipX - (hW / 2), (int)tipY - 15, (tipAlpha << 24) | 0xFFFFFF, false);

        // Draw Tip ID when hovered
        if (isOver) {
            String ghostID = "§7#" + (currentTipIndex + 1);
            context.drawText(this.textRenderer, ghostID, (int)tipX + (hW / 2) + 4, (int)tipY - 15, (tipAlpha << 24) | 0xAAAAAA, false);
        }

        context.getMatrices().popMatrix();

        // Copy success feedback text
        if (copyFeedbackTicks > 0) {
            String copyText = "§a§oCopied to Clipboard!";
            context.drawText(this.textRenderer, copyText, (int)tipX - (this.textRenderer.getWidth(copyText) / 2), (int)tipY - 30, (255 << 24) | 0x55FF55, false);
        }

        // Draw wrapped Tip text
        int currentY = (int)tipY;
        for (OrderedText line : wrappedLines) {
            context.drawText(this.textRenderer, line, (int)tipX - (this.textRenderer.getWidth(line) / 2), currentY, (tipAlpha << 24) | 0xFFFFFF, false);
            currentY += 10;
        }
    }
}
