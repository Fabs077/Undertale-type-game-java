---
name: Bit-Quest Monolith
colors:
  surface: '#131313'
  surface-dim: '#131313'
  surface-bright: '#393939'
  surface-container-lowest: '#0e0e0e'
  surface-container-low: '#1b1b1b'
  surface-container: '#1f1f1f'
  surface-container-high: '#2a2a2a'
  surface-container-highest: '#353535'
  on-surface: '#e2e2e2'
  on-surface-variant: '#dac2ad'
  inverse-surface: '#e2e2e2'
  inverse-on-surface: '#303030'
  outline: '#a28d79'
  outline-variant: '#544433'
  surface-tint: '#ffb869'
  primary: '#ffc485'
  on-primary: '#482900'
  primary-container: '#ff9d00'
  on-primary-container: '#663c00'
  inverse-primary: '#885200'
  secondary: '#c6c6c7'
  on-secondary: '#2f3131'
  secondary-container: '#454747'
  on-secondary-container: '#b4b5b5'
  tertiary: '#d6d600'
  on-tertiary: '#323200'
  tertiary-container: '#baba00'
  on-tertiary-container: '#484800'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#ffdcbb'
  primary-fixed-dim: '#ffb869'
  on-primary-fixed: '#2c1700'
  on-primary-fixed-variant: '#673d00'
  secondary-fixed: '#e2e2e2'
  secondary-fixed-dim: '#c6c6c7'
  on-secondary-fixed: '#1a1c1c'
  on-secondary-fixed-variant: '#454747'
  tertiary-fixed: '#eaea00'
  tertiary-fixed-dim: '#cdcd00'
  on-tertiary-fixed: '#1d1d00'
  on-tertiary-fixed-variant: '#494900'
  background: '#131313'
  on-background: '#e2e2e2'
  surface-variant: '#353535'
typography:
  headline-lg:
    fontFamily: Space Mono
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -1px
  headline-md:
    fontFamily: Space Mono
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
    letterSpacing: 0px
  body-lg:
    fontFamily: Space Mono
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-md:
    fontFamily: Space Mono
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-lg:
    fontFamily: JetBrains Mono
    fontSize: 14px
    fontWeight: '700'
    lineHeight: 16px
  label-sm:
    fontFamily: JetBrains Mono
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 14px
  headline-lg-mobile:
    fontFamily: Space Mono
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
spacing:
  base: 4px
  unit-1: 4px
  unit-2: 8px
  unit-4: 16px
  unit-8: 32px
  gutter: 16px
  margin-mobile: 16px
  margin-desktop: 32px
---

## Brand & Style
The design system is a love letter to the 8-bit era, specifically focusing on the high-contrast, atmospheric aesthetics of early tactical RPGs and dungeon crawlers. It targets a nostalgic audience that values clarity, digital grit, and a "hardware-limited" aesthetic.

The visual style is a hybrid of **Retro-Brutalism** and **Minimalism**. It eschews modern affordances like gradients, soft shadows, and rounded corners in favor of rigid geometry, sharp aliasing, and a stark black-canvas approach. The interface should feel like a glowing CRT monitor in a dark room—purposeful, immersive, and uncompromising.

## Colors
This design system utilizes a high-contrast, limited palette to simulate the technical constraints of vintage display hardware. 

- **Primary Background:** Absolute black (#000000) provides the canvas, ensuring that every interactive element "pops" with maximum luminance.
- **Primary Accent (Action/Focus):** A vibrant orange (#FF9D00) is reserved for active states, selection indicators, and primary borders. 
- **Secondary Accent (Data/Passive):** Pure white (#FFFFFF) handles all standard text, passive borders, and secondary iconography.
- **Status/Utility:** Yellow (#FFFF00) is utilized sparingly for critical status updates, such as health warnings, XP gains, or special item highlights.

## Typography
Typography in this design system is strictly monospaced to reinforce the 8-bit computer aesthetic. **Space Mono** serves as the primary typeface for headlines and body copy, providing a geometric, slightly futuristic character that feels technical yet legible. **JetBrains Mono** is used for smaller labels and UI metadata where maximum clarity in tight spaces is required.

All text must maintain high contrast against the black background. Do not use font weights below 400. For mobile, headline sizes are scaled down aggressively to ensure they remain within the bounds of rigid "pixel" windows.

## Layout & Spacing
The layout operates on a **Fixed Grid** system built on a 4px base unit, mimicking a pixel grid. Everything must align to an 8px or 16px rhythm to prevent "sub-pixel" rendering issues and maintain the blocky, retro feel.

- **The Panel System:** Instead of fluid containers, use "Windows" or "Panels" that have fixed widths or height increments. 
- **Desktop:** A 12-column grid with 16px gutters. Content should be centered within a max-width container that feels like a standalone terminal.
- **Mobile:** Single-column layout with 16px margins. Panels should stack vertically with a clear 8px gap between them.
- **Alignment:** Never use soft-centering or complex flex-grow logic that results in uneven spacing. If a gap exists, it should be a multiple of 8px.

## Elevation & Depth
In this design system, depth is purely structural and symbolic rather than physical. 

- **Layering:** Hierarchy is achieved through "Stacked Panels." A panel appearing over another does not use a shadow; instead, it uses a thick 4px border (White or Orange) to separate itself from the elements below.
- **Tonal Layers:** There are no gray scales. Depth is communicated via the thickness of borders. A primary modal might have a 4px border, while a sub-menu has a 2px border.
- **No Blurs:** Transparency and blurs are strictly forbidden. Backgrounds are always opaque #000000.

## Shapes
The shape language is strictly **Sharp (0px)**. Rounding corners is inconsistent with the pixel-art narrative. Every button, input field, and container must be a perfect rectangle or square.

To create visual interest without curves, use "corner-cutting" or "stepped" geometry for special containers (manually drawing a 4px inward step at corners using borders) to simulate a more complex 8-bit architectural style.

## Components
- **Buttons:** Rectangular blocks with a 2px White border. On hover or active state, the border thickness increases to 4px and changes to Orange (#FF9D00). Text remains White or switches to Orange.
- **Input Fields:** Black background with a 2px White border. The cursor is a blinking solid Orange block. Labels are placed inside the top-left border line, interrupting the border (the "fieldset" style).
- **Cards/Panels:** Heavy 4px borders. Use "header strips"—a solid White bar at the top of a panel with Black text to indicate the section title.
- **Chips/Tags:** Small boxes with a 1px border. When "Selected," they fill with Orange and use Black text.
- **Checkboxes/Radios:** Square boxes only. A "checked" state is indicated by a solid 8x8 pixel Orange square inside the White border.
- **Icons:** Must be pixel-perfect icons on a 16x16 or 32x32 grid. Avoid anti-aliasing; icons should be composed of distinct, sharp blocks of color (White or Orange).
- **Dialogue Box:** A large panel at the bottom of the viewport with a "blinking arrow" indicator in the bottom-right corner to signal more text, classic to the RPG genre.