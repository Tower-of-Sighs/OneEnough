package cc.sighs.oneenoughitem.client.gui.util;

import cc.sighs.oneenoughitem.Oneenoughitem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class GuiUtils {
    private static final Identifier BACKGROUND_TEXTURE = Identifier.fromNamespaceAndPath(Oneenoughitem.MODID, "textures/gui/background.png");
    private static final Identifier BUTTON_NORMAL_TEXTURE = Identifier.fromNamespaceAndPath(Oneenoughitem.MODID, "textures/gui/button_0.png");
    private static final Identifier BUTTON_PRESSED_TEXTURE = Identifier.fromNamespaceAndPath(Oneenoughitem.MODID, "textures/gui/button_1.png");
    private static final Identifier EDITOR_TEXTURE = Identifier.fromNamespaceAndPath(Oneenoughitem.MODID, "textures/gui/editor.png");
    private static final Identifier ITEM_BOX_TEXTURE = Identifier.fromNamespaceAndPath(Oneenoughitem.MODID, "textures/gui/item_box.png");


    public static void drawTiledBackground(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        int textureSize = 16;

        for (int tileX = 0; tileX < width; tileX += textureSize) {
            for (int tileY = 0; tileY < height; tileY += textureSize) {
                int tileWidth = Math.min(textureSize, width - tileX);
                int tileHeight = Math.min(textureSize, height - tileY);

                blit(graphics,BACKGROUND_TEXTURE,
                        x + tileX, y + tileY,
                        0, 0,
                        tileWidth, tileHeight,
                        textureSize, textureSize);
            }
        }
    }

    public static void drawItemBox(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        int textureSize = 18;
        int border = 6;
        drawNinePatch(graphics, ITEM_BOX_TEXTURE, x, y, width, height, textureSize, border);
    }

    public static void drawStretchableButton(GuiGraphicsExtractor graphics, int x, int y, int width, int height, boolean pressed) {
        Identifier texture = pressed ? BUTTON_PRESSED_TEXTURE : BUTTON_NORMAL_TEXTURE;
        int textureSize = 18;
        int border = 6;

        drawNinePatch(graphics, texture, x, y, width, height, textureSize, border);
    }

    private static void drawNinePatch(GuiGraphicsExtractor graphics, Identifier texture,
                                      int x, int y, int width, int height,
                                      int textureSize, int border) {
        // 角落（不拉伸）
        blit(graphics,texture, x, y, 0, 0, border, border, textureSize, textureSize); // 左上
        blit(graphics,texture, x + width - border, y, textureSize - border, 0, border, border, textureSize, textureSize); // 右上
        blit(graphics,texture, x, y + height - border, 0, textureSize - border, border, border, textureSize, textureSize); // 左下
        blit(graphics,texture, x + width - border, y + height - border, textureSize - border, textureSize - border, border, border, textureSize, textureSize); // 右下

        // 上边（横向拉伸）
        if (width > border * 2) {
            blit(graphics,texture,
                    x + border, y,
                    width - border * 2, border,           // 目标尺寸
                    border, 0,                             // 源UV起点
                    textureSize - border * 2, border,      // 源区尺寸（只取顶部边框带）
                    textureSize, textureSize);
        }

        // 下边（横向拉伸）
        if (width > border * 2) {
            blit(graphics,texture,
                    x + border, y + height - border,
                    width - border * 2, border,
                    border, textureSize - border,
                    textureSize - border * 2, border,
                    textureSize, textureSize);
        }

        // 左边（纵向拉伸）
        if (height > border * 2) {
            blit(graphics,texture,
                    x, y + border,
                    border, height - border * 2,
                    0, border,
                    border, textureSize - border * 2,
                    textureSize, textureSize);
        }

        // 右边（纵向拉伸）
        if (height > border * 2) {
            blit(graphics,texture,
                    x + width - border, y + border,
                    border, height - border * 2,
                    textureSize - border, border,
                    border, textureSize - border * 2,
                    textureSize, textureSize);
        }

        // 中心（双轴拉伸）
        if (width > border * 2 && height > border * 2) {
            blit(graphics,texture,
                    x + border, y + border,
                    width - border * 2, height - border * 2,
                    border, border,
                    textureSize - border * 2, textureSize - border * 2,
                    textureSize, textureSize);
        }
    }

    public static void drawPanelBackground(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        drawTiledBackground(graphics, x, y, width, height);
    }

    public static void drawListBackground(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        drawTiledBackground(graphics, x, y, width, height);
    }

    public static void drawFileEntryBackground(GuiGraphicsExtractor graphics, int x, int y, int width, int height, boolean isHovered, boolean isSelected) {
        if (isSelected) {
            graphics.fill(x, y, x + width, y + height, 0x80404040);
        } else if (isHovered) {
            graphics.fill(x, y, x + width, y + height, 0x40FFFFFF);
        }
    }

    public static void drawFileIcon(GuiGraphicsExtractor graphics, int x, int y, String fileType) {
        graphics.fill(x, y, x + 12, y + 12, 0xFF4CAF50);
        drawBorder(graphics, x, y, 12, 12, 0xFF2E7D32);
        graphics.text(Minecraft.getInstance().font, fileType, x + 3, y + 2, 0xFFFFFFFF);
    }

    public static void drawObjectDropdownBackground(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        drawTiledBackground(graphics, x, y, width, height);
    }

    public static Button createButton(Component text, Button.OnPress onPress, int x, int y, int width, int height) {
        return createCustomButton(text, onPress, x, y, width, height);
    }

    public static Button createObjectDropdownButton(Component text, Button.OnPress onPress, int x, int y, int width, int height) {
        return createCustomButton(text, onPress, x, y, width, height);
    }

    private static Button createCustomButton(Component text, Button.OnPress onPress, int x, int y, int width, int height) {
        return new Button(x, y, width, height, text, onPress, Button.DEFAULT_NARRATION) {
            @Override
            protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
                renderCustomButton(graphics, this, mouseX, mouseY, partialTick);
                this.extractDefaultLabel(graphics.textRendererForWidget(
                        this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
            }
        };
    }

    public static void blit(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int u, int v,
                            int width, int height, int textureWidth, int textureHeight) {
        graphics.blit(texture, x, y, x + width, y + height,
                (float) u / textureWidth, (float) (u + width) / textureWidth,
                (float) v / textureHeight, (float) (v + height) / textureHeight);
    }

    public static void blit(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int width, int height,
                            int u, int v, int sourceWidth, int sourceHeight, int textureWidth, int textureHeight) {
        graphics.blit(texture, x, y, x + width, y + height,
                (float) u / textureWidth, (float) (u + sourceWidth) / textureWidth,
                (float) v / textureHeight, (float) (v + sourceHeight) / textureHeight);
    }

    private static void renderCustomButton(GuiGraphicsExtractor graphics, Button button, int mouseX, int mouseY, float partialTick) {
        boolean hovered = button.isHovered();
        boolean mouseDown = GLFW.glfwGetMouseButton(
                Minecraft.getInstance().getWindow().handle(),
                GLFW.GLFW_MOUSE_BUTTON_LEFT
        ) == GLFW.GLFW_PRESS;

        boolean pressed = hovered && mouseDown;

        drawStretchableButton(graphics, button.getX(), button.getY(), button.getWidth(), button.getHeight(), pressed);

    }

    private static void drawBorder(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int borderColor) {
        graphics.fill(x - 1, y - 1, x + width + 1, y, borderColor); // 顶边
        graphics.fill(x - 1, y + height, x + width + 1, y + height + 1, borderColor); // 底边
        graphics.fill(x - 1, y, x, y + height, borderColor); // 左边
        graphics.fill(x + width, y, x + width + 1, y + height, borderColor); // 右边
    }

    private static void drawHighlightBorder(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + 1, 0x80FFFFFF); // 顶亮
        graphics.fill(x, y + height - 1, x + width, y + height, 0x40FFFFFF); // 底亮
    }

    public static void drawEditorPanel(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        int textureSize = 256;
        int border = 20;
        drawNinePatch(graphics, EDITOR_TEXTURE, x, y, width, height, textureSize, border);
    }
}
