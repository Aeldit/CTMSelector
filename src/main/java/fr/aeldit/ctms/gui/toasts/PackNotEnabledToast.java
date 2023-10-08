/*
 * Copyright (c) 2023  -  Made by Aeldit
 *
 *              GNU LESSER GENERAL PUBLIC LICENSE
 *                  Version 3, 29 June 2007
 *
 *  Copyright (C) 2007 Free Software Foundation, Inc. <https://fsf.org/>
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 *
 *
 * This version of the GNU Lesser General Public License incorporates
 * the terms and conditions of version 3 of the GNU General Public
 * License, supplemented by the additional permissions listed in the LICENSE.txt file
 * in the repo of this mod (https://github.com/Aeldit/Cyan)
 */

package fr.aeldit.ctms.gui.toasts;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Environment(EnvType.CLIENT)
public class PackNotEnabledToast implements Toast
{
    private static final Identifier TEXTURE = new Identifier("toast/system");
    private final Type type;
    private final Text title;
    private final List<OrderedText> lines;
    private long startTime;
    private boolean justUpdated;
    private final int width;

    public PackNotEnabledToast(Type type, Text title, @Nullable Text description)
    {
        this(type, title, getTextAsList(description), Math.max(160, 30 + Math.max(MinecraftClient.getInstance().textRenderer.getWidth(title), description == null ? 0 : MinecraftClient.getInstance().textRenderer.getWidth(description))));
    }

    private PackNotEnabledToast(Type type, Text title, List<OrderedText> lines, int width)
    {
        this.type = type;
        this.title = title;
        this.lines = lines;
        this.width = width;
    }

    private static @NotNull ImmutableList<OrderedText> getTextAsList(@Nullable Text text)
    {
        return text == null ? ImmutableList.of() : ImmutableList.of(text.asOrderedText());
    }

    public int getWidth()
    {
        return this.width;
    }

    public int getHeight()
    {
        return 20 + Math.max(this.lines.size(), 1) * 12;
    }

    public Toast.Visibility draw(DrawContext context, ToastManager manager, long startTime)
    {
        if (this.justUpdated)
        {
            this.startTime = startTime;
            this.justUpdated = false;
        }

        int i = this.getWidth();
        int j;
        if (i == 160 && this.lines.size() <= 1)
        {
            context.drawGuiTexture(TEXTURE, 0, 0, i, this.getHeight());
        }
        else
        {
            j = this.getHeight();
            int l = Math.min(4, j - 28);
            this.drawPart(context, i, 0, 0, 28);

            for (int m = 28; m < j - l; m += 10)
            {
                this.drawPart(context, i, 16, m, Math.min(16, j - m - l));
            }

            this.drawPart(context, i, 32 - l, j - l, l);
        }

        if (this.lines == null)
        {
            context.drawText(manager.getClient().textRenderer, this.title, 18, 12, -256, false);
        }
        else
        {
            context.drawText(manager.getClient().textRenderer, this.title, 18, 7, -256, false);

            for (j = 0; j < this.lines.size(); ++j)
            {
                context.drawText(manager.getClient().textRenderer, this.lines.get(j), 18, 18 + j * 12, -1, false);
            }
        }

        return (double) (startTime - this.startTime) < (double) this.type.displayDuration * manager.getNotificationDisplayTimeMultiplier() ? Visibility.SHOW : Visibility.HIDE;
    }

    private void drawPart(@NotNull DrawContext context, int i, int j, int k, int l)
    {
        int m = j == 0 ? 20 : 5;
        int n = Math.min(60, i - m);
        Identifier identifier = TEXTURE;
        context.drawGuiTexture(identifier, 160, 32, 0, j, 0, k, m, l);

        for (int o = m; o < i - n; o += 64)
        {
            context.drawGuiTexture(identifier, 160, 32, 32, j, o, k, Math.min(64, i - o - n), l);
        }

        context.drawGuiTexture(identifier, 160, 32, 160 - n, j, i - n, k, n, l);
    }

    public Type getType()
    {
        return this.type;
    }

    @Environment(EnvType.CLIENT)
    public enum Type
    {
        PACK_NOT_ENABLED;

        final long displayDuration;

        Type(long displayDuration)
        {
            this.displayDuration = displayDuration;
        }

        Type()
        {
            this(5000L);
        }
    }
}
