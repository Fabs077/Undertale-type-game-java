package com.rpg.ui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.rpg.ui.theme.Fonts;
import com.rpg.ui.theme.Palette;

public class PixelButton {
    private final float x, y, width, height;
    private final String label;
    private final GlyphLayout layout;
    private boolean selected;

    public PixelButton(float x, float y, float width, float height, String label) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.label = label;
        this.layout = new GlyphLayout(Fonts.sansMedium, label);
    }

    public void render(ShapeRenderer shapes, SpriteBatch batch) {
        int bw = selected ? 4 : 2;
        Color borderColor = selected ? Palette.PRIMARY : Palette.ON_SURFACE;

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(borderColor);
        shapes.rect(x, y, width, height);
        shapes.setColor(Palette.BG);
        shapes.rect(x + bw, y + bw, width - 2 * bw, height - 2 * bw);
        shapes.end();

        batch.begin();
        Fonts.sansMedium.setColor(selected ? Palette.PRIMARY : Palette.ON_SURFACE);
        float textX = x + (width - layout.width) / 2f;
        float textY = y + (height + layout.height) / 2f;
        // Draw with the label string so the font's current color is used at draw time.
        // Drawing from a pre-computed GlyphLayout would use the baked-in color from construction.
        Fonts.sansMedium.draw(batch, label, textX, textY);
        batch.end();
    }

    public void setSelected(boolean selected) { this.selected = selected; }
    public boolean isSelected()               { return selected; }
    public String getLabel()                  { return label; }
}
