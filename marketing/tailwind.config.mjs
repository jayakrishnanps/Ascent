/** @type {import('tailwindcss').Config} */
export default {
  content: ['./src/**/*.{astro,html,js,jsx,md,mdx,svelte,ts,tsx,vue}'],
  darkMode: "class",
  theme: {
    extend: {
      "colors": {
          "on-error": "#690005",
          "inverse-surface": "#e2e2e2",
          "surface-bright": "#393939",
          "on-secondary-fixed": "#280057",
          "lavender-tint": "#E0D4F5",
          "pure-white": "#FFFFFF",
          "on-error-container": "#ffdad6",
          "surface-container-highest": "#353535",
          "void-purple": "#120526",
          "inverse-primary": "#7832d9",
          "on-primary-fixed-variant": "#5f00c0",
          "on-background": "#e2e2e2",
          "on-surface": "#e2e2e2",
          "surface-container-high": "#2a2a2a",
          "on-tertiary-fixed": "#2f1500",
          "secondary-container": "#5d1caf",
          "on-surface-variant": "#cdc3d7",
          "surface-container-lowest": "#0e0e0e",
          "tertiary-fixed-dim": "#ffb77d",
          "on-tertiary-container": "#ffd0ad",
          "secondary": "#d6baff",
          "tertiary": "#ffb77d",
          "surface-dim": "#131313",
          "secondary-fixed": "#ecdcff",
          "on-secondary": "#430089",
          "background": "#000000",
          "on-secondary-container": "#c8a4ff",
          "error-container": "#93000a",
          "surface-tint": "#d6baff",
          "primary-container": "#7832d9",
          "surface-container-low": "#1b1b1b",
          "error": "#ffb4ab",
          "tertiary-fixed": "#ffdcc3",
          "surface-variant": "#353535",
          "tertiary-container": "#914d00",
          "on-tertiary-fixed-variant": "#6e3900",
          "primary-fixed-dim": "#d6baff",
          "on-primary": "#42008a",
          "on-primary-container": "#e4d0ff",
          "inverse-on-surface": "#303030",
          "primary-fixed": "#ecdcff",
          "primary": "#d6baff",
          "outline-variant": "#4b4454",
          "surface": "#131313",
          "secondary-fixed-dim": "#d6baff",
          "on-secondary-fixed-variant": "#5d1caf",
          "on-primary-fixed": "#270057",
          "outline": "#968da0",
          "surface-container": "#1f1f1f",
          "on-tertiary": "#4d2600"
      },
      "borderRadius": {
          "DEFAULT": "1rem",
          "lg": "2rem",
          "xl": "3rem",
          "full": "9999px"
      },
      "spacing": {
          "container-max": "1280px",
          "unit": "8px",
          "gutter": "24px",
          "section-gap": "120px",
          "margin-safe": "32px"
      },
      "fontFamily": {
          "label-md": ["Space Grotesk", "sans-serif"],
          "body-lg": ["Inter", "sans-serif"],
          "headline-lg": ["Space Grotesk", "sans-serif"],
          "headline-md": ["Space Grotesk", "sans-serif"],
          "display-lg": ["Space Grotesk", "sans-serif"],
          "body-md": ["Inter", "sans-serif"],
          "display-lg-mobile": ["Space Grotesk", "sans-serif"]
      },
      "fontSize": {
          "label-md": ["14px", { "lineHeight": "1.2", "letterSpacing": "0.1em", "fontWeight": "500" }],
          "body-lg": ["18px", { "lineHeight": "1.6", "fontWeight": "400" }],
          "headline-lg": ["48px", { "lineHeight": "1.2", "fontWeight": "600" }],
          "headline-md": ["32px", { "lineHeight": "1.3", "fontWeight": "600" }],
          "display-lg": ["72px", { "lineHeight": "1.1", "letterSpacing": "-0.02em", "fontWeight": "700" }],
          "body-md": ["16px", { "lineHeight": "1.6", "fontWeight": "400" }],
          "display-lg-mobile": ["40px", { "lineHeight": "1.2", "fontWeight": "700" }]
      }
    }
  }
}
